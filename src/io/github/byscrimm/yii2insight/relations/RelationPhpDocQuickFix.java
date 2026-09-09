package io.github.byscrimm.yii2insight.relations;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import io.github.byscrimm.yii2insight.common.LocalPhpValues;
import org.jetbrains.annotations.NotNull;
import java.util.*;

/** Recompute missing entries when invoked; preserve existing PHPDoc text and use one IDE write command. */
public final class RelationPhpDocQuickFix implements LocalQuickFix {
    private final SmartPsiElementPointer<PhpClass> owner;
    private final String getterName;
    public RelationPhpDocQuickFix(PhpClass owner, String getterName) {
        this.owner = SmartPointerManager.getInstance(owner.getProject()).createSmartPsiElementPointer(owner);
        this.getterName = getterName;
    }
    @Override public @NotNull String getFamilyName() {
        return getterName == null ? "Add PHPDoc for missing relations" : "Add PHPDoc for this relation";
    }
    public static List<String> entries(PhpClass owner, String getterName, YiiModelResolver resolver) {
        List<String> entries = new ArrayList<>();
        for (var relation : resolver.relations(owner)) {
            ProgressManager.checkCanceled();
            Method getter = relation.getter();
            if (!owner.isEquivalentTo(getter.getContainingClass()) || getterName != null && !getterName.equals(getter.getName())
                    || !RelationPhpDocInspection.missing(getter, resolver) || relation.targets().size() != 1) continue;
            String type = propertyType(getter, relation.targets().getFirst());
            if (type != null) entries.add(RelationPhpDocInspection.suggestedTag(getter, resolver) + " " + type + " $" + relation.name());
        }
        return entries;
    }
    private static String propertyType(Method getter, PhpClass target) {
        String cardinality = null;
        for (PhpReturn ret : PsiTreeUtil.findChildrenOfType(getter, PhpReturn.class)) {
            ProgressManager.checkCanceled();
            if (PsiTreeUtil.getParentOfType(ret, Function.class) != getter) continue;
            PsiElement expression = LocalPhpValues.resolve(ret.getFirstPsiChild());
            String found = null;
            for (int depth = 0; depth < 24 && expression instanceof MethodReference call; depth++) {
                if ("asArray".equals(call.getName())) return null;
                if ("hasOne".equals(call.getName()) || "hasMany".equals(call.getName())) {
                    if (call.getClassReference() instanceof Variable v && "this".equals(v.getName())) found = call.getName();
                    break;
                }
                expression = call.getClassReference();
            }
            if (found == null || cardinality != null && !cardinality.equals(found)) return null;
            cardinality = found;
        }
        return cardinality == null ? null : target.getFQN() + ("hasMany".equals(cardinality) ? "[]" : "|null");
    }
    public static String append(String existing, List<String> entries) {
        if (entries.isEmpty()) return existing;
        String newline = existing.contains("\r\n") ? "\r\n" : "\n";
        int end = existing.lastIndexOf("*/");
        int line = existing.lastIndexOf('\n', Math.max(0, end));
        String indent = line >= 0 && end >= 0 && existing.substring(line + 1, end).isBlank()
                ? existing.substring(line + 1, end) : " ";
        String additions = String.join(newline, entries.stream().map(entry -> indent + "* " + entry).toList());
        if (existing.isEmpty()) return "/**" + newline + additions + newline + " */";
        if (end < 0) return existing;
        if (line >= 0 && existing.substring(line + 1, end).isBlank())
            return existing.substring(0, line + 1) + additions + newline + existing.substring(line + 1);
        return existing.substring(0, end) + newline + additions + newline + " " + existing.substring(end);
    }

    public static boolean add(PhpClass owner, String getterName, YiiModelResolver resolver) {
        var entries = entries(owner, getterName, resolver);
        if (entries.isEmpty()) return false;
        var old = owner.getDocComment();
        String source = owner.getContainingFile().getText();
        int start = owner.getTextRange().getStartOffset();
        String indentation = source.substring(source.lastIndexOf('\n', Math.max(0, start - 1)) + 1, start);
        if (!indentation.isBlank()) indentation = "";
        String text = append(old == null ? "" : old.getText(), entries);
        if (old == null) text = text.replace("\n", "\n" + indentation);
        var template = PhpPsiElementFactory.createFromText(owner.getProject(), PhpClass.class, text + "\n" + indentation + "class Yii2InsightDocTemplate {}");
        if (template == null || template.getDocComment() == null) return false;
        var comment = template.getDocComment();
        if (old != null) old.replace(comment);
        else {
            PsiElement whitespace = comment.getNextSibling();
            if (!(whitespace instanceof PsiWhiteSpace)) return false;
            owner.getParent().addBefore(comment, owner);
            owner.getParent().addBefore(whitespace, owner);
        }
        return true;
    }
    @Override public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        PhpClass clazz = owner.getElement();
        if (clazz == null || !clazz.isValid() || DumbService.isDumb(project)) return;
        add(clazz, getterName, new YiiModelResolver(project));
    }
}
