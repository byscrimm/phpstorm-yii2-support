package io.github.byscrimm.yii2insight.common;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class FileUtil {

    @NotNull
    public static VirtualFile getVirtualFile(PsiFile file) {
        VirtualFile result = file.getVirtualFile();
        if (result == null) {
            result = file.getOriginalFile().getVirtualFile();
        }
        return result;
    }
}
