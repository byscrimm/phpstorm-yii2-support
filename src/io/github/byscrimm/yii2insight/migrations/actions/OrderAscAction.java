package io.github.byscrimm.yii2insight.migrations.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Toggleable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.AnActionButton;
import io.github.byscrimm.yii2insight.migrations.ui.toolWindow.MigrationPanel;
import io.github.byscrimm.yii2insight.utils.Yii2InsightSettings;

@SuppressWarnings("ComponentNotRegistered")
public class OrderAscAction extends AnActionButton implements Toggleable {
    public OrderAscAction() {
        super("Newest migrations first", AllIcons.RunConfigurations.SortbyDuration);

//        if (SystemInfo.isMac) {
//            setShortcut(CustomShortcutSet.fromString("meta S"));
//        } else {
//            setShortcut(CustomShortcutSet.fromString("ctrl S"));
//        }
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        final Yii2InsightSettings settings = Yii2InsightSettings.getInstance(anActionEvent.getProject());
        settings.newestFirst = !settings.newestFirst;
        anActionEvent.getPresentation().putClientProperty(SELECTED_PROPERTY, settings.newestFirst);

        MigrationPanel panel = (MigrationPanel) getContextComponent();
        ApplicationManager.getApplication().invokeLater(() -> {
            panel.updateTree();
            panel.updateUI();
        });
    }

    @Override
    public void updateButton(AnActionEvent e) {
        final Yii2InsightSettings settings = Yii2InsightSettings.getInstance(e.getProject());
        e.getPresentation().putClientProperty(SELECTED_PROPERTY, settings.newestFirst);

        super.updateButton(e);
    }
}
