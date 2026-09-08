package com.nvlad.yii2support.common;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Yii aliases use the longest complete path segment; cycles are unresolved. */
public final class AliasResolver {
    private AliasResolver() {}

    public static String resolve(Map<String, String> aliases, String path) {
        if (path == null) return null;
        Set<String> visited = new HashSet<>();
        while (path.startsWith("@")) {
            if (!visited.add(path) || visited.size() > 64) return null;
            String best = null;
            for (String key : aliases.keySet()) {
                if ((path.equals(key) || path.startsWith(key + "/"))
                        && (best == null || key.length() > best.length())) best = key;
            }
            if (best == null || aliases.get(best) == null) return null;
            String base = aliases.get(best);
            path = (base.isEmpty() ? "." : base) + path.substring(best.length());
        }
        return path;
    }
}
