package com.nvlad.yii2support.configurations;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static com.nvlad.yii2support.common.PsiUtil.getArrayCreationChild;

public class ComponentsIndex extends FileBasedIndexExtension<String, String> {
    public static final ID<String, String> identity = ID.create("Yii2Support.ComponentsIndex");

    @Override
    public @NotNull ID<String, String> getName() {
        return identity;
    }

    @Override
    public @NotNull DataIndexer<String, String, FileContent> getIndexer() {
        return file -> {
            final HashMap<String, String> result = new HashMap<>();

            PsiFile psiFile = file.getPsiFile();
            for (ArrayHashElement hash : com.intellij.psi.util.PsiTreeUtil.findChildrenOfType(psiFile, ArrayHashElement.class)) {
                if (!"components".equals(com.nvlad.yii2support.common.PhpArrays.string(hash.getKey()))
                        || !(hash.getValue() instanceof ArrayCreationExpression components)) continue;
                for (ArrayHashElement component : components.getHashElements()) {
                    String name = com.nvlad.yii2support.common.PhpArrays.string(component.getKey());
                    if (name == null) continue;
                    String className = com.nvlad.yii2support.common.PhpArrays.className(component.getValue());
                    if (className == null && component.getValue() instanceof Function factory) {
                        for (PhpReturn ret : com.intellij.psi.util.PsiTreeUtil.findChildrenOfType(factory, PhpReturn.class)) {
                            className = com.nvlad.yii2support.common.PhpArrays.className(ret.getFirstPsiChild());
                            if (className != null) break;
                        }
                    }
                    if (className != null && !className.isBlank()) result.put(name, className);
                }
            }
            return result;
        };
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public @NotNull DataExternalizer<String> getValueExternalizer() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public int getVersion() {
        return 7;
    }

    @Override
    public @NotNull FileBasedIndex.InputFilter getInputFilter() {
        return file -> (
                file.getFileType() == PhpFileType.INSTANCE
                && file.getUrl().contains("/config/")
                && !file.getUrl().contains("/environments/")
        );
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }
}
