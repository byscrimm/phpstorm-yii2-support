package com.nvlad.yii2support.common;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.nvlad.yii2support.utils.Yii2SupportSettings;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class YiiAlias {
    private final Project project;
    private YiiAlias(Project project) { this.project = project; }
    public static YiiAlias getInstance(Project project) { return new YiiAlias(project); }
    public String getAlias(String alias, boolean console) {
        Map<String, String> aliases = new HashMap<>(Yii2SupportSettings.getInstance(project).aliasMap);
        aliases.putIfAbsent("@app", "");
        if (console) aliases.put("@app", aliases.getOrDefault("@yii2support-console-command-app-root", ""));
        return AliasResolver.resolve(aliases, alias);
    }
    public String resolveAlias(String alias, boolean console) { return getAlias(alias, console); }
    public VirtualFile resolveVirtualFile(String alias, boolean console) {
        String value = resolveAlias(alias, console);
        String root = YiiApplicationUtils.getYiiRootPath(project);
        if (value == null || root == null) return null;
        try { return LocalFileSystem.getInstance().findFileByPath(Path.of(root).resolve(value).normalize().toString()); }
        catch (java.nio.file.InvalidPathException e) { return null; }
    }
}
