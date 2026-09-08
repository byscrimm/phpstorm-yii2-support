package io.github.byscrimm.yii2insight.widgetsconfig;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocParamTag;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import io.github.byscrimm.yii2insight.common.*;

/** Structural widget context. This class is syntax-only and safe during indexing. */
public record WidgetContext(MethodReference call, ArrayCreationExpression config,
                            ArrayCreationExpression column, String listKey, Kind kind) {
    public enum Kind { ATTRIBUTE, FORMAT, FILTER_ATTRIBUTE, VALUE, OPTION, CALLBACK }

    public static WidgetContext of(StringLiteralExpression literal) {
        ArrayHashElement entry = PsiTreeUtil.getParentOfType(literal, ArrayHashElement.class);
        if (entry != null && entry.getValue() == literal) {
            String key = PhpArrays.string(entry.getKey());
            Kind kind = switch (key == null ? "" : key) {
                case "attribute" -> Kind.ATTRIBUTE;
                case "format" -> Kind.FORMAT;
                case "filterAttribute" -> Kind.FILTER_ATTRIBUTE;
                case "value" -> Kind.VALUE;
                default -> null;
            };
            if (kind == null) return null;
            ArrayCreationExpression column = arrayParent(entry);
            return fromList(arrayParent(column), column, kind);
        }
        ArrayCreationExpression array = arrayParent(literal);
        if (array == null) return null;
        if (PhpArrays.keyEntry(literal) != null) {
            // Column option keys, not arbitrary nested HTML/formatter configuration.
            return fromList(arrayParent(array), array, Kind.OPTION);
        }
        if (!isListValue(literal, array)) return null;
        WidgetContext shorthand = fromList(array, null, Kind.ATTRIBUTE);
        return shorthand != null ? shorthand : fromList(arrayParent(array), array, Kind.OPTION);
    }

    public static WidgetContext callback(Parameter parameter) {
        Function function = PsiTreeUtil.getParentOfType(parameter, Function.class);
        if (function == null || !function.isClosure() || function.getParameters().length == 0
                || function.getParameters()[0] != parameter || parameter.isVariadic()
                || parameter.getTypeDeclaration() != null || parameter.getDocTag() != null) return null;
        ArrayHashElement entry = PsiTreeUtil.getParentOfType(function, ArrayHashElement.class);
        if (entry == null || (entry.getValue() == null || !entry.getValue().getTextRange().equals(function.getTextRange())) || !"value".equals(PhpArrays.string(entry.getKey()))) return null;
        if (hasInlineDocType(function, entry, parameter)) return null;
        ArrayCreationExpression column = arrayParent(entry);
        return fromList(arrayParent(column), column, Kind.CALLBACK);
    }

    private static boolean hasInlineDocType(Function function, ArrayHashElement entry, Parameter parameter) {
        for (PsiElement element = function; element != null && element != entry; element = element.getParent()) {
            PsiElement previous = element.getPrevSibling();
            while (previous instanceof PsiWhiteSpace) previous = previous.getPrevSibling();
            if (previous instanceof PhpDocComment doc) {
                for (var tag : doc.getTagElementsByName("@param")) {
                    if (tag instanceof PhpDocParamTag param && (parameter.getName().equals(param.getVarName())
                            || ("$" + parameter.getName()).equals(param.getVarName()))) return true;
                }
            }
        }
        return false;
    }

    private static WidgetContext fromList(ArrayCreationExpression list, ArrayCreationExpression column, Kind kind) {
        if (list == null || column != null && !isListValue(column, list)) return null;
        ArrayHashElement entry = PsiTreeUtil.getParentOfType(list, ArrayHashElement.class);
        if (entry == null || entry.getValue() != list) return null;
        String key = PhpArrays.string(entry.getKey());
        if (!"columns".equals(key) && !"attributes".equals(key)) return null;
        ArrayCreationExpression config = arrayParent(entry);
        MethodReference call = PsiTreeUtil.getParentOfType(config, MethodReference.class);
        if (call == null || !"widget".equals(call.getName()) || PhpArguments.get(call, "config", 0) != config) return null;
        return new WidgetContext(call, config, column, key, kind);
    }

    private static ArrayCreationExpression arrayParent(PsiElement element) {
        return element == null ? null : PsiTreeUtil.getParentOfType(element, ArrayCreationExpression.class);
    }

    private static boolean isListValue(PsiElement value, ArrayCreationExpression array) {
        for (PsiElement parent = value.getParent(); parent != null && parent != array; parent = parent.getParent()) {
            if (parent instanceof ArrayHashElement || parent instanceof ArrayCreationExpression
                    || parent instanceof Function || parent instanceof ParameterListOwner) return false;
        }
        return PsiTreeUtil.isAncestor(array, value, true);
    }
}
