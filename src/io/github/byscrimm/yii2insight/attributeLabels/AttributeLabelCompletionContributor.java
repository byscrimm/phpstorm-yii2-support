package io.github.byscrimm.yii2insight.attributeLabels;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import org.jetbrains.annotations.NotNull;

public class AttributeLabelCompletionContributor extends com.intellij.codeInsight.completion.CompletionContributor{
    public AttributeLabelCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), new AttributeLabelCompletionProvider());
    }

    @Override
    public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
        if ((typeChar == '\'' || typeChar == '"') && position.getParent() instanceof ArrayCreationExpression) {
            return true;
        }

        return false;
    }
}
