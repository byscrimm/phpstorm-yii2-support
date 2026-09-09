package io.github.byscrimm.yii2insight.migrations.services;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.SmartList;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import io.github.byscrimm.yii2insight.common.FileUtil;
import io.github.byscrimm.yii2insight.common.YiiApplicationUtils;
import io.github.byscrimm.yii2insight.migrations.entities.DefaultMigrateCommand;
import io.github.byscrimm.yii2insight.migrations.entities.MigrateCommand;
import io.github.byscrimm.yii2insight.migrations.entities.MigrateCommandComparator;
import io.github.byscrimm.yii2insight.migrations.entities.Migration;
import io.github.byscrimm.yii2insight.utils.Yii2InsightSettings;

import java.util.*;

public class MigrationService {
    public static MigrationService getInstance(Project project) {
        return project.getService(MigrationService.class);
    }

    private final Project myProject;
    private final PhpIndex myPhpIndex;
    private final int baseUrlLength;
    private Map<MigrateCommand, Collection<Migration>> myMigrationMap;
    private List<Migration> myMigrations = List.of();
    private Set<MigrationServiceListener> listeners;

    public MigrationService(Project project) {
        myProject = project;
        myPhpIndex = PhpIndex.getInstance(project);
        listeners = new java.util.concurrent.CopyOnWriteArraySet<>();

        String projectRootUrl = YiiApplicationUtils.getYiiRootUrl(project);
        baseUrlLength = projectRootUrl != null ? projectRootUrl.length() : 0;
    }


    public Map<MigrateCommand, Collection<Migration>> getMigrationCommandMap() {
        if (myMigrationMap == null) {
            myMigrationMap = new HashMap<>();
            sync();
        }

        return myMigrationMap;
    }

    public synchronized void sync() {
        if (myProject.isDisposed()) return;
        ApplicationManager.getApplication().runReadAction(this::_sync);
    }

    public void _sync() {
        Collection<PhpClass> migrations;
        try {
            migrations = myPhpIndex.getAllSubclasses("\\yii\\db\\MigrationInterface");
        } catch (IndexNotReadyException e) {
            return;
        }

        List<MigrateCommand> commands = new SmartList<>(Yii2InsightSettings.getInstance(myProject).migrateCommands);
        commands.add(new DefaultMigrateCommand(commands));
        commands.sort(new MigrateCommandComparator());

        Map<MigrateCommand, Collection<Migration>> migrationMap = new HashMap<>();
        List<Migration> migrationList = new SmartList<>();
        for (MigrateCommand command : commands) {
            migrationMap.put(command, new SmartList<>());
        }

        for (PhpClass migrationClass : migrations) {
            if (migrationClass.isAbstract() || !Migration.isValidMigrationClass(migrationClass)) {
                continue;
            }

            VirtualFile virtualFile = FileUtil.getVirtualFile(migrationClass.getContainingFile());
            String rootUrl = YiiApplicationUtils.getYiiRootUrl(myProject);
            if (virtualFile == null || rootUrl == null || !virtualFile.getUrl().startsWith(rootUrl + "/")) {
                continue;
            }

            String path = virtualFile.getUrl().substring(rootUrl.length() + 1);
            int pathLength = path.length();
            path = path.substring(0, pathLength - virtualFile.getName().length() - 1);
            Migration migration = getMigrationForClass(migrationClass, path);
            for (MigrateCommand command : commands) {
                if (command.containsMigration(myProject, migration)) {
                    migrationMap.get(command).add(migration);
                    migrationList.add(migration);
                    break;
                }
            }
        }

        if (!migrationMap.equals(myMigrationMap)) {
            myMigrationMap = migrationMap;
            myMigrations = migrationList;

            for (MigrationServiceListener listener : listeners) {
                listener.treeChanged();
            }
        }
    }

    public List<Migration> getMigrations() {
        return myMigrations;
    }

    public void addListener(MigrationServiceListener listener) {
        listeners.add(listener);
    }

    public void removeListener(MigrationServiceListener listener) {
        listeners.remove(listener);
    }

    private Migration getMigrationForClass(PhpClass phpClass, String path) {
        if (myMigrations != null) {
            for (Migration migration : myMigrations) {
                if (migration.migrationClass.equals(phpClass)) {
                    return migration;
                }
            }
        }

        return new Migration(phpClass, path);
    }
}
