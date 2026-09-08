package com.nvlad.yii2support.widgetsconfig;

import com.intellij.openapi.project.*;
import com.intellij.openapi.util.RecursionManager;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.*;
import com.nvlad.yii2support.common.FileUtil;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Index phase records only local context; SDK indices are consulted only by complete(). */
public final class WidgetCallbackTypeProvider implements PhpTypeProvider4 {
    @Override public char getKey() { return '\u0858'; }
    @Override public PhpType getType(PsiElement element) {
        if (!(element instanceof Parameter parameter) || WidgetContext.callback(parameter) == null) return null;
        PsiFile file = parameter.getContainingFile();
        var virtualFile = file == null ? null : FileUtil.getVirtualFile(file);
        if (virtualFile == null) return null;
        String payload = virtualFile.getUrl() + "\n" + parameter.getTextOffset() + "\n" + parameter.getName();
        return new PhpType().add("#" + getKey() + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8)));
    }
    @Override public PhpType complete(String signature, Project project) {
        if (DumbService.isDumb(project) || !signature.startsWith("#" + getKey())) return null;
        return RecursionManager.doPreventingRecursion(signature, false, () -> {
            String[] data;
            try {
                data = new String(Base64.getUrlDecoder().decode(signature.substring(2)), StandardCharsets.UTF_8).split("\n", -1);
                if (data.length != 3) return null;
                int offset = Integer.parseInt(data[1]);
                var virtualFile = VirtualFileManager.getInstance().findFileByUrl(data[0]);
                PsiFile file = virtualFile == null ? null : PsiManager.getInstance(project).findFile(virtualFile);
                if (file == null || offset < 0 || offset >= file.getTextLength()) return null;
                Parameter parameter = PsiTreeUtil.getParentOfType(file.findElementAt(offset), Parameter.class, false);
                if (parameter == null || parameter.getTextOffset() != offset || !parameter.getName().equals(data[2])) return null;
                return infer(parameter, WidgetConfigCompletionProvider.resolver(parameter));
            } catch (IllegalArgumentException ignored) { return null; }
        });
    }
    public static PhpType infer(Parameter parameter, WidgetModelResolver resolver) {
        WidgetContext context = WidgetContext.callback(parameter);
        if (!resolver.supports(context)) return null;
        List<PhpClass> models = resolver.models(context);
        if (models.isEmpty()) return null;
        // A dynamic flag cannot prove whether the row is an object or an array.
        WidgetModelResolver.RowMode mode = resolver.rowMode(context);
        if (mode == WidgetModelResolver.RowMode.UNKNOWN) return null;
        if (mode == WidgetModelResolver.RowMode.ARRAY) return new PhpType().add("array");
        PhpType result = new PhpType();
        for (PhpClass model : models) result.add(model.getFQN());
        return result;
    }
    @Override public Collection<? extends PhpNamedElement> getBySignature(String signature, Set<String> visited, int depth, Project project) {
        return null;
    }
}
