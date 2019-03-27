package ro.cs.tao.eodata.naming;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NameExpressionParser {
    private final NamingRule rule;
    private final Pattern rulePattern;
    private final Pattern tokenPattern = Pattern.compile("(\\$\\{\\d+\\:[^}]+\\})");

    public NameExpressionParser(NamingRule rule) {
        this.rule = rule;
        this.rulePattern = Pattern.compile(this.rule.getRegEx());
    }

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
        if (invalidTokens.size() > 0) {
            throw new ParseException(String.format("Invalid tokens: %s", String.join(",", invalidTokens)));
        }
    }

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

        return transformed;
    }

    public class Token {
        final String name;

        public Token(String name) {
            this.name = name;
        }
    }
}
