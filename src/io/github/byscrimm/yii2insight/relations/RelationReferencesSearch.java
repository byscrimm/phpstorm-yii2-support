package io.github.byscrimm.yii2insight.relations;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.search.RequestResultProcessor;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocProperty;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.Method;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.search.PsiSearchScopeUtil;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/** Find the relation spelling, while retaining model-aware reference matching. */
public final class RelationReferencesSearch extends QueryExecutorBase<PsiReference,ReferencesSearch.SearchParameters> {
    public RelationReferencesSearch() { super(true); }
    @Override public void processQuery(@NotNull ReferencesSearch.SearchParameters parameters, @NotNull Processor<? super PsiReference> consumer) {
        PsiElement target = parameters.getElementToSearch();
        if (DumbService.isDumb(target.getProject())) return;
        var resolver = new YiiModelResolver(target.getProject());
        var getter = RelationSymbols.getter(target, resolver);
        if (getter == null) return;
        if (target instanceof Method) for (var reference : documentationReferences(getter)) {
            ProgressManager.checkCanceled();
            if (PsiSearchScopeUtil.isInScope(parameters.getEffectiveSearchScope(), reference.getElement()) && !consumer.process(reference)) return;
        }
        String property = RelationPath.property(getter.getName());
        parameters.getOptimizer().searchWord(property, parameters.getEffectiveSearchScope(), UsageSearchContext.IN_STRINGS, true, target,
            new RequestResultProcessor(target) {
                @Override public boolean processTextOccurrence(@NotNull PsiElement element, int offset, @NotNull Processor<? super PsiReference> processor) {
                    ProgressManager.checkCanceled();
                    if (DumbService.isDumb(element.getProject())) return true;
                    var literal = PsiTreeUtil.getParentOfType(element, StringLiteralExpression.class, false);
                    if (literal == null) return true;
                    int inLiteral = element.getTextRange().getStartOffset() + offset - literal.getTextRange().getStartOffset();
                    for (var reference : RelationReferenceContributor.references(literal, new YiiModelResolver(element.getProject()))) {
                        if (!reference.getRangeInElement().containsOffset(inLiteral) || !reference.isReferenceTo(target)) continue;
                        PsiReference result = target instanceof PhpDocProperty ? propertyReference(reference, target) : reference;
                        if (!processor.process(result)) return false;
                    }
                    return true;
                }
            });
    }
    public static List<PsiReference> documentationReferences(Method getter) {
        var pointer = SmartPointerManager.getInstance(getter.getProject()).createSmartPsiElementPointer(getter);
        return RelationSymbols.properties(getter).stream().map(property -> {
            PsiElement identifier = property.getNameIdentifier();
            TextRange range = identifier == null ? TextRange.from(0, property.getTextLength())
                    : identifier.getTextRange().shiftLeft(property.getTextRange().getStartOffset());
            return (PsiReference) new PsiReferenceBase<PhpDocProperty>(property, range, true) {
                @Override public PsiElement resolve() { return pointer.getElement(); }
                // The paired declaration rename owns this write, avoiding a second rename to getArea.
                @Override public PsiElement handleElementRename(@NotNull String name) { return getElement(); }
            };
        }).toList();
    }
    public static PsiReference propertyReference(PsiReference reference, PsiElement property) {
        return new PsiReferenceBase<PsiElement>(reference.getElement(), reference.getRangeInElement(), true) {
            @Override public PsiElement resolve() { return property; }
            @Override public PsiElement handleElementRename(@NotNull String name) {
                return RelationReferenceContributor.renameProperty(reference, name);
            }
        };
    }
}
