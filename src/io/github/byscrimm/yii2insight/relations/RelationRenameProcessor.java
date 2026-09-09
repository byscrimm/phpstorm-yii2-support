package io.github.byscrimm.yii2insight.relations;

import com.intellij.openapi.project.DumbService;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import org.jetbrains.annotations.NotNull;
import java.util.Map;

/** Supplements native PHP processors, which retain conflict checking and actual writes. */
public final class RelationRenameProcessor extends RenamePsiElementProcessor {
    @Override public boolean canProcessElement(@NotNull PsiElement element) {
        return !DumbService.isDumb(element.getProject()) && RelationSymbols.getter(element, new YiiModelResolver(element.getProject())) != null;
    }
    @Override public void prepareRenaming(PsiElement element, String name, Map<PsiElement,String> renames) {
        RelationSymbols.prepare(element, name, renames, new YiiModelResolver(element.getProject()));
    }
    @Override public void findExistingNameConflicts(@NotNull PsiElement element, @NotNull String name,
            @NotNull com.intellij.util.containers.MultiMap<PsiElement,String> conflicts) {
        String conflict = RelationSymbols.conflict(element, name, new YiiModelResolver(element.getProject()));
        if (conflict != null) conflicts.putValue(element, conflict);
    }
    @Override public boolean forcesShowPreview() { return true; }
}
