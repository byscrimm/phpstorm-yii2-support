package io.github.byscrimm.yii2insight.migrations.ui.toolWindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerEx;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import io.github.byscrimm.yii2insight.migrations.services.MigrationService;
import io.github.byscrimm.yii2insight.migrations.services.MigrationServiceListener;
import io.github.byscrimm.yii2insight.migrations.services.MigrationsVirtualFileMonitor;
import io.github.byscrimm.yii2insight.migrations.util.TreeUtil;
import io.github.byscrimm.yii2insight.utils.Yii2InsightSettings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class MigrationsToolWindowFactory implements ToolWindowFactory {
    public static final String TOOL_WINDOW_ID = "Yii2 Insight Migrations";

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        MigrationPanel migrationPanel = new MigrationPanel(project, toolWindow);
        ConsolePanel consolePanel = new ConsolePanel(project);

        project.getMessageBus().connect(project).subscribe(ToolWindowManagerListener.TOPIC,
                new MigrationToolWindowManagerListener(project, migrationPanel.getTree()));

        Content navigator = ContentFactory.getInstance().createContent(migrationPanel, "Explorer", false);
        toolWindow.getContentManager().addContent(navigator);

        Content console = ContentFactory.getInstance().createContent(consolePanel, "Output", false);
        toolWindow.getContentManager().addContent(console);
    }

    class MigrationToolWindowManagerListener implements ToolWindowManagerListener {
        private final Project myProject;
        private final MigrationsVirtualFileMonitor fileMonitor;
        private final VirtualFileSystem fileSystem;
        private final MigrationService service;
        private final MigrationServiceListener serviceListener;
        private boolean myToolWindowVisible = false;

        MigrationToolWindowManagerListener(Project project, JTree tree) {
            myProject = project;
            fileMonitor = new MigrationsVirtualFileMonitor(project);
            fileSystem = com.intellij.openapi.vfs.LocalFileSystem.getInstance();
            service = MigrationService.getInstance(project);
            serviceListener = new ServiceListener(tree, project);
            com.intellij.openapi.util.Disposer.register(project, () -> {
                fileSystem.removeVirtualFileListener(fileMonitor);
                service.removeListener(serviceListener);
            });
        }


        @Override
        public void stateChanged(@NotNull ToolWindowManager toolWindowManager) {
            ToolWindow window = ToolWindowManager
                    .getInstance(myProject)
                    .getToolWindow(MigrationsToolWindowFactory.TOOL_WINDOW_ID);
            if (window == null) {
                return;
            }

            boolean toolWindowVisible = window.isVisible();
            if (myToolWindowVisible != toolWindowVisible) {
                if (myToolWindowVisible) {
                    fileSystem.removeVirtualFileListener(fileMonitor);
                    service.removeListener(serviceListener);
                } else {
                    service.addListener(serviceListener);
                    fileSystem.addVirtualFileListener(fileMonitor);

                    ApplicationManager.getApplication().executeOnPooledThread(service::sync);
                }

                myToolWindowVisible = toolWindowVisible;
            }
        }
    }

    class ServiceListener implements MigrationServiceListener {
        private final JTree myTree;
        private final Project project;
        private boolean myProjectDisposed() { return project.isDisposed(); }
        private final MigrationService service;
        private final Yii2InsightSettings settings;

        ServiceListener(JTree tree, Project project) {
            myTree = tree;
            this.project = project;

            service = MigrationService.getInstance(project);
            settings = Yii2InsightSettings.getInstance(project);
        }

        @Override
        public void treeChanged() {
            ApplicationManager.getApplication().invokeLater(() -> {
                if (!myProjectDisposed()) TreeUtil.updateTree(myTree, service.getMigrationCommandMap(), settings.newestFirst);
            });
        }
    }
}
