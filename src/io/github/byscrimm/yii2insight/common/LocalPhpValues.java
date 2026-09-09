package io.github.byscrimm.yii2insight.common;

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import java.util.*;

/** Local syntax only: safe during indexing. Ambiguous control flow stays unresolved. */
public final class LocalPhpValues {
    private LocalPhpValues() {}
    public static PsiElement resolve(PsiElement value) {
        Set<PsiElement> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (int depth = 0; value instanceof Variable variable && depth < 24; depth++) {
            if (!seen.add(value)) return null;
            value = definition(variable.getName(), variable);
        }
        return value instanceof Variable ? null : value;
    }
    public static PsiElement definition(String name, PsiElement use) {
        Function function = PsiTreeUtil.getParentOfType(use, Function.class);
        PsiElement scope = function != null ? function : use.getContainingFile();
        if (scope == null) return null;
        AssignmentExpression last = null;
        for (AssignmentExpression assignment : PsiTreeUtil.findChildrenOfType(scope, AssignmentExpression.class)) {
            ProgressManager.checkCanceled();
            if (PsiTreeUtil.isAncestor(assignment, use, false) || assignment.getTextOffset() >= use.getTextOffset()
                    || PsiTreeUtil.getParentOfType(assignment, Function.class) != function) continue;
            if (!(assignment.getVariable() instanceof Variable variable) || !name.equals(variable.getName())) continue;
            if (last == null || assignment.getTextOffset() > last.getTextOffset()) last = assignment;
        }
        if (last == null || PsiTreeUtil.isAncestor(last, use, false)) return null;
        // A branch/loop assignment is not necessarily the value reaching this use.
        GroupStatement block = PsiTreeUtil.getParentOfType(last, GroupStatement.class);
        if (block == null || !PsiTreeUtil.isAncestor(block, use, false)) return null;
        return last.getValue();
    }
}
