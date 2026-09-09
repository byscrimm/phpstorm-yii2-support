package io.github.byscrimm.yii2insight.relations;

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocProperty;
import com.jetbrains.php.lang.psi.elements.Method;
import java.util.List;
import java.util.Map;
import java.util.ArrayDeque;
import java.util.HashSet;

/** Identity and rename pairing of a confirmed relation getter and its local PHPDoc property. */
public final class RelationSymbols {
    private RelationSymbols() {}
    public static Method getter(PsiElement element, YiiModelResolver resolver) {
        var owner = element instanceof Method m ? m.getContainingClass()
                : element instanceof PhpDocProperty p ? p.getContainingClass() : null;
        if (owner == null) return null;
        for (var relation : resolver.relations(owner)) {
            ProgressManager.checkCanceled();
            if (element instanceof Method m && relation.getter().isEquivalentTo(m)) return m;
            if (element instanceof PhpDocProperty p && relation.name().equals(p.getName())) return relation.getter();
        }
        return null;
    }
    public static List<PhpDocProperty> properties(Method getter) {
        var owner = getter.getContainingClass();
        if (owner == null || owner.getDocComment() == null) return List.of();
        String name = RelationPath.property(getter.getName());
        return PsiTreeUtil.findChildrenOfType(owner.getDocComment(), PhpDocProperty.class).stream()
                .filter(p -> name != null && name.equals(p.getName())).toList();
    }
    public static String conflict(PsiElement element, String newName, YiiModelResolver resolver) {
        Method getter = getter(element, resolver);
        if (getter == null) return "The relation is no longer resolvable.";
        String propertyName = element instanceof Method ? RelationPath.property(newName) : newName;
        if (propertyName == null || !propertyName.matches("[a-zA-Z_][a-zA-Z_0-9]*")) return "Use a valid relation name.";
        String methodName = element instanceof Method ? newName : "get" + Character.toUpperCase(newName.charAt(0)) + newName.substring(1);
        var allowedProperties = properties(getter);
        var pending = new ArrayDeque<com.jetbrains.php.lang.psi.elements.PhpClass>();
        pending.add(element instanceof PhpDocProperty p ? p.getContainingClass() : getter.getContainingClass());
        var seen = new HashSet<String>();
        while (!pending.isEmpty()) {
            ProgressManager.checkCanceled();
            var owner = pending.removeFirst();
            if (!seen.add(owner.getFQN())) continue;
            if (seen.size() > 32) return "The class hierarchy is too deep to validate the rename.";
            for (var method : owner.getOwnMethods())
                if (methodName.equalsIgnoreCase(method.getName()) && !method.isEquivalentTo(getter)) return "A method named " + methodName + " already exists.";
            for (var field : owner.getOwnFields())
                if (propertyName.equals(field.getName()) && !field.isEquivalentTo(element)
                        && allowedProperties.stream().noneMatch(p -> p.isEquivalentTo(field))) return "A property named $" + propertyName + " already exists.";
            pending.addAll(resolver.classes(owner.getSuperFQN()));
            for (String trait : owner.getTraitNames()) pending.addAll(resolver.classes(trait));
        }
        return null;
    }
    public static void prepare(PsiElement element, String newName, Map<PsiElement,String> renames, YiiModelResolver resolver) {
        Method getter = getter(element, resolver);
        if (getter == null || newName == null || newName.isEmpty()) return;
        String propertyName = element instanceof Method ? RelationPath.property(newName) : newName;
        if (propertyName == null) return;
        String methodName = element instanceof Method ? newName : "get" + Character.toUpperCase(newName.charAt(0)) + newName.substring(1);
        renames.put(getter, methodName);
        for (var property : properties(getter)) renames.put(property, propertyName);
        if (element instanceof PhpDocProperty) renames.put(element, propertyName);
    }
}
