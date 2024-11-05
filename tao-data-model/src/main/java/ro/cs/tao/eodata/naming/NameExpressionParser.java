package ro.cs.tao.eodata.naming;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for naming rule expression that also allows the replacement of tokens with actual values.
 *
 * @author Cosmin Cara
 */
public class NameExpressionParser {
    private static Set<TokenResolver> resolvers = new LinkedHashSet<>();
    private final NamingRule rule;
    private final Pattern rulePattern;
    private final Pattern tokenPattern = Pattern.compile("(\\$\\{\\d+\\:[^}]+\\})");

    public static void setResolvers(Set<TokenResolver> resolvers) {
        NameExpressionParser.resolvers.addAll(resolvers);
    }

    public NameExpressionParser(NamingRule rule) {
        this.rule = rule;
        this.rulePattern = Pattern.compile(this.rule.getRegEx());
    }

    /**
     * Parses (validates) the given naming rule expression.
     * @param expression    The expression to be validated
     */
    public void parse(String expression) {
        Matcher tokenMatcher = tokenPattern.matcher(expression);
        List<String> invalidTokens = new ArrayList<>();
        while (tokenMatcher.find()) {
            String group = tokenMatcher.group();
            if (!group.startsWith("$") || !group.contains(":")) {
                invalidTokens.add(group);
            } else {
                final String token = group.substring(group.indexOf(":") + 1, group.length() - 1);
                if (rule.getTokens().stream().noneMatch(t -> token.equals(t.getName()))) {
                    invalidTokens.add(token);
                }
            }
        }
        if (!invalidTokens.isEmpty()) {
            throw new ParseException(String.format("Invalid tokens: %s", String.join(",", invalidTokens)));
        }
    }

    /**
     * Replaces the naming rule expression tokens with actual values.
     */
    public String resolve(String expression, String[] names) throws ParseException {
        parse(expression);
        String transformed = expression;
        Matcher tokenMatch = this.tokenPattern.matcher(expression);
        while (tokenMatch.find()) {
            String group = tokenMatch.group();
            int index = Integer.parseInt(group.substring(2, group.indexOf(":"))) - 1;
            final String token = group.substring(group.indexOf(":") + 1, group.length() - 1);
            String name = names[index];
            Matcher nameMatcher = this.rulePattern.matcher(name);
            if (nameMatcher.matches()) {
                NameToken nameToken = this.rule.getTokens().stream().filter(t -> token.equalsIgnoreCase(t.getName())).findFirst().orElse(null);
                if (nameToken == null) {
                    throw new ParseException(String.format("Token {%s} is not defined", token));
                }
                String value = nameMatcher.group(nameToken.getMatchingGroupNumber());
                transformed = transformed.replace(group, value);
            } else {
                throw new ParseException(String.format("'%s' doesn't follow the expected pattern [%s]",
                                                       name, this.rule.getRegEx()));
            }
        }
        final Iterator<TokenResolver> iterator = resolvers.iterator();
        while (iterator.hasNext()) {
            transformed = iterator.next().resolve(transformed);
        }
        return transformed;
    }

    /**
     * Replaces any custom functions with their result.
     */
    public static String resolve(String expression) throws ParseException {
        String transformed = expression;
        final Iterator<TokenResolver> iterator = resolvers.iterator();
        while (iterator.hasNext()) {
            transformed = iterator.next().resolve(transformed);
        }
        return transformed;
    }
}
