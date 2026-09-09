package io.github.byscrimm.yii2insight.migrations.commands;

import com.intellij.openapi.project.Project;
import io.github.byscrimm.yii2insight.migrations.entities.MigrateCommand;
import io.github.byscrimm.yii2insight.migrations.entities.Migration;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class MigrationUp extends CommandUpDownRedoBase {
    public MigrationUp(@NotNull Project project, @NotNull List<Migration> migrations, @NotNull MigrateCommand command, String path) {
        super(project, migrations, command, path);
        direction = "applying";
    }

    @Override
    public void run() {
        LinkedList<String> params = new LinkedList<>();
        params.add(String.valueOf(myMigrations.size()));
        prepareCommandParams(params, myCommand, myPath);
        executeActionWithParams("up", params);
    }
}
