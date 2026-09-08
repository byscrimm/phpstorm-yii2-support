package com.nvlad.yii2support.migrations.commands;

import java.time.Duration;
import java.util.regex.Pattern;

/** Complete Yii migrate status lines, with optional duration and namespace. */
public final class MigrationOutput {
    private static final Pattern LINE = Pattern.compile("^\\s*\\*\\*\\* (applying|applied|reverting|reverted|failed to apply|failed to revert) (\\\\?(?:[\\w]+\\\\)*)([mM]\\d{6}_?\\d{6}_[\\w]+)(?:\\s+\\(time: ([\\d.]+)s\\))?\\s*$");
    public record Event(String status, String namespace, String name, Duration duration) {}
    public static Event parse(String text) {
        var match = LINE.matcher(text);
        if (!match.matches()) return null;
        Duration duration = null;
        try { if (match.group(4) != null) duration = Duration.parse("PT" + match.group(4) + "S"); }
        catch (java.time.format.DateTimeParseException ignored) { }
        String namespace = match.group(2);
        if (!namespace.startsWith("\\")) namespace = "\\" + namespace;
        return new Event(match.group(1), namespace, match.group(3), duration);
    }
    private MigrationOutput() {}
}
