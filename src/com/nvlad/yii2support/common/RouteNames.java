package com.nvlad.yii2support.common;

import java.util.ArrayDeque;

public final class RouteNames {
    private RouteNames() {}
    public static String id(String className) {
        return className.replaceAll("([A-Z]+)([A-Z][a-z])", "$1-$2")
                .replaceAll("([a-z0-9])([A-Z])", "$1-$2").toLowerCase(java.util.Locale.ROOT);
    }
    public static String modulePrefix(String relativeFile) {
        StringBuilder prefix = new StringBuilder();
        String[] parts = relativeFile.split("/");
        for (int i = 0; i + 1 < parts.length; i++) {
            if (parts[i].equals("controllers")) break;
            if (parts[i].equals("modules")) prefix.append(parts[++i]).append('/');
        }
        return prefix.toString();
    }
    public static String resolve(String value, String controller, String module) {
        if (value.startsWith("/") || controller == null) return normalize(value);
        return normalize((value.contains("/") ? module : controller + "/") + value);
    }
    public static String normalize(String route) {
        ArrayDeque<String> parts = new ArrayDeque<>();
        for (String part : route.split("/")) {
            if (part.isEmpty() || part.equals(".")) continue;
            if (part.equals("..")) { if (!parts.isEmpty()) parts.removeLast(); }
            else parts.addLast(part);
        }
        return String.join("/", parts);
    }
}
