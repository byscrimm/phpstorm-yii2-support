package io.github.byscrimm.yii2insight.views.references;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceRegistrar;
import io.github.byscrimm.yii2insight.common.Patterns;
import io.github.byscrimm.yii2insight.views.util.ViewUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Created by NVlad on 02.01.2017.
 */
public class PsiReferenceContributor extends com.intellij.psi.PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        psiReferenceRegistrar.registerReferenceProvider(ElementPattern(), new PsiReferenceProvider());
    }

    private static ElementPattern<PsiElement> ElementPattern() {
        return PlatformPatterns.psiElement(PsiElement.class)
                .withSuperParent(2, Patterns.methodWithName(ViewUtil.renderMethods));
    }
}
