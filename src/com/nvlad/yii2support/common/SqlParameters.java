package com.nvlad.yii2support.common;

import java.util.LinkedHashSet;
import java.util.Set;

/** Named SQL placeholders, excluding literals, comments and PostgreSQL casts. */
public final class SqlParameters {
    private SqlParameters() {}

    public static Set<String> names(String sql) {
        Set<String> result = new LinkedHashSet<>();
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (c == '\'' || c == '"' || c == '`') {
                char quote = c;
                while (++i < sql.length()) {
                    if (sql.charAt(i) == '\\') { i++; continue; }
                    if (sql.charAt(i) == quote) {
                        if (i + 1 < sql.length() && sql.charAt(i + 1) == quote) { i++; continue; }
                        break;
                    }
                }
            } else if (c == '-' && i + 1 < sql.length() && sql.charAt(i + 1) == '-') {
                while (i < sql.length() && sql.charAt(i) != '\n') i++;
            } else if (c == '/' && i + 1 < sql.length() && sql.charAt(i + 1) == '*') {
                int end = sql.indexOf("*/", i + 2);
                i = end < 0 ? sql.length() : end + 1;
            } else if (c == '$') {
                int tagEnd = sql.indexOf('$', i + 1);
                if (tagEnd >= 0 && sql.substring(i + 1, tagEnd).matches("[A-Za-z_][A-Za-z_0-9]*|")) {
                    String tag = sql.substring(i, tagEnd + 1);
                    int end = sql.indexOf(tag, tagEnd + 1);
                    if (end >= 0) i = end + tag.length() - 1;
                }
            } else if (c == ':' && (i == 0 || sql.charAt(i - 1) != ':')
                    && i + 1 < sql.length() && (Character.isLetter(sql.charAt(i + 1)) || sql.charAt(i + 1) == '_')) {
                int start = ++i;
                while (i + 1 < sql.length() && (Character.isLetterOrDigit(sql.charAt(i + 1)) || sql.charAt(i + 1) == '_')) i++;
                result.add(sql.substring(start, i + 1));
            }
        }
        return result;
    }

    public static String normalize(String name) {
        return name.startsWith(":") ? name.substring(1) : name;
    }
}
