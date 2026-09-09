package io.github.byscrimm.yii2insight.objectfactory;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceRegistrar;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import io.github.byscrimm.yii2insight.common.Patterns;

import org.jetbrains.annotations.NotNull;

public class ObjectFactoryReferenceContributor extends com.intellij.psi.PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        psiReferenceRegistrar.registerReferenceProvider(ElementPattern(), new ObjectFactoryReferenceProvider());
    }

    private static ElementPattern<? extends PsiElement> ElementPattern() {
        return PlatformPatterns.psiElement(com.jetbrains.php.lang.psi.elements.StringLiteralExpression.class);
    }
}
