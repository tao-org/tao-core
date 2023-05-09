package ro.cs.tao.eodata.naming;

import ro.cs.tao.utils.StringUtilities;

import java.util.function.Function;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DateFunctions {
    private static final Pattern yearPattern = Pattern.compile("(YEAR\\(([0-9-]+)\\))");
    private static final Pattern monthPattern = Pattern.compile("(MONTH\\(([0-9-]+)\\))");
    private static final Pattern dayPattern = Pattern.compile("(DAY\\(([0-9-]+)\\))");
    private static final Pattern hourPattern = Pattern.compile("(HOUR\\(([0-9:]+)\\))");
    private static final Pattern minutePattern = Pattern.compile("(MINUTE\\(([0-9:]+)\\))");
    private static final Pattern secondPattern = Pattern.compile("(SECOND\\(([0-9:]+)\\))");
    public static final Function<String, String> YEAR = (str) -> str.substring(0, 4);
    public static final Function<String, String> MONTH = (str) -> {
        int idx = str.indexOf('-');
        return StringUtilities.padLeft(idx > 0 ? str.substring(idx + 1, str.indexOf('-', idx + 1)) : str.substring(4, 6),
                                       2, "0");
    };
    public static final Function<String, String> DAY = (str) -> {
        int idx = str.lastIndexOf('-');
        return StringUtilities.padLeft(idx > 0 ? str.substring(str.lastIndexOf('-', idx - 1) + 1, idx) : str.substring(6, 8),
                                       2, "0");
    };
    public static final Function<String, String> HOUR = (str) -> str.substring(0, 2);
    public static final Function<String, String> MINUTE = (str) -> {
        int idx = str.indexOf(':');
        return StringUtilities.padLeft(idx > 0 ? str.substring(idx + 1, str.indexOf(':', idx + 1)) : str.substring(2, 4),
                                       2, "0");
    };
    public static final Function<String, String> SECOND = (str) -> {
        int idx = str.lastIndexOf(':');
        return StringUtilities.padLeft(idx > 0 ? str.substring(idx + 1, str.indexOf(':', idx + 1)) : str.substring(4, 6),
                                       2, "0");
    };

    public static String resolve(final String expression) {
        if (StringUtilities.isNullOrEmpty(expression)) {
            return expression;
        }
        String transformed = expression;
        try {
            Matcher matcher = yearPattern.matcher(expression);
            if (matcher.find()) {
                transformed = transformed.replace(matcher.group(1), YEAR.apply(matcher.group(2)));
            }
            matcher = monthPattern.matcher(expression);
            if (matcher.find()) {
                transformed = transformed.replace(matcher.group(1), MONTH.apply(matcher.group(2)));
            }
            matcher = dayPattern.matcher(expression);
            if (matcher.find()) {
                transformed = transformed.replace(matcher.group(1), DAY.apply(matcher.group(2)));
            }
            matcher = hourPattern.matcher(expression);
            if (matcher.find()) {
                transformed = transformed.replace(matcher.group(1), HOUR.apply(matcher.group(2)));
            }
            matcher = minutePattern.matcher(expression);
            if (matcher.find()) {
                transformed = transformed.replace(matcher.group(1), MINUTE.apply(matcher.group(2)));
            }
            matcher = secondPattern.matcher(expression);
            if (matcher.find()) {
                transformed = transformed.replace(matcher.group(1), SECOND.apply(matcher.group(2)));
            }
        } catch (Throwable t) {
            Logger.getLogger(DateFunctions.class.getName()).warning(t.getMessage());
        }
        return transformed;
    }

}
