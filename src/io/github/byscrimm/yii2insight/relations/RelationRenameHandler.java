package io.github.byscrimm.yii2insight.relations;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.refactoring.rename.RenameHandler;
import com.intellij.refactoring.rename.RenameDialog;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocProperty;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

/** Consume Rename on relation links without starting the native getter refactoring. */
public final class RelationRenameHandler implements RenameHandler {
    public static Method target(StringLiteralExpression literal, int offset, YiiModelResolver resolver) {
        for (PsiReference reference : RelationReferenceContributor.references(literal, resolver)) {
            if (!reference.getRangeInElement().containsOffset(offset)) continue;
            PsiElement resolved = reference.resolve();
            return resolved instanceof Method method ? method : null;
        }
        return null;
    }
    private static Method target(DataContext context) {
        Editor editor = CommonDataKeys.EDITOR.getData(context);
        PsiFile file = CommonDataKeys.PSI_FILE.getData(context);
        if (editor == null || file == null || DumbService.isDumb(file.getProject())) return null;
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = file.findElementAt(offset);
        StringLiteralExpression literal = PsiTreeUtil.getParentOfType(element, StringLiteralExpression.class, false);
        return literal == null ? null : target(literal, offset - literal.getTextRange().getStartOffset(), new YiiModelResolver(file.getProject()));
    }
    private static PsiElement declaration(DataContext context) {
        PsiElement element = CommonDataKeys.PSI_ELEMENT.getData(context);
        if (element == null) {
            Editor editor = CommonDataKeys.EDITOR.getData(context);
            PsiFile file = CommonDataKeys.PSI_FILE.getData(context);
            if (editor == null || file == null) return null;
            PsiElement leaf = file.findElementAt(editor.getCaretModel().getOffset());
            element = PsiTreeUtil.getParentOfType(leaf, PhpDocProperty.class, false);
            if (element == null) {
                Method method = PsiTreeUtil.getParentOfType(leaf, Method.class, false);
                if (method != null && method.getNameIdentifier() != null && method.getNameIdentifier().getTextRange().containsOffset(editor.getCaretModel().getOffset())) element = method;
            }
        }
        if (element == null || DumbService.isDumb(element.getProject())) return null;
        return RelationSymbols.getter(element, new YiiModelResolver(element.getProject())) == null ? null : element;
    }
    @Override public boolean isAvailableOnDataContext(@NotNull DataContext context) { return target(context) != null || declaration(context) != null; }
    @Override public void invoke(@NotNull Project project, Editor editor, PsiFile file, @NotNull DataContext context) {
        if (target(context) != null) {
            HintManager.getInstance().showErrorHint(editor, "Rename this relation from its getter or PHPDoc property, not from the string.");
            return;
        }
        PsiElement element = declaration(context);
        if (element == null) return;
        new RenameDialog(project, element, element, editor) {
            @Override protected void canRun() throws com.intellij.openapi.options.ConfigurationException {
                super.canRun();
                if (element instanceof Method && RelationPath.property(getNewName()) == null)
                    throw new com.intellij.openapi.options.ConfigurationException("Use a getter name, for example getArea.");
                String conflict = RelationSymbols.conflict(element, getNewName(), new YiiModelResolver(project));
                if (conflict != null) throw new com.intellij.openapi.options.ConfigurationException(conflict);
            }
        }.show();
    }

    @Override public void invoke(@NotNull Project project, PsiElement @NotNull [] elements, @NotNull DataContext context) {
        Editor editor = CommonDataKeys.EDITOR.getData(context);
        PsiFile file = CommonDataKeys.PSI_FILE.getData(context);
        if (editor != null && file != null) invoke(project, editor, file, context);
    }
}
