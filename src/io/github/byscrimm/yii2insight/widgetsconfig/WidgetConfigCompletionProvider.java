package io.github.byscrimm.yii2insight.widgetsconfig;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.DumbService;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.*;
import io.github.byscrimm.yii2insight.common.*;
import io.github.byscrimm.yii2insight.configurations.ComponentResolver;
import io.github.byscrimm.yii2insight.relations.YiiModelResolver;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public final class WidgetConfigCompletionProvider extends CompletionProvider<CompletionParameters> {
    public static WidgetModelResolver resolver(PsiElement origin) {
        YiiModelResolver yii = new YiiModelResolver(origin.getProject());
        return new WidgetModelResolver(yii, expression -> {
            if (!(expression instanceof PhpTypedElement typed)) return List.of();
            Set<PhpClass> result = new LinkedHashSet<>();
            for (String name : typed.getType().global(origin.getProject()).getTypes())
                if (name.startsWith("\\") && !name.endsWith("[]")) result.addAll(yii.classes(name));
            return List.copyOf(result);
        });
    }
    @Override protected void addCompletions(@NotNull CompletionParameters parameters,
                                           @NotNull ProcessingContext processing, @NotNull CompletionResultSet result) {
        if (DumbService.isDumb(parameters.getPosition().getProject())
                || !(parameters.getPosition().getParent() instanceof StringLiteralExpression literal)) return;
        WidgetContext context = WidgetContext.of(literal);
        WidgetModelResolver resolver = resolver(literal);
        if (!resolver.supports(context)) return;
        int start = literal.getTextOffset() + literal.getValueRange().getStartOffset();
        int offset = Math.max(0, Math.min(literal.getContents().length(), parameters.getOffset() - start));
        if (context.kind() == WidgetContext.Kind.OPTION) {
            String[] options = resolver.isGrid(context)
                    ? new String[]{"attribute", "value", "format", "label", "visible", "filterAttribute", "filter", "contentOptions", "headerOptions", "enableSorting"}
                    : new String[]{"attribute", "value", "format", "label", "visible", "contentOptions", "captionOptions"};
            // Only supply names. No raw quote/offset insertion that could overwrite an existing value.
            for (String option : options) result.addElement(LookupElementBuilder.create(option));
            return;
        }
        WidgetAttributePosition position = context.kind() == WidgetContext.Kind.FORMAT
                ? new WidgetAttributePosition(true, "", literal.getContents().substring(0, offset))
                : WidgetAttributePosition.parse(literal.getContents(), offset, context.column() == null);
        if (position == null) return;
        result = result.withPrefixMatcher(position.prefix());
        if (position.format()) {
            for (var entry : formatters(context).entrySet()) result.addElement(LookupElementBuilder.create(entry.getValue(), entry.getKey()));
            return;
        }
        for (var attribute : resolver.attributes(resolver.models(context), position.parentPath())) {
            result.addElement(LookupElementBuilder.create(attribute.declaration(), attribute.name())
                    .withIcon(attribute.declaration().getIcon())
                    .withTailText(attribute.declaration() instanceof Method ? "  getter" : "", true));
        }
    }
    private Map<String, Method> formatters(WidgetContext context) {
        YiiModelResolver yii = new YiiModelResolver(context.call().getProject());
        PsiElement configured = LocalPhpValues.resolve(PhpArrays.value(context.config(), "formatter"));
        Set<PhpClass> classes = new LinkedHashSet<>(yii.classes(PhpArrays.className(configured)));
        if (classes.isEmpty()) for (String fqn : ComponentResolver.classes(context.call(), "formatter")) classes.addAll(yii.classes(fqn));
        if (classes.isEmpty()) classes.addAll(yii.classes("\\yii\\i18n\\Formatter"));
        Map<String, Method> result = new LinkedHashMap<>();
        for (PhpClass clazz : classes) for (Method method : ClassUtils.getFormatterAsMethods(clazz))
            if (method.getAccess().isPublic() && !method.isStatic()) result.putIfAbsent(ClassUtils.getAsPropertyName(method), method);
        return result;
    }
}
