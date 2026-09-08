package com.nvlad.yii2support.widgetsconfig;

/** Cursor-aware shorthand parsing: attribute:format:label. Never completes inside the label. */
public record WidgetAttributePosition(boolean format, String parentPath, String prefix) {
    public static WidgetAttributePosition parse(String contents, int offset, boolean shorthand) {
        if (offset < 0 || offset > contents.length()) return null;
        String typed = contents.substring(0, offset);
        int colon = typed.indexOf(':');
        if (colon >= 0) {
            if (!shorthand || typed.indexOf(':', colon + 1) >= 0) return null;
            String prefix = typed.substring(colon + 1);
            return prefix.matches("[a-zA-Z_0-9]*") ? new WidgetAttributePosition(true, "", prefix) : null;
        }
        if (!typed.matches("[a-zA-Z_0-9.]*")) return null;
        int dot = typed.lastIndexOf('.');
        String parent = dot < 0 ? "" : typed.substring(0, dot);
        if (dot == 0 || parent.endsWith(".")) return null;
        return new WidgetAttributePosition(false, parent, typed.substring(dot + 1));
    }
}
