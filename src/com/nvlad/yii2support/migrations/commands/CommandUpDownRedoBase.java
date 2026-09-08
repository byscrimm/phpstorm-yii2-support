package com.nvlad.yii2support.migrations.commands;

import com.intellij.database.dataSource.DataSourceUiUtil;
import com.intellij.database.dataSource.LocalDataSource;
import com.intellij.database.psi.DbDataSource;
import com.intellij.database.psi.DbPsiFacade;
import com.intellij.database.util.DbImplUtil;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.nvlad.yii2support.common.YiiCommandLineUtil;
import com.nvlad.yii2support.migrations.entities.DefaultMigrateCommand;
import com.nvlad.yii2support.migrations.entities.MigrateCommand;
import com.nvlad.yii2support.migrations.entities.Migration;
import com.nvlad.yii2support.migrations.entities.MigrationStatus;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class CommandUpDownRedoBase extends CommandBase {


    final String myPath;
    final List<Migration> myMigrations;
    String direction = null;
    private Map<String, DefaultMutableTreeNode> myMigrationNodeMap;
    private Map<Migration, MigrationStatus> myMigrationStatusMap;

    CommandUpDownRedoBase(@NotNull Project project, @NotNull List<Migration> migrations, @NotNull MigrateCommand command, String path) {
        super(project, command);

        myPath = path;
        myMigrations = migrations;
    }

    @Override
    void processOutput(String text) {
        MigrationOutput.Event event = MigrationOutput.parse(text);
        if (event != null) {
            Migration migration = findMigration(event.namespace(), event.name());
            if (migration == null) {
                return;
            }

            switch (event.status()) {
                case "applying":
                    migration.status = MigrationStatus.Progress;
                    migration.applyAt = null;
                    migration.upDuration = null;

                    direction = "applying";
                    break;
                case "applied":
                    migration.status = MigrationStatus.Success;
                    migration.upDuration = event.duration();
                    break;
                case "failed to apply":
                    migration.status = MigrationStatus.ApplyError;
                    break;
                case "reverting":
                    migration.status = MigrationStatus.Progress;
                    migration.applyAt = null;
                    migration.upDuration = null;
                    migration.downDuration = null;

                    direction = "reverting";
                    break;
                case "reverted":
                    migration.status = MigrationStatus.NotApply;
                    migration.downDuration = event.duration();
                    break;
                case "failed to revert":
                    migration.status = MigrationStatus.RollbackError;
                    break;
            }

            repaintMigrationNode(migration);
        }
    }

    void executeActionWithParams(String action, List<String> parameters) {
        try {
            String command = myCommand.command + "/" + action;
            ProcessHandler processHandler = YiiCommandLineUtil.configureHandler(myProject, command, parameters);
            if (processHandler == null) {
                return;
            }

            myMigrationStatusMap = new HashMap<>();
            for (Migration migration : myMigrations) {
                myMigrationStatusMap.put(migration, migration.status);
            }

            executeProcess(processHandler);

            setErrorStatusForMigrationInProgress(action);
        } catch (ExecutionException e) {
            YiiCommandLineUtil.processError(e);
        }

        syncDataSources();
    }

    DefaultMutableTreeNode findTreeNode(Migration migration) {
        if (myComponent instanceof JTree) {
            return getMigrationNodeMap().get(migration.name);
        }

        return null;
    }

    private Map<String, DefaultMutableTreeNode> getMigrationNodeMap() {
        if (myMigrationNodeMap == null) {
            myMigrationNodeMap = new HashMap<>();
            buildMigrationNodeMap((DefaultMutableTreeNode) ((JTree) myComponent).getModel().getRoot());
        }

        return myMigrationNodeMap;
    }

    private void buildMigrationNodeMap(DefaultMutableTreeNode parentNode) {
        Enumeration pathEnumeration = parentNode.children();
        while (pathEnumeration.hasMoreElements()) {
            DefaultMutableTreeNode node = ((DefaultMutableTreeNode) pathEnumeration.nextElement());
            if (node.getUserObject() instanceof Migration) {
                myMigrationNodeMap.put(((Migration) node.getUserObject()).name, node);
            }
        }
    }

    private void setErrorStatusForMigrationInProgress(String action) {
        if (myMigrations.size() > 0) {
            for (Migration migration : myMigrations) {
                if (isInvalidMigrationStatus(migration, action)) {
                    if ("reverting".equals(direction)) {
                        migration.status = MigrationStatus.RollbackError;
                    } else {
                        migration.status = MigrationStatus.ApplyError;
                    }
                }
            }
        }
    }

    private boolean isInvalidMigrationStatus(Migration migration, String action) {
        if (migration.status == MigrationStatus.Progress) {
            return true;
        }

        if (action.equals("redo") && migration.status != myMigrationStatusMap.get(migration)) {
            return true;
        }

        return !action.equals("redo") && migration.status == myMigrationStatusMap.get(migration);

    }

    private void syncDataSources() {
        if (myProject.isDisposed()) return;
        String selected = com.nvlad.yii2support.utils.Yii2SupportSettings.getInstance(myProject).dataSourceId;
        DbPsiFacade facade = DbPsiFacade.getInstance(myProject);
        for (DbDataSource dataSource : facade.getDataSources()) {
            if (!selected.isEmpty() && !selected.equals(dataSource.getUniqueId())) continue;
            if (dataSource.getDelegate() instanceof LocalDataSource) {
                if (DbImplUtil.isConnected(dataSource)) {
                    LocalDataSource localDataSource = (LocalDataSource) dataSource.getDelegate();
                    com.intellij.database.util.DataSourceUtilKt.performAutoSyncTask(
                            com.intellij.database.util.LoaderContext.selectGeneralTask(myProject, localDataSource), false);
                }
            }
        }
    }

    private Migration findMigration(String namespace, String name) {
        for (Migration migration : myMigrations) {
            if (StringUtil.equals(name, migration.name) && StringUtil.equals(namespace, migration.namespace)) {
                return migration;
            }
        }

        return null;
    }

    void prepareCommandParams(List<String> params, MigrateCommand command, String path) {
        boolean useNamespaces = myMigrations.stream().anyMatch(migration -> !migration.namespace.equals("\\"));
        super.prepareCommandParams(params, useNamespaces ? "@vendor" : path);
        if (command instanceof DefaultMigrateCommand && useNamespaces) {
            Set<String> namespaces = new HashSet<>();
            for (Migration migration : myMigrations) {
                if (!migration.namespace.equals("\\")) {
                    namespaces.add(migration.namespace);
                }
            }

            params.add("--migrationNamespaces=" + StringUtil.join(namespaces, ","));
        }
    }
}
