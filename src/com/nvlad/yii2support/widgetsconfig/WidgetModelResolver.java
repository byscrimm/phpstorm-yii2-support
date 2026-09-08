package com.nvlad.yii2support.widgetsconfig;

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocProperty;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.*;
import com.nvlad.yii2support.common.*;
import com.nvlad.yii2support.relations.*;
import java.util.*;

/** Short-lived widget model resolver. Never call the indexed type fallback during indexing. */
public final class WidgetModelResolver {
    @FunctionalInterface public interface Types { List<PhpClass> classes(PsiElement expression); }
    public record Attribute(String name, PhpClassMember declaration) {}
    private final YiiModelResolver yii;
    private final Types types;
    public WidgetModelResolver(YiiModelResolver yii, Types types) { this.yii = yii; this.types = types; }
    public WidgetModelResolver(YiiModelResolver yii) { this(yii, ignored -> List.of()); }

    public boolean isGrid(WidgetContext context) {
        return context != null && "columns".equals(context.listKey()) && widgetClasses(context).stream()
                .anyMatch(c -> yii.inherits(c, "\\yii\\grid\\GridView"));
    }
    public boolean isDetail(WidgetContext context) {
        return context != null && "attributes".equals(context.listKey()) && widgetClasses(context).stream()
                .anyMatch(c -> yii.inherits(c, "\\yii\\widgets\\DetailView"));
    }
    private List<PhpClass> widgetClasses(WidgetContext context) {
        return context.call().getClassReference() instanceof ClassReference ref ? yii.classes(ref.getFQN()) : List.of();
    }
    public boolean supports(WidgetContext context) {
        if (context == null) return false;
        if (isDetail(context)) return context.kind() != WidgetContext.Kind.FILTER_ATTRIBUTE && context.kind() != WidgetContext.Kind.VALUE;
        if (!isGrid(context)) return false;
        // Custom non-data columns can assign different meanings to attribute/value callbacks.
        PsiElement columnClass = context.column() == null ? null : PhpArrays.value(context.column(), "class");
        return columnClass == null || yii.classes(PhpArrays.className(columnClass)).stream()
                .anyMatch(c -> yii.inherits(c, "\\yii\\grid\\DataColumn"));
    }
    public List<PhpClass> models(WidgetContext context) {
        if (!supports(context)) return List.of();
        if (isDetail(context)) return objects(PhpArrays.value(context.config(), "model"));
        if (context.kind() == WidgetContext.Kind.FILTER_ATTRIBUTE) return objects(PhpArrays.value(context.config(), "filterModel"));
        PsiElement provider = LocalPhpValues.resolve(PhpArrays.value(context.config(), "dataProvider"));
        if (provider instanceof NewExpression object && object.getClassReference() != null
                && yii.classes(object.getClassReference().getFQN()).stream().anyMatch(c -> yii.inherits(c, "\\yii\\data\\ActiveDataProvider"))) {
            PsiElement config = LocalPhpValues.resolve(PhpArguments.get(object, "config", 0));
            if (config instanceof ArrayCreationExpression array) return queryModels(PhpArrays.value(array, "query"));
        }
        // filterModel describes filter attributes, not row objects passed to value callbacks.
        return context.kind() == WidgetContext.Kind.CALLBACK || context.kind() == WidgetContext.Kind.VALUE
                ? List.of() : objects(PhpArrays.value(context.config(), "filterModel"));
    }
    private List<PhpClass> queryModels(PsiElement expression) {
        PsiElement value = LocalPhpValues.resolve(expression);
        if (value instanceof MethodReference call) {
            if (Set.of("one", "all", "findOne", "findAll").contains(call.getName() == null ? "" : call.getName())) return List.of();
            return yii.models(value);
        }
        if (value instanceof NewExpression object && object.getClassReference() != null
                && yii.classes(object.getClassReference().getFQN()).stream().anyMatch(c -> yii.inherits(c, "\\yii\\db\\ActiveQuery")))
            return yii.models(value);
        return List.of();
    }
    private List<PhpClass> objects(PsiElement expression) {
        PsiElement value = LocalPhpValues.resolve(expression);
        if (value instanceof NewExpression object && object.getClassReference() != null) return yii.classes(object.getClassReference().getFQN());
        if (value instanceof MethodReference call) {
            if ("all".equals(call.getName()) || "findAll".equals(call.getName())) return List.of();
            if ("one".equals(call.getName()) || "findOne".equals(call.getName())) {
                if (queryMode(call) != RowMode.OBJECT) return List.of();
                return yii.models(value);
            }
        }
        return types.classes(expression);
    }
    public enum RowMode { OBJECT, ARRAY, UNKNOWN }
    public RowMode rowMode(WidgetContext context) {
        if (!isGrid(context)) return RowMode.OBJECT;
        PsiElement provider = LocalPhpValues.resolve(PhpArrays.value(context.config(), "dataProvider"));
        if (!(provider instanceof NewExpression object)) return RowMode.OBJECT;
        PsiElement config = LocalPhpValues.resolve(PhpArguments.get(object, "config", 0));
        if (!(config instanceof ArrayCreationExpression array)) return RowMode.OBJECT;
        return queryMode(PhpArrays.value(array, "query"));
    }
    private RowMode queryMode(PsiElement expression) {
        PsiElement query = LocalPhpValues.resolve(expression);
        Set<PsiElement> seen = new HashSet<>();
        for (int depth = 0; query instanceof MethodReference call && depth < 24 && seen.add(query); depth++) {
            if ("asArray".equals(call.getName())) {
                PsiElement argument = PhpArguments.get(call, "value", 0);
                if (argument == null) return RowMode.ARRAY;
                PsiElement value = LocalPhpValues.resolve(argument);
                if (value == null) return RowMode.UNKNOWN;
                if ("true".equalsIgnoreCase(value.getText())) return RowMode.ARRAY;
                if ("false".equalsIgnoreCase(value.getText())) return RowMode.OBJECT;
                return RowMode.UNKNOWN;
            }
            query = LocalPhpValues.resolve(call.getClassReference());
        }
        return RowMode.OBJECT;
    }
    public List<Attribute> attributes(List<PhpClass> models, String path) {
        if (!path.isEmpty() && RelationPath.segments(path, false).isEmpty()) return List.of();
        List<PhpClass> current = models;
        if (!path.isEmpty()) for (String segment : path.split("\\.")) {
            Set<PhpClass> targets = new LinkedHashSet<>();
            for (PhpClass model : current) for (var relation : yii.relations(model))
                if (segment.equals(relation.name())) targets.addAll(relation.targets());
            current = List.copyOf(targets);
            if (current.isEmpty()) return List.of();
        }
        Map<String, Attribute> result = new LinkedHashMap<>();
        for (PhpClass model : current) {
            Map<String, Field> fields = new LinkedHashMap<>();
            Map<String, Method> methods = new LinkedHashMap<>();
            members(model, fields, methods, new HashSet<>());
            for (Field field : fields.values()) if (!field.isConstant() && !field.getModifier().isStatic()
                    && field.getModifier().isPublic() && readable(field)) result.putIfAbsent(field.getName(), new Attribute(field.getName(), field));
            for (Method method : methods.values()) {
                String name = RelationPath.property(method.getName());
                if (name != null && method.getAccess().isPublic() && !method.isStatic()
                        && Arrays.stream(method.getParameters()).allMatch(p -> p.isOptional() || p.isVariadic()))
                    result.putIfAbsent(name, new Attribute(name, method));
            }
        }
        return List.copyOf(result.values());
    }
    private boolean readable(Field field) {
        if (!(field instanceof PhpDocProperty)) return true;
        PhpDocTag tag = PsiTreeUtil.getParentOfType(field, PhpDocTag.class);
        return tag == null || !"@property-write".equals(tag.getName());
    }
    private void members(PhpClass clazz, Map<String, Field> fields, Map<String, Method> methods, Set<String> seen) {
        if (seen.size() >= 32 || !seen.add(clazz.getFQN())) return;
        ProgressManager.checkCanceled();
        for (Field field : clazz.getOwnFields()) fields.putIfAbsent(field.getName(), field);
        for (Method method : clazz.getOwnMethods()) methods.putIfAbsent(method.getName().toLowerCase(Locale.ROOT), method);
        for (String trait : clazz.getTraitNames()) for (PhpClass type : yii.classes(trait)) members(type, fields, methods, seen);
        for (PhpClass parent : yii.classes(clazz.getSuperFQN())) members(parent, fields, methods, seen);
    }
}
