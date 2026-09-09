package io.github.byscrimm.yii2insight.migrations.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import io.github.byscrimm.yii2insight.migrations.commands.CommandBase;
import io.github.byscrimm.yii2insight.migrations.commands.MigrationHistory;
import io.github.byscrimm.yii2insight.migrations.entities.DefaultMigrateCommand;
import io.github.byscrimm.yii2insight.migrations.entities.MigrateCommand;
import io.github.byscrimm.yii2insight.migrations.entities.Migration;
import io.github.byscrimm.yii2insight.migrations.services.MigrationService;
import com.intellij.util.SmartList;

import java.util.*;

@SuppressWarnings("ComponentNotRegistered")
public class RefreshAction extends MigrateBaseAction {
    public RefreshAction() {
        super("Refresh migrations", AllIcons.Actions.Refresh);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return;
        }

        MigrationService service = MigrationService.getInstance(project);
        service.sync();

        Map<MigrateCommand, Set<Migration>> migrateCommandMap = new HashMap<>();
        MigrateCommand defaultCommand = null;
        for (MigrateCommand command : service.getMigrationCommandMap().keySet()) {
            migrateCommandMap.put(command, new HashSet<>());
            if (command.isDefault) {
                defaultCommand = command;
            }
        }

        for (Migration migration : service.getMigrations()) {
            for (MigrateCommand command : migrateCommandMap.keySet()) {
                if (command.containsMigration(project, migration)) {
                    if (command instanceof DefaultMigrateCommand) {
                        migrateCommandMap.get(defaultCommand).add(migration);
                    } else {
                        migrateCommandMap.get(command).add(migration);
                    }
                }
            }
        }

        List<CommandBase> commands = new SmartList<>();
        for (MigrateCommand command : migrateCommandMap.keySet()) {
            if (migrateCommandMap.get(command).isEmpty()) {
                continue;
            }

            commands.add(new MigrationHistory(project, command, new ArrayList<>(migrateCommandMap.get(command))));
        }

        executeCommand(project, commands);
    }

    @Override
    public boolean isEnabled() {
        return getTree().isEnabled();
    }
}
