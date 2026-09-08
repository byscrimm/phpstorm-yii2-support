package com.nvlad.yii2support.database;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.nvlad.yii2support.common.*;
import org.jetbrains.annotations.NotNull;
import java.util.*;

/** Replaces only the params argument; preserves values and does not require an open editor. */
public class MissedParamQuickFix implements LocalQuickFix {
    private final SmartPsiElementPointer<MethodReference> call;
    public MissedParamQuickFix(MethodReference reference) {
        call = SmartPointerManager.createPointer(reference);
    }
    @Override public @NotNull String getFamilyName() { return "Conform parameters to condition"; }
    @Override public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        MethodReference reference = call.getElement();
        if (reference == null || !(reference.resolve() instanceof Method method)) return;
        int params = ClassUtils.getParamIndex(method, "params");
        int condition = ClassUtils.getParamIndex(method, new String[]{"condition", "expression", "sql"});
        if (params < 0 || condition < 0 || params != condition + 1 || reference.getParameters().length <= condition) return;
        conform(reference, condition, params);
    }
    public static void conform(MethodReference reference, int conditionIndex, int paramsIndex) {
        PsiElement[] arguments = reference.getParameters();
        if (arguments.length <= conditionIndex || reference.getParameterList() == null) return;
        PsiElement condition = arguments[conditionIndex];
        // Unknown expressions can contain placeholders supplied at runtime.
        if (!(condition instanceof StringLiteralExpression || condition instanceof ConcatenationExpression)) return;
        String sql = ClassUtils.getStringByElement(condition);
        Map<String,String> values = new LinkedHashMap<>();
        if (arguments.length > paramsIndex) {
            if (!(arguments[paramsIndex] instanceof ArrayCreationExpression array)) return;
            for (ArrayHashElement hash : array.getHashElements()) {
                String key = PhpArrays.string(hash.getKey());
                if (key == null || hash.getValue() == null) return;
                values.put(SqlParameters.normalize(key), hash.getValue().getText());
            }
        }
        StringJoiner entries = new StringJoiner(", ", "[", "]");
        for (String key : SqlParameters.names(sql)) entries.add("':" + key + "' => " + values.getOrDefault(key, "null"));
        ArrayCreationExpression replacement = PhpPsiElementFactory.createFromText(reference.getProject(), ArrayCreationExpression.class, entries.toString());
        if (replacement == null) return;
        if (arguments.length > paramsIndex) arguments[paramsIndex].replace(replacement);
        else {
            ParameterList list = reference.getParameterList();
            if (arguments.length > 0) list.add(PhpPsiElementFactory.createComma(reference.getProject()));
            list.add(replacement);
        }
    }
}
