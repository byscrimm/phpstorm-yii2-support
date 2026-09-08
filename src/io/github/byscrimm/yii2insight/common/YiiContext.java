package io.github.byscrimm.yii2insight.common;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import java.util.List;

/** Application identity shared by completion, references and type resolution. */
public final class YiiContext {
    private YiiContext() {}

    public static VirtualFile applicationRoot(PsiElement element) {
        PsiFile file = element.getContainingFile();
        return file == null ? null : applicationRoot(FileUtil.getVirtualFile(file), element.getProject().getBasePath());
    }

    public static VirtualFile applicationRoot(VirtualFile file, String projectPath) {
        VirtualFile dir = file == null ? null : file.isDirectory() ? file : file.getParent();
        while (dir != null) {
            boolean module = dir.getParent() != null && "modules".equals(dir.getParent().getName());
            if (dir.getPath().equals(projectPath) || (!module && (dir.findChild("config") != null || dir.findChild("yii") != null))) return dir;
            dir = dir.getParent();
        }
        return null;
    }

    public static boolean sameApplication(PsiElement origin, PsiElement target) {
        VirtualFile a = applicationRoot(origin), b = applicationRoot(target);
        return a == null || b == null || a.equals(b);
    }

    /** Shared config is visible to sibling apps, unrelated app config is not. */
    public static int configPriority(VirtualFile origin, VirtualFile config, String projectPath) {
        VirtualFile app = applicationRoot(origin, projectPath);
        if (app == null) return -1;
        String path = config.getPath();
        String root = app.getPath();
        if (path.startsWith(root + "/config/")) return config.getNameWithoutExtension().endsWith("-local") ? 30 : 20;
        VirtualFile parent = app.getParent();
        if (parent != null && path.startsWith(parent.getPath() + "/common/config/")) return 10;
        return -1;
    }
}
