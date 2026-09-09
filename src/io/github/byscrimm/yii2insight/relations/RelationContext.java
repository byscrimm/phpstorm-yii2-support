package io.github.byscrimm.yii2insight.relations;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import io.github.byscrimm.yii2insight.common.PhpArguments;
import java.util.Set;

public record RelationContext(MethodReference call, PsiElement modelExpression, boolean allowAlias) {
    public static RelationContext of(StringLiteralExpression literal) {
        MethodReference call = PsiTreeUtil.getParentOfType(literal,MethodReference.class);
        if (call == null || call.getName() == null) return null;
        String name = call.getName();
        if (!Set.of("with","joinWith","innerJoinWith","getRelation","via").contains(name)) return null;
        PsiElement argument = literal;
        while (argument != null && argument.getParent() != call.getParameterList()) argument = argument.getParent();
        if (argument == null) return null;
        if (!name.equals("with")) {
            String parameter = name.equals("getRelation") ? "name" : name.equals("via") ? "relationName" : "with";
            if (argument != PhpArguments.get(call,parameter,0)) return null;
        }
        if (argument != literal) {
            if (!(argument instanceof ArrayCreationExpression array)) return null;
            ArrayHashElement hash = PsiTreeUtil.getParentOfType(literal,ArrayHashElement.class);
            if (hash != null && hash.getParent() == array) {
                if (io.github.byscrimm.yii2insight.common.PhpArrays.keyEntry(literal) != hash) return null;
            } else if (literal.getParent() != array && literal.getParent().getParent() != array) return null;
        }
        PsiElement model = call.getClassReference();
        if (name.equals("via")) {
            // via() names a relation on the declaring AR, not on the related model.
            while (model instanceof MethodReference previous) model = previous.getClassReference();
        }
        return new RelationContext(call, model, name.equals("joinWith") || name.equals("innerJoinWith"));
    }
}
