package io.github.byscrimm.yii2insight.common;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.ArrayUtil;
import com.intellij.util.PathMappingSettings;
import com.jetbrains.php.config.commandLine.PhpCommandSettings;
import com.jetbrains.php.config.commandLine.PhpCommandSettingsBuilder;
import com.jetbrains.php.run.remote.PhpRemoteInterpreterManager;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class YiiCommandLineUtil {
    public static GeneralCommandLine create(Project project, String command) throws ExecutionException {
        return create(project, command, (String[]) null);
    }

    public static GeneralCommandLine create(Project project, String command, String... parameters) throws ExecutionException {
        return create(project, command, new java.util.ArrayList<>(parameters == null ? java.util.List.of() : Arrays.asList(parameters)));
    }

    public static GeneralCommandLine create(Project project, String command, List<String> parameters) throws ExecutionException {
        parameters = new java.util.ArrayList<>(parameters);
        parameters.add("--color");

        String yiiRootPath = YiiApplicationUtils.getYiiRootPath(project);
        PhpCommandSettings commandSettings = commandSettings(project, command, parameters);
        GeneralCommandLine commandLine = commandSettings.createGeneralCommandLine();
        commandLine.setWorkDirectory(yiiRootPath);

        return commandLine;
    }

    @Nullable
    public static ProcessHandler configureHandler(Project project, String command, List<String> parameters) throws ExecutionException {
        parameters = new java.util.ArrayList<>(parameters);
        parameters.add("--color");

        PhpCommandSettings commandSettings = commandSettings(project, command, parameters);
        GeneralCommandLine commandLine = commandSettings.createGeneralCommandLine();
        if (commandSettings.isRemote()) {
            PhpRemoteInterpreterManager interpreterManager = PhpRemoteInterpreterManager.getInstance();
            if (interpreterManager == null) throw new ExecutionException("Enable the PHP Remote Interpreter plugin.");
            try {
                return interpreterManager.getRemoteProcessHandler(project, commandSettings.getAdditionalData(),
                        commandLine, false, commandSettings.getAdditionalMappings());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ExecutionException("Yii command interrupted", e);
            }
        }

        return new OSProcessHandler(commandLine);
    }

    public static void processError(Throwable e) {
        com.intellij.openapi.diagnostic.Logger.getInstance(YiiCommandLineUtil.class).warn("Yii command failed", e);
        SwingUtilities.invokeLater(() -> Messages.showErrorDialog(
                e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage(), "Yii Command Failed"));
    }

    private static PhpCommandSettings commandSettings(Project project, String command, List<String> parameters) throws ExecutionException {
        String yiiRootPath = YiiApplicationUtils.getYiiRootPath(project);
        if (yiiRootPath == null) throw new ExecutionException("Set the Yii root directory in PHP > Yii2 Insight.");
        PhpCommandSettings commandSettings = PhpCommandSettingsBuilder.create(project, false);
        if (YiiApplicationUtils.getAppTemplate(project) == YiiApplicationTemplate.StarterKit) {
            commandSettings.setScript(yiiRootPath + "/console/yii");
        } else {
            commandSettings.setScript(yiiRootPath + "/yii");
        }

        commandSettings.setWorkingDir(yiiRootPath);
        commandSettings.addArgument(command);
        commandSettings.addArguments(parameters);

        return commandSettings;
    }
}
