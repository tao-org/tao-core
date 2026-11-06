/*
 * Copyright (C) 2018 CS ROMANIA
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package ro.cs.tao.persistence.config;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import ro.cs.tao.configuration.ConfigurationManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Synchronizes table and column definitions from the persistence SQL scripts with
 * an existing PostgreSQL database.
 */
final class DatabaseSchemaSynchronizer {

    private static final String ENABLED_PROPERTY = "persistence.schema.synchronize";
    private static final String SCHEMA_SCRIPTS = "classpath*:persistence/scripts/02-schema/*.sql";
    private static final String TABLE_SCRIPTS = "classpath*:persistence/scripts/04-tables/*.sql";
    private static final Pattern CREATE_SCHEMA_PATTERN = Pattern.compile(
            "(?is)^\\s*CREATE\\s+SCHEMA\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?(.+?)(?:\\s+AUTHORIZATION tao)");
    private static final Pattern CREATE_TABLE_PATTERN = Pattern.compile(
            "(?is)^\\s*CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?(.+?)\\s*\\(");
    private static final Pattern CREATE_SEQUENCE_PATTERN = Pattern.compile(
            "(?is)^\\s*CREATE\\s+SEQUENCE\\s+(?!IF\\s+NOT\\s+EXISTS\\s+)(.+)$");
    private static final Pattern ALTER_TABLE_PATTERN = Pattern.compile(
            "(?is)^\\s*ALTER\\s+TABLE\\s+(.+?)\\s+(?:ADD\\s+CONSTRAINT|ALTER\\s+COLUMN)\\b.*");
    private static final Pattern ALTER_SEQUENCE_OWNED_PATTERN = Pattern.compile(
            "(?is).*\\bOWNED\\s+BY\\s+(.+?)\\s*\\.\\s*[^.\\s;]+\\s*$");
    private static final List<String> COLUMN_TERMINATORS = List.of(
            "not", "null", "default", "primary", "references", "constraint", "unique", "check", "collate");

    private final Logger logger;

    DatabaseSchemaSynchronizer(Logger logger) {
        this.logger = logger;
    }

    void synchronize(Connection connection, String databaseProvider) {
        if (!Boolean.parseBoolean(ConfigurationManager.getInstance().getValue(ENABLED_PROPERTY, "true"))) {
            logger.fine("Database schema synchronization is disabled");
            return;
        }
        if (!"PostgreSQL".equals(databaseProvider)) {
            logger.fine("Database schema synchronization is skipped for " + databaseProvider);
            return;
        }
        try {
            final List<String> schemas = loadSchemas();
            if (schemas.isEmpty()) {
                logger.warning("No schemas were found in " + SCHEMA_SCRIPTS);
            }
            for (String schema : schemas) {
                if (!schemaExists(connection, schema)) {
                    executeNewSchemaStatement(connection, schema);
                }
            }
            final List<TableDefinition> definitions = loadDefinitions();
            if (definitions.isEmpty()) {
                logger.warning("No table definitions were found in " + TABLE_SCRIPTS);
                return;
            }
            final DatabaseMetaData metaData = connection.getMetaData();
            for (TableDefinition definition : definitions) {
                synchronizeTable(connection, metaData, definition);
            }
        } catch (Exception e) {
            logger.warning("Database schema synchronization failed: " + e.getMessage());
        }
    }

    private List<String> loadSchemas() throws IOException {
        final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(
                DatabaseSchemaSynchronizer.class.getClassLoader());
        final Resource[] resources = resolver.getResources(SCHEMA_SCRIPTS);
        final List<Resource> sortedResources = new ArrayList<>(List.of(resources));
        sortedResources.sort(Comparator.comparing(resource -> {
            try {
                return resource.getURL().toExternalForm();
            } catch (IOException e) {
                return resource.getDescription();
            }
        }));

        final List<String> definitions = new ArrayList<>();
        for (Resource resource : sortedResources) {
            if (!resource.isReadable()) {
                logger.warning("Cannot read persistence script " + resource.getDescription());
                continue;
            }
            final String sql;
            try (var inputStream = resource.getInputStream()) {
                sql = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
            definitions.addAll(parseSchemas(removeLineComments(sql), resource.getDescription()));
        }
        return definitions;
    }

    private List<TableDefinition> loadDefinitions() throws IOException {
        final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(
                DatabaseSchemaSynchronizer.class.getClassLoader());
        final Resource[] resources = resolver.getResources(TABLE_SCRIPTS);
        final List<Resource> sortedResources = new ArrayList<>(List.of(resources));
        sortedResources.sort(Comparator.comparing(resource -> {
            try {
                return resource.getURL().toExternalForm();
            } catch (IOException e) {
                return resource.getDescription();
            }
        }));

        final List<TableDefinition> definitions = new ArrayList<>();
        for (Resource resource : sortedResources) {
            if (!resource.isReadable()) {
                logger.warning("Cannot read persistence script " + resource.getDescription());
                continue;
            }
            final String sql;
            try (var inputStream = resource.getInputStream()) {
                sql = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
            definitions.addAll(parseDefinitions(removeLineComments(sql), resource.getDescription()));
        }
        return definitions;
    }

    private List<String> parseSchemas(String sql, String source) {
        final List<String> statements = splitStatements(sql);
        final List<String> definitions = new ArrayList<>();
        TableDefinition current = null;
        for (String statement : statements) {
            final Matcher createTableMatcher = CREATE_SCHEMA_PATTERN.matcher(statement);
            if (createTableMatcher.find()) {
                definitions.add(createTableMatcher.group(1));
                continue;
            }
            if (current == null) {
                continue;
            }
        }
        return definitions;
    }

    private List<TableDefinition> parseDefinitions(String sql, String source) {
        final List<String> statements = splitStatements(sql);
        final List<TableDefinition> definitions = new ArrayList<>();
        TableDefinition current = null;
        for (String statement : statements) {
            final Matcher createTableMatcher = CREATE_TABLE_PATTERN.matcher(statement);
            if (createTableMatcher.find()) {
                final QualifiedName tableName = QualifiedName.parse(createTableMatcher.group(1));
                current = new TableDefinition(tableName, ensureCreateTableIfNotExists(statement), source);
                current.followUpStatements.add(current.createStatement);
                current.columns.putAll(parseColumns(statement));
                definitions.add(current);
                continue;
            }
            if (current == null) {
                continue;
            }
            if (isDropStatement(statement)) {
                continue;
            }
            if (isStatementForTable(statement, current.name)) {
                current.followUpStatements.add(normalizeFollowUpStatement(statement));
            }
        }
        return definitions;
    }

    private void synchronizeTable(Connection connection, DatabaseMetaData metaData, TableDefinition definition) {
        try {
            if (!tableExists(metaData, definition.name)) {
                executeNewTableStatements(connection, definition);
                return;
            }
            final Map<String, ColumnMetadata> actualColumns = readColumns(connection, metaData, definition.name);
            for (ColumnDefinition expectedColumn : definition.columns.values()) {
                final ColumnMetadata actualColumn = actualColumns.get(expectedColumn.normalizedName);
                if (actualColumn == null) {
                    try {
                        execute(connection, "ALTER TABLE " + definition.name.sqlName + " ADD COLUMN " + expectedColumn.sqlDefinition,
                                "Added database column " + definition.name.displayName + "." + expectedColumn.name);
                    } catch (SQLException e) {
                        logColumnWarning(definition, expectedColumn, "definition", e);
                    }
                    continue;
                }
                //synchronizeColumn(connection, definition, expectedColumn, actualColumn);
            }
        } catch (SQLException e) {
            logger.warning("Cannot synchronize table " + definition.name.displayName + ": " + e.getMessage());
        }
    }

    private void executeNewSchemaStatement(Connection connection, String schema) throws SQLException {
        logger.info("Creating missing database schema " + schema);
        execute(connection, "CREATE SCHEMA IF NOT EXISTS " + schema + " AUTHORIZATION tao", "Created database schema " + schema);
    }

    private void executeNewTableStatements(Connection connection, TableDefinition definition) throws SQLException {
        logger.info("Creating missing database table " + definition.name.displayName + " from " + definition.source);
        for (int i = 0; i < definition.followUpStatements.size(); i++) {
            final String statement = definition.followUpStatements.get(i);
            try {
                execute(connection, statement, "Applied schema statement for " + definition.name.displayName);
            } catch (SQLException e) {
                if (i == 0) {
                    throw e;
                }
                logger.warning("Cannot apply schema statement for " + definition.name.displayName + ": " + e.getMessage());
            }
        }
    }

    private void synchronizeColumn(Connection connection, TableDefinition table, ColumnDefinition expected,
                                   ColumnMetadata actual) {
        final String expectedType = normalizeType(expected.type);
        final String actualType = normalizeType(actual.type);
        if (!expectedType.isBlank() && !actualType.isBlank() && !Objects.equals(expectedType, actualType)) {
            try {
                logger.fine("Expected: " + expected + ", actual: " + actual);
                execute(connection,
                        "ALTER TABLE " + table.name.sqlName + " ALTER COLUMN " + expected.sqlName
                                + " TYPE " + alterType(expected.type) + " USING " + expected.sqlName + "::" + alterType(expected.type),
                        "Changed database column type " + table.name.displayName + "." + expected.name
                                + " from " + actual.type + " to " + expected.type);
            } catch (SQLException e) {
                logColumnWarning(table, expected, "type", e);
            }
        }

        if (expected.nullable != null && actual.nullable != null && !expected.nullable.equals(actual.nullable)) {
            final String nullabilityStatement = "ALTER TABLE " + table.name.sqlName + " ALTER COLUMN " + expected.sqlName
                    + (expected.nullable ? " DROP NOT NULL" : " SET NOT NULL");
            try {
                execute(connection, nullabilityStatement,
                        "Changed database column nullability " + table.name.displayName + "." + expected.name);
            } catch (SQLException e) {
                logColumnWarning(table, expected, "nullability", e);
            }
        }

        if (!isSerialType(expected.type)
                && !Objects.equals(normalizeDefault(expected.defaultValue), normalizeDefault(actual.defaultValue))) {
            final String defaultStatement = "ALTER TABLE " + table.name.sqlName + " ALTER COLUMN " + expected.sqlName
                    + (expected.defaultValue == null ? " DROP DEFAULT" : " SET DEFAULT " + expected.defaultValue);
            try {
                execute(connection, defaultStatement,
                        "Changed database column default " + table.name.displayName + "." + expected.name);
            } catch (SQLException e) {
                logColumnWarning(table, expected, "default", e);
            }
        }
    }

    private boolean schemaExists(Connection connection, String schema) throws SQLException {
        try (ResultSet schemas = connection.getMetaData().getSchemas(null, schema.replace("\"", ""))) {
            return schemas.next();
        }
    }

    private boolean tableExists(DatabaseMetaData metaData, QualifiedName tableName) throws SQLException {
        try (ResultSet tables = metaData.getTables(null, tableName.schema, tableName.table, new String[]{"TABLE"})) {
            return tables.next();
        }
    }

    private Map<String, ColumnMetadata> readColumns(Connection connection, DatabaseMetaData metaData, QualifiedName tableName)
            throws SQLException {
        final Map<String, ColumnMetadata> columns = new LinkedHashMap<>();
        try (ResultSet resultSet = metaData.getColumns(null, tableName.schema, tableName.table, null)) {
            while (resultSet.next()) {
                final String name = resultSet.getString("COLUMN_NAME");
                final int nullable = resultSet.getInt("NULLABLE");
                String colType = buildActualType(resultSet);
                String srId = null;
                switch (colType) {
                    case "geometry":
                        srId = getGeometryColumnDefinition(connection, tableName.schema, tableName.table, name);
                        break;
                    case "geography":
                        srId = getGeographyColumnDefinition(connection, tableName.schema, tableName.table, name);
                        break;
                    default:
                }
                if (srId != null) {
                    colType = colType + "(" + srId + ")";
                }
                columns.put(normalizeIdentifier(name),
                            new ColumnMetadata(name,
                                               colType,
                                               nullable == DatabaseMetaData.columnNullable
                                                       ? Boolean.TRUE
                                                       : nullable == DatabaseMetaData.columnNoNulls ? Boolean.FALSE : null,
                                               resultSet.getString("COLUMN_DEF")));
            }
        }
        return columns;
    }

    private void execute(Connection connection, String sql, String successMessage) throws SQLException {
        logger.fine(sql);
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
        logger.info(successMessage);
    }

    private String getGeographyColumnDefinition(Connection connection, String schema, String table, String column) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT srid, type FROM geography_columns " +
                                                                       "WHERE f_table_schema=? AND f_table_name=? AND f_geography_column=?")) {
            statement.setString(1, schema);
            statement.setString(2, table);
            statement.setString(3, column);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("type").toUpperCase() + "," +  resultSet.getInt("srid");
                }
            }
        }
        return null;
    }

    private String getGeometryColumnDefinition(Connection connection, String schema, String table, String column) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT srid, type FROM geometry_columns " +
                                                                       "WHERE f_table_schema=? AND f_table_name=? AND f_geometry_column=?")) {
            statement.setString(1, schema);
            statement.setString(2, table);
            statement.setString(3, column);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("type").toUpperCase() + "," +  resultSet.getInt("srid");
                }
            }
        }
        return null;
    }

    private void logColumnWarning(TableDefinition table, ColumnDefinition column, String property, SQLException e) {
        logger.warning("Cannot change " + property + " for " + table.name.displayName + "."
                       + column.name + ": " + e.getMessage());
    }

    private Map<String, ColumnDefinition> parseColumns(String createTableStatement) {
        final Map<String, ColumnDefinition> columns = new LinkedHashMap<>();
        final int openParenthesis = createTableStatement.indexOf('(');
        final int closeParenthesis = findMatchingParenthesis(createTableStatement, openParenthesis);
        if (openParenthesis < 0 || closeParenthesis < 0) {
            return columns;
        }
        final String body = createTableStatement.substring(openParenthesis + 1, closeParenthesis);
        for (String part : splitTopLevel(body, ',')) {
            final String definition = part.trim();
            if (definition.isBlank() || isTableConstraint(definition)) {
                continue;
            }
            final String columnName = readIdentifier(definition, 0);
            if (columnName == null) {
                continue;
            }
            final int definitionStart = skipWhitespace(definition, columnName.length());
            final String remainder = definition.substring(definitionStart).trim();
            if (remainder.isBlank()) {
                continue;
            }
            final String type = extractType(remainder);
            final Boolean nullable = parseNullability(remainder);
            final String defaultValue = parseDefaultValue(remainder);
            final String normalizedName = normalizeIdentifier(unquote(columnName));
            columns.put(normalizedName,
                        new ColumnDefinition(unquote(columnName), columnName, normalizedName, remainder, type,
                                             nullable, defaultValue));
        }
        return columns;
    }

    private boolean isStatementForTable(String statement, QualifiedName tableName) {
        final Matcher alterTableMatcher = ALTER_TABLE_PATTERN.matcher(statement);
        if (alterTableMatcher.matches()) {
            return tableName.equals(QualifiedName.parse(alterTableMatcher.group(1)));
        }
        final Matcher ownedMatcher = ALTER_SEQUENCE_OWNED_PATTERN.matcher(statement);
        if (ownedMatcher.matches()) {
            return tableName.equals(QualifiedName.parse(ownedMatcher.group(1)));
        }
        if (CREATE_SEQUENCE_PATTERN.matcher(statement).matches()) {
            return true;
        }
        return false;
    }

    private String normalizeFollowUpStatement(String statement) {
        final Matcher createSequenceMatcher = CREATE_SEQUENCE_PATTERN.matcher(statement);
        if (createSequenceMatcher.matches()) {
            return "CREATE SEQUENCE IF NOT EXISTS " + createSequenceMatcher.group(1).trim();
        }
        return statement;
    }

    private String ensureCreateTableIfNotExists(String statement) {
        if (statement.toLowerCase().matches("(?is)^\\s*create\\s+table\\s+if\\s+not\\s+exists\\b.*")) {
            return statement;
        }
        return statement.replaceFirst("(?is)^\\s*CREATE\\s+TABLE\\s+", "CREATE TABLE IF NOT EXISTS ");
    }

    private boolean isDropStatement(String statement) {
        return statement.trim().toLowerCase().startsWith("drop ");
    }

    private boolean isTableConstraint(String definition) {
        final String lower = definition.toLowerCase();
        return lower.startsWith("constraint ")
                || lower.startsWith("primary ")
                || lower.startsWith("foreign ")
                || lower.startsWith("unique ")
                || lower.startsWith("check ");
    }

    private String extractType(String definition) {
        final List<String> tokens = splitWhitespaceRespectingParentheses(definition);
        final StringBuilder type = new StringBuilder();
        for (String token : tokens) {
            final String normalized = token.toLowerCase(Locale.ROOT);
            if (COLUMN_TERMINATORS.contains(normalized)) {
                break;
            }
            if (!type.isEmpty()) {
                type.append(' ');
            }
            type.append(token);
        }
        return type.toString();
    }

    private Boolean parseNullability(String definition) {
        final String lower = definition.toLowerCase(Locale.ROOT);
        if (lower.matches("(?is).*\\bnot\\s+null\\b.*")) {
            return Boolean.FALSE;
        }
        if (lower.matches("(?is).*\\bnull\\b.*")) {
            return Boolean.TRUE;
        }
        return Boolean.TRUE;
    }

    private String parseDefaultValue(String definition) {
        final int defaultIndex = findKeyword(definition, "default");
        if (defaultIndex < 0) {
            return null;
        }
        int start = skipWhitespace(definition, defaultIndex + "default".length());
        final String remainder = definition.substring(start).trim();
        final List<String> tokens = splitWhitespaceRespectingParentheses(remainder);
        final StringBuilder value = new StringBuilder();
        for (String token : tokens) {
            if (COLUMN_TERMINATORS.stream()
                    .filter(terminator -> !"default".equals(terminator))
                    .anyMatch(terminator -> terminator.equals(token.toLowerCase(Locale.ROOT)))) {
                break;
            }
            if (!value.isEmpty()) {
                value.append(' ');
            }
            value.append(token);
        }
        return value.isEmpty() ? null : value.toString();
    }

    static String normalizeType(String type) {
        if (type == null) {
            return "";
        }
        String normalized = type.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        normalized = normalized.replace("character varying", "varchar");
        normalized = normalized.replace("character", "char");
        normalized = normalized.replace("bpchar", "char");
        normalized = normalized.replace("timestamp without time zone", "timestamp");
        normalized = normalized.replaceAll("\\s*,\\s*", ",");
        final int parametersIndex = normalized.indexOf('(');
        final String baseType = parametersIndex >= 0 ? normalized.substring(0, parametersIndex) : normalized;
        final String parameters = parametersIndex >= 0 ? normalized.substring(parametersIndex) : "";
        switch (baseType) {
            case "smallserial":
            case "serial2":
            case "int2":
                return "smallint" + parameters;
            case "serial":
            case "serial4":
            case "int":
            case "int4":
                return "integer" + parameters;
            case "bigserial":
            case "serial8":
            case "int8":
                return "bigint" + parameters;
            case "bool":
                return "boolean" + parameters;
            case "float4":
                return "real" + parameters;
            case "float8":
                return "double precision" + parameters;
            case "decimal":
                return "numeric" + parameters;
            default:
                return normalized;
        }
    }

    private String alterType(String type) {
        final String normalized = normalizeType(type);
        if ("bigint".equals(normalized) && "bigserial".equals(type.trim().toLowerCase(Locale.ROOT))) {
            return "bigint";
        }
        if ("integer".equals(normalized) && "serial".equals(type.trim().toLowerCase(Locale.ROOT))) {
            return "integer";
        }
        return type;
    }

    private boolean isSerialType(String type) {
        if (type == null) {
            return false;
        }
        final String normalized = type.trim().toLowerCase(Locale.ROOT);
        return "serial".equals(normalized) || "bigserial".equals(normalized);
    }

    private String buildActualType(ResultSet resultSet) throws SQLException {
        final String typeName = resultSet.getString("TYPE_NAME");
        if (typeName == null) {
            return "";
        }
        final String normalizedType = typeName.toLowerCase(Locale.ROOT);
        final int size = resultSet.getInt("COLUMN_SIZE");
        final int decimals = resultSet.getInt("DECIMAL_DIGITS");
        if (("varchar".equals(normalizedType) || "character varying".equals(normalizedType)
                || "bpchar".equals(normalizedType) || "char".equals(normalizedType)
                || "character".equals(normalizedType)) && size > 0 && size < Integer.MAX_VALUE) {
            return typeName + "(" + size + ")";
        }
        if (("numeric".equals(normalizedType) || "decimal".equals(normalizedType)) && size > 0) {
            return decimals > 0 ? typeName + "(" + size + "," + decimals + ")" : typeName + "(" + size + ")";
        }
        return typeName;
    }

    private String normalizeDefault(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.endsWith("::character varying")) {
            normalized = normalized.substring(0, normalized.length() - "::character varying".length());
        }
        if (normalized.endsWith("::text")) {
            normalized = normalized.substring(0, normalized.length() - "::text".length());
        }
        return normalized.replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private String removeLineComments(String sql) {
        final StringBuilder result = new StringBuilder(sql.length());
        boolean singleQuoted = false;
        boolean doubleQuoted = false;
        for (int i = 0; i < sql.length(); i++) {
            final char current = sql.charAt(i);
            final char next = i + 1 < sql.length() ? sql.charAt(i + 1) : '\0';
            if (!doubleQuoted && current == '\'' && (i == 0 || sql.charAt(i - 1) != '\\')) {
                singleQuoted = !singleQuoted;
            } else if (!singleQuoted && current == '"') {
                doubleQuoted = !doubleQuoted;
            }
            if (!singleQuoted && !doubleQuoted && current == '-' && next == '-') {
                while (i < sql.length() && sql.charAt(i) != '\n') {
                    i++;
                }
                result.append('\n');
                continue;
            }
            result.append(current);
        }
        return result.toString();
    }

    private List<String> splitStatements(String sql) {
        return splitTopLevel(sql, ';').stream()
                .map(String::trim)
                .filter(statement -> !statement.isBlank())
                .toList();
    }

    private List<String> splitTopLevel(String text, char delimiter) {
        final List<String> parts = new ArrayList<>();
        final StringBuilder current = new StringBuilder();
        int depth = 0;
        boolean singleQuoted = false;
        boolean doubleQuoted = false;
        for (int i = 0; i < text.length(); i++) {
            final char character = text.charAt(i);
            if (!doubleQuoted && character == '\'' && (i == 0 || text.charAt(i - 1) != '\\')) {
                singleQuoted = !singleQuoted;
            } else if (!singleQuoted && character == '"') {
                doubleQuoted = !doubleQuoted;
            } else if (!singleQuoted && !doubleQuoted) {
                if (character == '(') {
                    depth++;
                } else if (character == ')' && depth > 0) {
                    depth--;
                }
            }
            if (!singleQuoted && !doubleQuoted && depth == 0 && character == delimiter) {
                parts.add(current.toString());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }
        if (!current.isEmpty()) {
            parts.add(current.toString());
        }
        return parts;
    }

    private List<String> splitWhitespaceRespectingParentheses(String text) {
        final List<String> tokens = new ArrayList<>();
        final StringBuilder current = new StringBuilder();
        int depth = 0;
        boolean singleQuoted = false;
        boolean doubleQuoted = false;
        for (int i = 0; i < text.length(); i++) {
            final char character = text.charAt(i);
            if (!doubleQuoted && character == '\'' && (i == 0 || text.charAt(i - 1) != '\\')) {
                singleQuoted = !singleQuoted;
            } else if (!singleQuoted && character == '"') {
                doubleQuoted = !doubleQuoted;
            } else if (!singleQuoted && !doubleQuoted) {
                if (character == '(') {
                    depth++;
                } else if (character == ')' && depth > 0) {
                    depth--;
                }
            }
            if (!singleQuoted && !doubleQuoted && depth == 0 && Character.isWhitespace(character)) {
                if (!current.isEmpty()) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(character);
            }
        }
        if (!current.isEmpty()) {
            tokens.add(current.toString());
        }
        return tokens;
    }

    private int findMatchingParenthesis(String text, int openParenthesis) {
        if (openParenthesis < 0) {
            return -1;
        }
        int depth = 0;
        boolean singleQuoted = false;
        boolean doubleQuoted = false;
        for (int i = openParenthesis; i < text.length(); i++) {
            final char character = text.charAt(i);
            if (!doubleQuoted && character == '\'' && (i == 0 || text.charAt(i - 1) != '\\')) {
                singleQuoted = !singleQuoted;
            } else if (!singleQuoted && character == '"') {
                doubleQuoted = !doubleQuoted;
            } else if (!singleQuoted && !doubleQuoted) {
                if (character == '(') {
                    depth++;
                } else if (character == ')') {
                    depth--;
                    if (depth == 0) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    private int findKeyword(String text, String keyword) {
        final Pattern pattern = Pattern.compile("(?is)\\b" + Pattern.quote(keyword) + "\\b");
        final Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.start() : -1;
    }

    private String readIdentifier(String text, int start) {
        int index = skipWhitespace(text, start);
        if (index >= text.length()) {
            return null;
        }
        if (text.charAt(index) == '"') {
            final int end = text.indexOf('"', index + 1);
            return end > index ? text.substring(index, end + 1) : null;
        }
        final int begin = index;
        while (index < text.length()) {
            final char character = text.charAt(index);
            if (Character.isWhitespace(character) || character == ',' || character == '(') {
                break;
            }
            index++;
        }
        return text.substring(begin, index);
    }

    private int skipWhitespace(String text, int start) {
        int index = start;
        while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
            index++;
        }
        return index;
    }

    private String unquote(String identifier) {
        final String trimmed = identifier.trim();
        if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return trimmed.substring(1, trimmed.length() - 1).replace("\"\"", "\"");
        }
        return trimmed;
    }

    private String normalizeIdentifier(String identifier) {
        return unquote(identifier).toLowerCase(Locale.ROOT);
    }

    private static final class TableDefinition {
        private final QualifiedName name;
        private final String createStatement;
        private final String source;
        private final Map<String, ColumnDefinition> columns = new LinkedHashMap<>();
        private final List<String> followUpStatements = new ArrayList<>();

        private TableDefinition(QualifiedName name, String createStatement, String source) {
            this.name = name;
            this.createStatement = createStatement;
            this.source = source;
        }
    }

    private static final class QualifiedName {
        private final String schema;
        private final String table;
        private final String sqlName;
        private final String displayName;

        private QualifiedName(String schema, String table, String sqlName) {
            this.schema = schema;
            this.table = table;
            this.sqlName = sqlName;
            this.displayName = schema + "." + table;
        }

        private static QualifiedName parse(String value) {
            final List<String> parts = splitQualifiedName(value.trim());
            final String schema = parts.size() > 1 ? normalize(parts.get(parts.size() - 2)) : "public";
            final String table = normalize(parts.get(parts.size() - 1));
            return new QualifiedName(schema, table, String.join(".", parts));
        }

        private static List<String> splitQualifiedName(String value) {
            final List<String> parts = new ArrayList<>();
            final StringBuilder current = new StringBuilder();
            boolean doubleQuoted = false;
            for (int i = 0; i < value.length(); i++) {
                final char character = value.charAt(i);
                if (character == '"') {
                    doubleQuoted = !doubleQuoted;
                    current.append(character);
                } else if (!doubleQuoted && character == '.') {
                    parts.add(current.toString().trim());
                    current.setLength(0);
                } else if (!doubleQuoted && Character.isWhitespace(character)) {
                    break;
                } else {
                    current.append(character);
                }
            }
            if (!current.isEmpty()) {
                parts.add(current.toString().trim());
            }
            return parts;
        }

        private static String normalize(String identifier) {
            final String trimmed = identifier.trim();
            if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
                return trimmed.substring(1, trimmed.length() - 1).replace("\"\"", "\"");
            }
            return trimmed.toLowerCase(Locale.ROOT);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof QualifiedName that)) {
                return false;
            }
            return Objects.equals(schema, that.schema) && Objects.equals(table, that.table);
        }

        @Override
        public int hashCode() {
            return Objects.hash(schema, table);
        }
    }

    private record ColumnDefinition(String name, String sqlName, String normalizedName, String sqlDefinition,
                                    String type, Boolean nullable, String defaultValue) {
        @Override
        public String toString() {
            return "name=" + name + ", sqlName=" + sqlName + ", normalizedName=" + normalizedName + ", sqlDefinition=" + sqlDefinition + ", type=" + type + ", nullable=" + nullable + ", defaultValue=" + defaultValue;
        }
    }

    private record ColumnMetadata(String name, String type, Boolean nullable, String defaultValue) {
    }
}
