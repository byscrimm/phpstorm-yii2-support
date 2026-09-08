package com.nvlad.yii2support;

import com.intellij.core.*;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.config.PhpProjectSharedConfiguration;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.parser.PhpParserDefinition;
import com.jetbrains.php.lang.psi.elements.*;
import com.nvlad.yii2support.common.*;
import com.nvlad.yii2support.relations.*;
import java.util.*;

/** Real PhpStorm PHP parser/PSI in IntelliJ's Core test environment. No IDE UI or full index. */
public final class PhpPsiRegressionTest {
    private static int checks;
    private static com.intellij.mock.MockProject project;
    private static final Map<String,PhpClass> classes = new HashMap<>();
    private static final YiiModelResolver resolver = new YiiModelResolver(name -> classes.containsKey(name) ? List.of(classes.get(name)) : List.of());
    private static void eq(String test,Object expected,Object actual) {
        checks++;
        if (!Objects.equals(expected,actual)) throw new AssertionError(test+": expected "+expected+", got "+actual);
    }
    private static PsiFile php(String code) {
        PsiFile file=PsiFileFactory.getInstance(project).createFileFromText("fixture.php",PhpFileType.INSTANCE,"<?php "+code);
        for (PhpClass clazz:PsiTreeUtil.findChildrenOfType(file,PhpClass.class)) classes.put(clazz.getFQN(),clazz);
        SmartPointerManager.getInstance(project).createSmartPsiElementPointer(file);
        return file;
    }
    private static MethodReference call(PsiElement file,String name) {
        return PsiTreeUtil.findChildrenOfType(file,MethodReference.class).stream().filter(c->name.equals(c.getName())).findFirst().orElseThrow();
    }
    private static StringLiteralExpression literal(PsiFile file,String value) {
        return PsiTreeUtil.findChildrenOfType(file,StringLiteralExpression.class).stream().filter(c->value.equals(c.getContents())).findFirst().orElseThrow();
    }
    private static List<String> names(List<YiiModelResolver.Relation> relations) { return relations.stream().map(YiiModelResolver.Relation::name).sorted().toList(); }
    private static List<String> modelNames(PsiElement expression) { return resolver.models(expression).stream().map(PhpClass::getName).sorted().toList(); }
    public static void main(String[] args) {
        var disposable=Disposer.newDisposable();
        try {
            var app=new CoreApplicationEnvironment(disposable);
            app.registerFileType(PhpFileType.INSTANCE,"php"); app.registerParserDefinition(new PhpParserDefinition());
            app.addExplicitExtension(ElementManipulators.INSTANCE,StringLiteralExpression.class,new com.jetbrains.php.lang.psi.manipulators.StringLiteralManipulator());
            CoreApplicationEnvironment.registerApplicationExtensionPoint(com.intellij.psi.impl.source.tree.TreeCopyHandler.EP_NAME,com.intellij.psi.impl.source.tree.TreeCopyHandler.class);
            project=new CoreProjectEnvironment(disposable,app).getProject();
            Disposer.register(disposable,project);
            project.registerService(PhpProjectSharedConfiguration.class,new PhpProjectSharedConfiguration());
            project.registerService(com.intellij.pom.tree.TreeAspect.class,new com.intellij.pom.tree.TreeAspect());
            project.registerService(com.intellij.pom.PomModel.class,new com.intellij.pom.core.impl.PomModelImpl(project));
            arguments(); modelsAndRelations(); contexts(); references(); arrayMode(); incomplete();
            System.out.println("PASS: "+checks+" real PHP PSI regression checks");
        } finally { Disposer.dispose(disposable); }
    }
    private static void arguments() {
        var call=call(php("$this->render(params: compact('model'), view: 'index');"),"render");
        eq("named view reordered","'index'",PhpArguments.get(call,"view",0).getText());
        eq("named params reordered","compact('model')",PhpArguments.get(call,"params",1).getText());
        call=call(php("$this->render(params: []);"),"render");
        eq("missing named view has no positional fallback",null,PhpArguments.get(call,"view",0));
        call=call(php("$this->render('index', params: /* comment */ []);"),"render");
        eq("mixed arguments","'index'",PhpArguments.get(call,"view",0).getText());
        eq("commented named argument","[]",PhpArguments.get(call,"params",1).getText());
        call=call(php("$this->render();"),"render");
        eq("empty arguments",null,PhpArguments.get(call,"params",1));
        var config=php("namespace app; use app\\services\\Mailer as Mail; return ['class' => Mail::class];");
        eq("imported class alias","\\app\\services\\Mailer",PhpArrays.className(PsiTreeUtil.findChildOfType(config,ClassConstantReference.class)));
    }
    private static void modelsAndRelations() {
        php("namespace yii\\db; class BaseActiveRecord {} class ActiveRecord extends BaseActiveRecord {} class ActiveQuery {}");
        php("""
            namespace app;
            use yii\\db\\ActiveRecord;
            class Product extends ActiveRecord {}
            class Item extends ActiveRecord {
                public function getProduct() { return $this->hasOne(Product::class, ['id' => 'product_id']); }
            }
            class Order extends ActiveRecord {
                public function getItems() { return $this->hasMany(Item::class, ['order_id' => 'id'])->orderBy('id'); }
            }
            class User extends ActiveRecord {
                public function getOrders() { return $this->hasMany(Order::class, ['user_id' => 'id']); }
                public function getDisplayName() { return 'plain getter'; }
                protected function getSecret() { return $this->hasMany(Order::class, []); }
                public static function getStaticOrders() { return 'not a relation'; }
                public function getRequiresParameter($id) { return $this->hasOne(Order::class, []); }
                public function getClosure() { return function () { return $this->hasMany(Order::class, []); }; }
            }
            class ChildUser extends User { public function getOrders() { return []; } }
            class Other { public function getOrders() { return $this->hasMany(Order::class, []); } }
            """);
        php("namespace app; class Category extends User { public function getParent() { return $this->hasOne(self::class, []); } public function getChildren() { return $this->hasMany(static::class, []); } }");
        eq("self and static relations", List.of("children", "orders", "parent"), names(resolver.relations(classes.get("\\app\\Category"))));
        var user=classes.get("\\app\\User");
        eq("record inheritance",true,resolver.inherits(user,"\\yii\\db\\BaseActiveRecord"));
        eq("only real public relation getters",List.of("orders"),names(resolver.relations(user)));
        eq("nested relation path",List.of("product"),names(resolver.atPath(List.of(user),"orders.items")));
        eq("missing intermediate relation",List.of(),names(resolver.atPath(List.of(user),"orders.missing")));
        eq("override hides parent relation",List.of(),names(resolver.relations(classes.get("\\app\\ChildUser"))));
        eq("unrelated class",List.of(),names(resolver.relations(classes.get("\\app\\Other"))));
        var file=php("namespace app; User::find()->where(['id' => 1])->with('orders');");
        eq("static AR query chain",List.of("User"),modelNames(call(file,"with").getClassReference()));
        file=php("namespace app; $query = User::find(); $query->with('orders');");
        eq("query in local variable",List.of("User"),modelNames(call(file,"with").getClassReference()));
        file=php("namespace app; $query = User::find(); if ($test) {$query = Order::find();} $query->with('orders');");
        eq("ambiguous branch does not invent model",List.of(),modelNames(call(file,"with").getClassReference()));
        file=php("namespace app; $query = new \\yii\\db\\ActiveQuery(User::class); $query->with('orders');");
        eq("explicit ActiveQuery modelClass",List.of("User"),modelNames(call(file,"with").getClassReference()));
        file=php("namespace app; $user = new User(); $user->getOrders()->with('items');");
        eq("relation query model",List.of("Order"),modelNames(call(file,"with").getClassReference()));
        file=php("namespace app; $a=$b; $b=$a; $b->with('orders');");
        eq("cyclic local variables terminate",List.of(),modelNames(call(file,"with").getClassReference()));
        file=php("namespace app; $query=User::find(); $query=$query->where([]); $query->with('orders');");
        eq("self assignment preserves previous query",List.of("User"),modelNames(call(file,"with").getClassReference()));
    }
    private static void contexts() {
        var file=php("namespace app; User::find()->joinWith(['orders o' => function($q) { $q->where(['status'=>'active']); }], false, 'LEFT JOIN');");
        var entry = PsiTreeUtil.getParentOfType(literal(file,"orders o"),ArrayHashElement.class);
        eq("array key extraction", "orders o", PhpArrays.string(entry.getKey()));
        eq("JOIN alias context",true,RelationContext.of(literal(file,"orders o")).allowAlias());
        eq("SQL key is not relation",null,RelationContext.of(literal(file,"status")));
        eq("SQL value is not relation",null,RelationContext.of(literal(file,"active")));
        eq("joinType is not relation",null,RelationContext.of(literal(file,"LEFT JOIN")));
        file=php("namespace app; User::find()->with(['orders' => ['notRelation']]);");
        eq("callback nested value is not relation",null,RelationContext.of(literal(file,"notRelation")));
        eq("callback array key is relation",true,RelationContext.of(literal(file,"orders")) != null);
        file=php("namespace app; User::find()->with(['orders' => 'callbackName']);");
        eq("callback value is not relation",null,RelationContext.of(literal(file,"callbackName")));
        file=php("namespace app; User::find()->with('orders', 'orders.items');");
        eq("variadic with",true,RelationContext.of(literal(file,"orders.items")) != null);
        file=php("namespace app; User::find()->joinWith(eagerLoading: true, with: ['orders']);");
        eq("named joinWith",true,RelationContext.of(literal(file,"orders")) != null);
        file=php("namespace app; class PivotUser extends User { public function getProducts() { return $this->hasMany(Product::class,[])->via('orders'); } }");
        eq("via uses declaring model",List.of("PivotUser"),modelNames(RelationContext.of(literal(file,"orders")).modelExpression()));
    }
    private static void references() {
        var file = php("namespace app; User::find()->joinWith(['orders.items.product AS p']);");
        var str = literal(file,"orders.items.product AS p");
        var references = RelationReferenceContributor.references(str,resolver);
        eq("one reference per segment",3,references.length);
        eq("first segment range", "orders", references[0].getRangeInElement().substring(str.getText()));
        eq("last segment range excludes alias", "product", references[2].getRangeInElement().substring(str.getText()));
        for (int i=0;i<references.length;i++) {
            var target = references[i].resolve();
            eq("segment target "+i, List.of("getOrders","getItems","getProduct").get(i), target instanceof Method m ? m.getName() : null);
        }
        final var ref = references[1];
        var renamed = ref.handleElementRename("getLines");
        eq("rename preserves other segments and JOIN alias", "'orders.lines.product AS p'", renamed.getText());
        file = php("namespace app; Other::find()->with('orders');");
        eq("unrelated receiver has no references",0,RelationReferenceContributor.references(literal(file,"orders"),resolver).length);
    }
    private static void arrayMode() {
        var provider = new com.nvlad.yii2support.typeprovider.ActiveRecordTypeProvider();
        for (String chain : List.of("asArray()", "asArray(true)", "asArray(value: true)", "asArray($flag)", "asArray(false)->asArray()")) {
            var file=php("namespace app; User::find()->"+chain+"->one();");
            eq("array result stays with native provider: "+chain,null,provider.getType(call(file,"one")));
        }
        var file=php("namespace app; User::find()->asArray()->asArray(value: false)->one();");
        eq("last asArray(false) restores object mode",false,com.nvlad.yii2support.typeprovider.ActiveRecordTypeProvider.usesArrayResult(call(file,"one")));
    }
    private static void incomplete() {
        for (String code:List.of("$x->with('","$x->with(['orders' =>", "$this->hasMany(", "$this->render(params:","$x->joinWith(['orders.","$a=$a; $a->with('x');")) {
            var file=php(code);
            for (var str:PsiTreeUtil.findChildrenOfType(file,StringLiteralExpression.class)) RelationContext.of(str);
            for (var ref:PsiTreeUtil.findChildrenOfType(file,MethodReference.class)) resolver.models(ref.getClassReference());
            eq("incomplete PHP handled: "+code,true,true);
        }
    }
}
