package io.github.byscrimm.yii2insight.common;

import com.intellij.psi.*;
import com.jetbrains.php.lang.psi.elements.ParameterListOwner;

/** Named arguments are PSI siblings of their values, not wrapper expressions. */
public final class PhpArguments {
    private PhpArguments() {}
    public static PsiElement get(ParameterListOwner call, String name, int position) {
        if (call == null) return null;
        PsiElement[] args = call.getParameters();
        for (PsiElement argument : args) if (name.equals(name(argument))) return argument;
        return position >= 0 && position < args.length && name(args[position]) == null ? args[position] : null;
    }
    public static String name(PsiElement argument) {
        PsiElement colon = previous(argument);
        if (colon == null || !":".equals(colon.getText())) return null;
        PsiElement name = previous(colon);
        return name == null ? null : name.getText();
    }
    private static PsiElement previous(PsiElement element) {
        PsiElement sibling = element.getPrevSibling();
        while (sibling instanceof PsiWhiteSpace || sibling instanceof PsiComment) sibling = sibling.getPrevSibling();
        return sibling;
    }
}
