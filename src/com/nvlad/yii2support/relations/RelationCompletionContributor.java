package com.nvlad.yii2support.relations;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.ElementManipulators;
import com.intellij.openapi.project.DumbService;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;
import java.util.HashSet;

public final class RelationCompletionContributor extends CompletionContributor {
    public RelationCompletionContributor() {
        extend(CompletionType.BASIC,PlatformPatterns.psiElement().withParent(StringLiteralExpression.class),new CompletionProvider<>() {
            @Override protected void addCompletions(@NotNull CompletionParameters parameters,@NotNull ProcessingContext processing,@NotNull CompletionResultSet result) {
                if (DumbService.isDumb(parameters.getPosition().getProject())) return;
                StringLiteralExpression literal = (StringLiteralExpression) parameters.getPosition().getParent();
                RelationContext context = RelationContext.of(literal);
                if (context == null) return;
                int start = literal.getTextRange().getStartOffset() + literal.getValueRange().getStartOffset();
                int length = Math.max(0,Math.min(literal.getContents().length(),parameters.getOffset()-start));
                String typed = literal.getContents().substring(0,length);
                if (!typed.matches("[a-zA-Z_0-9.]*")) return;
                int dot = typed.lastIndexOf('.');
                String parent = dot < 0 ? "" : typed.substring(0,dot);
                String prefix = typed.substring(dot+1);
                if (!parent.isEmpty() && RelationPath.segments(parent,false).isEmpty()) return;
                result = result.withPrefixMatcher(prefix);
                YiiModelResolver resolver = new YiiModelResolver(literal.getProject());
                var models = resolver.models(context.modelExpression());
                HashSet<String> added = new HashSet<>();
                for (var relation : resolver.atPath(models,parent)) {
                    if (!added.add(relation.name())) continue;
                    String target = String.join("|",relation.targets().stream().map(c -> c.getName()).toList());
                    result.addElement(LookupElementBuilder.create(relation.getter(),relation.name())
                            .withTypeText(target,true).withTailText("  " + relation.getter().getName() + "()",true));
                }
            }
        });
    }
}
