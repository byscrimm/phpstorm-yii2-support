package io.github.byscrimm.yii2insight.relations;

import com.intellij.codeInspection.*;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.progress.ProgressManager;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import java.util.HashSet;
import java.util.Set;
import com.intellij.psi.*;
import com.jetbrains.php.lang.psi.elements.Method;
import org.jetbrains.annotations.NotNull;

/** Missing documentation is a weak suggestion, never an automatic source edit. */
public final class RelationPhpDocInspection extends LocalInspectionTool {
    @Override public @NotNull String getShortName() { return "Yii2InsightRelationPhpDocInspection"; }
    public static boolean missing(Method method, YiiModelResolver resolver) {
        if (RelationSymbols.getter(method, resolver) == null) return false;
        String name = RelationPath.property(method.getName());
        // Include inherited documentation; real fields must not be shadowed by a suggested PHPDoc property.
        return !hasProperty(method.getContainingClass(), name, resolver, new HashSet<>());
    }
    private static boolean hasProperty(PhpClass owner, String name, YiiModelResolver resolver, Set<String> seen) {
        ProgressManager.checkCanceled();
        if (seen.size() >= 32 || !seen.add(owner.getFQN())) return true;
        for (var field : owner.getOwnFields()) if (name.equals(field.getName())) return true;
        for (var parent : resolver.classes(owner.getSuperFQN())) if (hasProperty(parent, name, resolver, seen)) return true;
        for (String trait : owner.getTraitNames()) for (var type : resolver.classes(trait)) if (hasProperty(type, name, resolver, seen)) return true;
        return false;
    }
    public static String suggestedTag(Method getter, YiiModelResolver resolver) {
        var pending = new java.util.ArrayDeque<PhpClass>();
        pending.add(getter.getContainingClass());
        var seen = new HashSet<String>();
        String setterName = "set" + getter.getName().substring(3);
        while (!pending.isEmpty()) {
            ProgressManager.checkCanceled();
            var owner = pending.removeFirst();
            if (!seen.add(owner.getFQN())) continue;
            if (seen.size() > 32) return "@property";
            for (var method : owner.getOwnMethods())
                if (setterName.equalsIgnoreCase(method.getName()) && method.getModifier().isPublic() && !method.getModifier().isStatic()) return "@property";
            pending.addAll(resolver.classes(owner.getSuperFQN()));
            for (String trait : owner.getTraitNames()) pending.addAll(resolver.classes(trait));
        }
        return "@property-read";
    }
    @Override public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean onTheFly) {
        return new PsiElementVisitor() {
            @Override public void visitElement(@NotNull PsiElement element) {
                if (DumbService.isDumb(element.getProject())) return;
                var resolver = new YiiModelResolver(element.getProject());
                if (element instanceof PhpClass owner && !RelationPhpDocQuickFix.entries(owner, null, resolver).isEmpty()) {
                    PsiElement anchor = owner.getDocComment() != null ? owner.getDocComment() : owner.getNameIdentifier();
                    if (anchor != null) holder.registerProblem(anchor, "Class PHPDoc is missing relation properties", ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                            new RelationPhpDocQuickFix(owner, null));
                }
                if (element instanceof Method method && method.getNameIdentifier() != null && missing(method, resolver)) {
                    String message = "Add a class PHPDoc " + suggestedTag(method, resolver) + " for relation $" + RelationPath.property(method.getName());
                    if (!RelationPhpDocQuickFix.entries(method.getContainingClass(), method.getName(), resolver).isEmpty())
                        holder.registerProblem(method.getNameIdentifier(), message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                new RelationPhpDocQuickFix(method.getContainingClass(), method.getName()));
                    else holder.registerProblem(method.getNameIdentifier(), message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
            }
        };
    }
}
