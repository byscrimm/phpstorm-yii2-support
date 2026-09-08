package io.github.byscrimm.yii2insight.relations;

import java.util.*;
import java.util.regex.Pattern;

/** Exact source offsets allow renaming one relation without touching neighbours or JOIN aliases. */
public final class RelationPath {
    public record Segment(String name, int start, int end) {}
    private static final Pattern PATH = Pattern.compile("[a-zA-Z_][a-zA-Z_0-9]*(?:\\.[a-zA-Z_][a-zA-Z_0-9]*)*");
    private static final Pattern ALIAS = Pattern.compile("\\s+(?:(?i:as)\\s+)?[a-zA-Z_][a-zA-Z_0-9]*");
    private RelationPath() {}
    public static List<Segment> segments(String value, boolean allowAlias) {
        if (value.length() > 1024 || value.chars().filter(c -> c == '.').count() >= 32) return List.of();
        var matcher = PATH.matcher(value);
        if (!matcher.lookingAt()) return List.of();
        int end = matcher.end();
        if (end < value.length() && !(allowAlias && ALIAS.matcher(value.substring(end)).matches())) return List.of();
        List<Segment> result = new ArrayList<>();
        int start = 0;
        for (String part : value.substring(0, end).split("\\.")) {
            result.add(new Segment(part, start, start + part.length())); start += part.length() + 1;
        }
        return List.copyOf(result);
    }
    public static String property(String getter) {
        if (getter == null || !getter.startsWith("get") || getter.length() == 3) return null;
        return Character.toLowerCase(getter.charAt(3)) + getter.substring(4);
    }
    public static String rename(String value, Segment segment, String getter) {
        String property = property(getter);
        if (property == null || !PATH.matcher(property).matches() || property.contains(".")) return value;
        return value.substring(0,segment.start) + property + value.substring(segment.end);
    }
}
