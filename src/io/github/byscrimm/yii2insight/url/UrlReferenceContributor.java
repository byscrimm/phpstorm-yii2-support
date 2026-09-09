package io.github.byscrimm.yii2insight.url;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.*;
import io.github.byscrimm.yii2insight.common.*;
import org.jetbrains.annotations.NotNull;

public final class UrlReferenceContributor extends PsiReferenceContributor {
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(StringLiteralExpression.class), new PsiReferenceProvider() {
            public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                if (!isRoute((StringLiteralExpression) element)) return PsiReference.EMPTY_ARRAY;
                return new PsiReference[]{new PsiReferenceBase<PsiElement>(element) {
                    public PsiElement resolve() { return UrlUtils.resolve(((StringLiteralExpression) myElement).getContents(), myElement); }
                    public Object @NotNull [] getVariants() { return new Object[0]; }
                    public PsiElement handleElementRename(@NotNull String newName) {
                        String old = ((StringLiteralExpression) myElement).getContents();
                        // Inline actions map actionUpdate -> update; external Action::run does not rename its config key.
                        if (!newName.startsWith("action") || newName.length() <= 6) return myElement;
                        String value = old.substring(0, old.lastIndexOf('/') + 1) + RouteNames.id(newName.substring(6));
                        return ElementManipulators.handleContentChange(myElement, value);
                    }
                }};
            }
        });
    }
    public static boolean isRoute(StringLiteralExpression literal) {
        MethodReference call = PsiTreeUtil.getParentOfType(literal, MethodReference.class);
        if (call == null || !(call.resolve() instanceof Method method)) return false;
        PsiElement argument = literal;
        while (argument != null && !(argument.getParent() instanceof ParameterList)) argument = argument.getParent();
        if (argument == null) return false;
        int index = ClassUtils.indexForElementInParameterList(argument);
        if (index < 0 || index >= method.getParameters().length) return false;
        String name = method.getParameters()[index].getName();
        if (!name.equals("url") && !name.equals("route")) return false;
        PhpClass owner = method.getContainingClass();
        if (owner == null || !owner.getFQN().startsWith("\\yii\\")) return false;
        if (argument == literal) return name.equals("route");
        if (!(argument instanceof ArrayCreationExpression array)) return false;
        PsiElement first = array.getFirstPsiChild();
        return first != null && PsiTreeUtil.isAncestor(first, literal, false)
                && PhpArrays.keyEntry(literal) == null;
    }
}
