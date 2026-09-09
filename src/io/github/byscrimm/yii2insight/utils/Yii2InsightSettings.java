package io.github.byscrimm.yii2insight.utils;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.SmartList;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import io.github.byscrimm.yii2insight.common.YiiApplicationTemplate;
import io.github.byscrimm.yii2insight.common.YiiApplicationUtils;
import io.github.byscrimm.yii2insight.migrations.entities.MigrateCommand;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by oleg on 2017-09-06.
 */
@State(name = "Yii2 Insight", storages = @Storage("yii2-insight.xml"))
public class Yii2InsightSettings implements PersistentStateComponent<Yii2InsightSettings> {
    // Yii Settings
    public String yiiRootPath = null;

    // Database Settings
    public String tablePrefix = "";
    public boolean insertWithTablePrefix = false;
    public String dataSourceId = "";

    // View Settings
    public String defaultViewExtension = "php";
    public String defaultViewClass = "\\yii\\web\\View";
    @MapAnnotation(sortBeforeSave = false)
    public Map<String, String> viewPathMap;

    // Migrations
    public boolean newestFirst = false;
    public List<MigrateCommand> migrateCommands;

    // Aliases
    @MapAnnotation(sortBeforeSave = false)
    public Map<String, String> aliasMap;

    private Project myProject;

    public Yii2InsightSettings() {
        viewPathMap = new LinkedHashMap<>();
        viewPathMap.put("@app/themes/*/modules", "@app/modules");
        viewPathMap.put("@app/themes/*/widgets", "@app/widgets");
        viewPathMap.put("@app/themes/*", "@app/views");

        migrateCommands =  new SmartList<>();
        aliasMap = new HashMap<>();
    }

    public Yii2InsightSettings(Project project) {
        this();

        myProject = project;
        initProjectConfiguration(project);
    }

    @Nullable
    @Override
    public Yii2InsightSettings getState() {
        return this;
    }

    @Override
    public void loadState(Yii2InsightSettings settings) {
        if (myProject != null) {
            this.aliasMap.clear();
            this.migrateCommands.clear();
            settings.initProjectConfiguration(myProject);
        }

        XmlSerializerUtil.copyBean(settings, this);
    }

    public void initProjectConfiguration(Project project) {
        YiiApplicationUtils.resetYiiRootPath(project);
        YiiApplicationTemplate template = YiiApplicationUtils.getAppTemplate(project, yiiRootPath);
        if (aliasMap.isEmpty()) {
            aliasMap.put("@vendor", "vendor");
            aliasMap.put("@runtime", "@app/runtime");
            aliasMap.put("@webroot", "@app/web");
            switch (template) {
                case Unknown:
                case Basic:
                    aliasMap.put("@yii2-insight-console-command-app-root", "");
                    break;
                case StarterKit:
                    aliasMap.put("@base", "");
                    aliasMap.put("@api", "api");
                    aliasMap.put("@storage", "storage");
                case Advanced:
                    aliasMap.put("@yii2-insight-console-command-app-root", "@console");
                    aliasMap.put("@common", "common");
                    aliasMap.put("@frontend", "frontend");
                    aliasMap.put("@backend", "backend");
                    aliasMap.put("@console", "console");
                    break;
            }
        }

        if (migrateCommands.isEmpty()) {
            MigrateCommand command;
            switch (template) {
                case Unknown:
                case Basic:
                case Advanced:
                    command = new MigrateCommand();
                    command.command = "migrate";
                    command.migrationPath.add("@app/migrations");
                    command.migrationTable = "{{%migration}}";
                    command.db = "db";
                    command.isDefault = true;
                    command.useTablePrefix = false;
                    migrateCommands.add(command);
                    break;
                case StarterKit:
                    command = new MigrateCommand();
                    command.command = "migrate";
                    command.migrationPath.add("@common/migrations/db");
                    command.migrationTable = "{{%system_db_migration}}";
                    command.db = "db";
                    command.isDefault = true;
                    command.useTablePrefix = false;
                    migrateCommands.add(command);

                    command = new MigrateCommand();
                    command.command = "rbac-migrate";
                    command.migrationPath.add("@common/migrations/rbac/");
                    command.migrationTable = "{{%system_rbac_migration}}";
                    command.db = "db";
                    command.isDefault = false;
                    command.useTablePrefix = false;
                    migrateCommands.add(command);
                    break;
            }
        }
    }

    public static Yii2InsightSettings getInstance(Project project) {
        return project.getService(Yii2InsightSettings.class);
    }
}
