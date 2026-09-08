package com.nvlad.yii2support.relations;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public final class RelationReferenceContributor extends PsiReferenceContributor {
    @Override public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(StringLiteralExpression.class),new PsiReferenceProvider() {
            @Override public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element,@NotNull ProcessingContext processing) {
                if (DumbService.isDumb(element.getProject())) return PsiReference.EMPTY_ARRAY;
                StringLiteralExpression literal = (StringLiteralExpression) element;
                return references(literal, new YiiModelResolver(element.getProject()));
            }
        });
    }
    public static PsiReference[] references(StringLiteralExpression literal, YiiModelResolver resolver) {
        RelationContext context = RelationContext.of(literal);
        if (context == null || resolver.models(context.modelExpression()).isEmpty()) return PsiReference.EMPTY_ARRAY;
        List<RelationPath.Segment> segments = RelationPath.segments(literal.getContents(),context.allowAlias());
        List<PsiReference> result = new ArrayList<>();
        for (RelationPath.Segment segment : segments) result.add(new Reference(literal,segment,literal.getValueRange().getStartOffset(),resolver));
        return result.toArray(PsiReference.EMPTY_ARRAY);
    }
    private static final class Reference extends PsiPolyVariantReferenceBase<StringLiteralExpression> {
        private final RelationPath.Segment segment;
        private final YiiModelResolver resolver;
        Reference(StringLiteralExpression literal,RelationPath.Segment segment,int offset,YiiModelResolver resolver) {
            super(literal,new TextRange(offset+segment.start(),offset+segment.end()),true); this.segment=segment; this.resolver=resolver;
        }
        @Override public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
            RelationContext context=RelationContext.of(myElement);
            if (context==null || DumbService.isDumb(myElement.getProject())) return ResolveResult.EMPTY_ARRAY;
            String value=myElement.getContents();
            if (segment.end()>value.length()) return ResolveResult.EMPTY_ARRAY;
            String parent=segment.start()==0?"":value.substring(0,segment.start()-1);
            return resolver.atPath(resolver.models(context.modelExpression()),parent).stream()
                    .filter(r->r.name().equals(segment.name())).map(r->new PsiElementResolveResult(r.getter())).toArray(ResolveResult[]::new);
        }
        @Override public Object @NotNull [] getVariants() { return new Object[0]; }
        @Override public PsiElement handleElementRename(@NotNull String name) {
            String property=RelationPath.property(name);
            if (property==null) return myElement;
            return ElementManipulators.handleContentChange(myElement,getRangeInElement(),property);
        }
    }
}
