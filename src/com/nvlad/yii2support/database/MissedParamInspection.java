package com.nvlad.yii2support.database;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.nvlad.yii2support.common.ClassUtils;
import com.nvlad.yii2support.common.DatabaseUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Created by oleg on 30.03.2017.
 */
public class MissedParamInspection extends PhpInspection {
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder problemsHolder, boolean isOnTheFly) {

        return new PhpElementVisitor() {

            @Override
            public void visitPhpMethodReference(MethodReference reference) {
                if (reference != null && reference.getParameters().length > 0) {

                    Method method = reference.resolve() instanceof Method resolved ? resolved : null;
                    if (method == null || method.getContainingClass() == null || !method.getContainingClass().getFQN().startsWith("\\yii\\db\\"))
                        return;
                    int paramParameterIndex = ClassUtils.getParamIndex(method, "params");
                    int conditionParameterIndex = ClassUtils.getParamIndex(method, new String[]{ "condition", "expression", "sql"});

                    if (paramParameterIndex > -1 && conditionParameterIndex > -1 && reference.getParameters().length > conditionParameterIndex &&
                            conditionParameterIndex == paramParameterIndex - 1) {

                        PsiElement element = reference.getParameters()[conditionParameterIndex];

                        if (!(element instanceof StringLiteralExpression)) return;
                        String condition = ClassUtils.getStringByElement(element);
                        String[] conditionParams = DatabaseUtils.extractParamsFromCondition(condition);
                        String[] conditionParamsWithoutColon = DatabaseUtils.extractParamsFromCondition(condition, false);

                        if (conditionParams.length > 0) {
                            if (reference.getParameters().length > paramParameterIndex) {
                                PsiElement paramParam = reference.getParameters()[paramParameterIndex];
                                if (paramParam instanceof ArrayCreationExpression) {
                                    ArrayCreationExpression array = (ArrayCreationExpression) paramParam;
                                    ArrayList<String> paramString = new ArrayList<>();
                                    for (ArrayHashElement elem : array.getHashElements()) {
                                        if (!(elem.getKey() instanceof StringLiteralExpression)) return;
                                        if (elem.getKey() != null && elem.getKey().getText() != null)
                                            paramString.add(ClassUtils.removeQuotes(elem.getKey().getText()).trim());
                                    }
                                    if (!paramString.stream().map(com.nvlad.yii2support.common.SqlParameters::normalize).collect(java.util.stream.Collectors.toSet())
                                            .equals(new java.util.HashSet<>(Arrays.asList(conditionParamsWithoutColon)))) {
                                        MissedParamQuickFix qFix = new MissedParamQuickFix(reference);
                                        problemsHolder.registerProblem(reference.getParameters()[paramParameterIndex], "Condition parameters do not conform to the condition", qFix);
                                    }
                                }
                            } else {
                                    MissedParamQuickFix qFix = new MissedParamQuickFix(reference);
                                    problemsHolder.registerProblem(reference.getParameters()[conditionParameterIndex], "Condition parameters must be defined", qFix);

                            }

                        }

                    }
                }
                super.visitPhpMethodReference(reference);
            }

        };

    }

}
