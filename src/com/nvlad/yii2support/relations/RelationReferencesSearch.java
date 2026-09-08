package com.nvlad.yii2support.relations;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.openapi.project.DumbService;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import com.jetbrains.php.lang.psi.elements.Method;
import org.jetbrains.annotations.NotNull;

/** Method searches normally look for getOrders; relation strings contain orders instead. */
public final class RelationReferencesSearch extends QueryExecutorBase<PsiReference,ReferencesSearch.SearchParameters> {
    public RelationReferencesSearch() { super(true); }
    @Override public void processQuery(@NotNull ReferencesSearch.SearchParameters parameters,@NotNull Processor<? super PsiReference> consumer) {
        if (!(parameters.getElementToSearch() instanceof Method method) || method.getContainingClass()==null
                || DumbService.isDumb(method.getProject())) return;
        String property=RelationPath.property(method.getName());
        if (property==null) return;
        var resolver=new YiiModelResolver(method.getProject());
        boolean relation=resolver.relations(method.getContainingClass()).stream().anyMatch(r->r.getter().isEquivalentTo(method));
        if (relation) parameters.getOptimizer().searchWord(property,parameters.getEffectiveSearchScope(),UsageSearchContext.IN_STRINGS,true,method);
    }
}
