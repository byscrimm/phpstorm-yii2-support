package io.github.byscrimm.yii2insight.widgetsconfig;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PlatformPatterns;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;

public final class WidgetConfigCompletionContributor extends CompletionContributor {
    public WidgetConfigCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement().withParent(StringLiteralExpression.class),
                new WidgetConfigCompletionProvider());
    }
}
