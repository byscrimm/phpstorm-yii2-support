package io.github.byscrimm.yii2insight;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ApplicationStarter;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.elements.*;
import io.github.byscrimm.yii2insight.common.*;
import java.util.*;

/** Runs in a disposable PhpStorm profile, never included in the release plugin. */
public final class IdeSmokeTest implements ApplicationStarter {
    private static int checks;
    @Override public boolean isHeadless() { return true; }
    @Override public int getRequiredModality() { return NOT_IN_EDT; }
    private static void eq(Object expected, Object actual) {
        checks++;
        if (!Objects.equals(expected,actual)) throw new AssertionError("Expected " + expected + ", got " + actual);
    }
    @Override public void main(List<String> args) {
        try {
            Project project = ProjectManager.getInstance().getDefaultProject();
            ReadAction.run(() -> {
                PsiFile file = PsiFileFactory.getInstance(project).createFileFromText("test.php", PhpFileType.INSTANCE,
                    "<?php namespace app; use app\\services\\Mailer; return ['components' => ['mail' => ['class' => Mailer::class], 'cache' => 'app\\Cache']];");
                Collection<ArrayHashElement> hashes = PsiTreeUtil.findChildrenOfType(file, ArrayHashElement.class);
                eq(4, hashes.size());
                ArrayHashElement mail = hashes.stream().filter(h -> "mail".equals(PhpArrays.string(h.getKey()))).findFirst().orElseThrow();
                eq("\\app\\services\\Mailer", PhpArrays.className(mail.getValue()));
                ArrayHashElement cache = hashes.stream().filter(h -> "cache".equals(PhpArrays.string(h.getKey()))).findFirst().orElseThrow();
                eq("app\\Cache", PhpArrays.className(cache.getValue()));
                PsiFile action = PsiFileFactory.getInstance(project).createFileFromText("Controller.php", PhpFileType.INSTANCE,
                    "<?php class SiteController { public function actions() { return ['index' => ['class' => IndexAction::class]]; } }");
                Method method = PsiTreeUtil.findChildOfType(action, Method.class);
                eq(true, PhpArrays.returnedArray(method) != null);
                eq(true, io.github.byscrimm.yii2insight.utils.Yii2InsightSettings.getInstance(project) != null);
            });
            ApplicationManager.getApplication().invokeAndWait(() -> {
                eq(true, new io.github.byscrimm.yii2insight.ui.settings.SettingsForm(project).createComponent() != null);
                eq(true, new io.github.byscrimm.yii2insight.views.settings.ViewSettings(project).createComponent() != null);
                eq(true, new io.github.byscrimm.yii2insight.database.settings.SettingsForm(project).createComponent() != null);
            });
            System.out.println("YII2_IDE_SMOKE_PASS " + checks + " checks");
            System.exit(0);
        } catch (Throwable e) {
            e.printStackTrace(); System.out.println("YII2_IDE_SMOKE_FAIL"); System.exit(1);
        }
    }
}
