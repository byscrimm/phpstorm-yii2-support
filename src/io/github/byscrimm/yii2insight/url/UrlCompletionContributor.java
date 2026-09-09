package io.github.byscrimm.yii2insight.url;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.*;
import io.github.byscrimm.yii2insight.common.*;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public final class UrlCompletionContributor extends CompletionContributor {
    public UrlCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement().withParent(StringLiteralExpression.class), new CompletionProvider<>() {
            @Override protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                StringLiteralExpression literal = (StringLiteralExpression) parameters.getPosition().getParent();
                if (UrlReferenceContributor.isRoute(literal)) {
                    // Rooted routes remain correct when inserted from nested module controllers or views.
                    for (Map.Entry<String,Method> route : UrlUtils.getRoutes(literal).entrySet()) {
                        String name = "/" + route.getKey();
                        Method method = route.getValue();
                        LookupElementBuilder lookup = LookupElementBuilder.create(method, name).withLookupString(route.getKey());
                        if (method.getContainingClass() != null) lookup = lookup.withTypeText(method.getContainingClass().getFQN(),true);
                        result.addElement(lookup);
                    }
                    return;
                }
                ArrayHashElement hash = PhpArrays.keyEntry(literal);
                if (hash == null) return;
                ArrayCreationExpression array = PsiTreeUtil.getParentOfType(hash, ArrayCreationExpression.class);
                if (array == null) return;
                PsiElement first = array.getFirstPsiChild();
                StringLiteralExpression route = first instanceof StringLiteralExpression direct ? direct
                    : first == null ? null : PsiTreeUtil.findChildOfType(first, StringLiteralExpression.class);
                if (route == null || !UrlReferenceContributor.isRoute(route)) return;
                Parameter[] actionParams = UrlUtils.getParamsByUrl(route.getContents(), route);
                if (actionParams == null) return;
                Set<String> used = new HashSet<>();
                for (ArrayHashElement entry : array.getHashElements()) if (entry != hash) used.add(PhpArrays.string(entry.getKey()));
                for (Parameter parameter : actionParams) {
                    if (!used.contains(parameter.getName())) result.addElement(LookupElementBuilder.create(parameter, parameter.getName())
                        .withTypeText(parameter.getType().toString(),true));
                }
            }
        });
    }
}
