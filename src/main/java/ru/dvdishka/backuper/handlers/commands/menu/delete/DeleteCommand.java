package ru.dvdishka.backuper.handlers.commands.menu.delete;

import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.command.CommandSender;
import ru.dvdishka.backuper.backend.utils.*;
import ru.dvdishka.backuper.handlers.commands.Command;
import ru.dvdishka.backuper.handlers.commands.status.StatusCommand;

import java.io.File;
import java.util.Objects;

public class DeleteCommand extends Command implements Task {

    private String taskName = "DeleteBackup";
    private long maxProgress = 0;
    private volatile long currentProgress = 0;

    private boolean isDeleteSuccessful = true;

    public DeleteCommand(CommandSender sender, CommandArguments arguments) {
        super(sender, arguments);
    }

    @Override
    public void execute() {

        String backupName = (String) arguments.get("backupName");

        if (!Backup.checkBackupExistenceByName(backupName)) {
            cancelButtonSound();
            returnFailure("Backup does not exist!");
            return;
        }

        normalButtonSound();

        Backup backup = new Backup(backupName);

        if (Backup.isLocked() || Backup.isLocked()) {
            cancelButtonSound();
            returnFailure("Blocked by another operation!");
            return;
        }

        File backupFile = backup.getFile();

        Backup.lock(this);
        maxProgress = backup.getByteSize();

        StatusCommand.sendTaskStartedMessage("Delete", sender);

        Logger.getLogger().log("The Delete Backup process has been started, it may take some time...", sender);

        if (backup.zipOrFolder().equals("(ZIP)")) {

            Scheduler.getScheduler().runAsync(Utils.plugin, () -> {
                if (backupFile.delete()) {
                    Logger.getLogger().log("The Delete Backup process has been finished successfully", sender);
                } else {
                    Logger.getLogger().warn("Backup " + backupName + " can not be deleted!", sender);
                }
                Backup.unlock();
            });

        } else {

            Scheduler.getScheduler().runAsync(Utils.plugin, () -> {
                deleteDir(backupFile);
                if (!isDeleteSuccessful) {
                    Logger.getLogger().warn("The Delete Backup process has been finished with an exception!", sender);
                } else {
                    Logger.getLogger().log("The Delete Backup process has been finished successfully", sender);
                }
                backup.unlock();
            });
        }
    }

    public void deleteDir(File dir) {

        if (dir != null && dir.listFiles() != null) {

            for (File file : Objects.requireNonNull(dir.listFiles())) {

                if (file.isDirectory()) {

                    deleteDir(file);

                } else {

                    incrementCurrentProgress(Utils.getFolderOrFileByteSize(file));

                    if (!file.delete()) {

                        isDeleteSuccessful = false;
                        Logger.getLogger().warn("Can not delete file " + file.getName(), sender);
                    }
                }
            }
            if (!dir.delete()) {

                isDeleteSuccessful = false;
                Logger.getLogger().warn("Can not delete directory " + dir.getName(), sender);
            }
        }
    }

    private synchronized void incrementCurrentProgress(long progress) {
        currentProgress += progress;
    }

    @Override
    public String getTaskName() {
        return taskName;
    }

    @Override
    public long getTaskProgress() {
        return (long) (((double) currentProgress) / ((double) maxProgress) * 100.0);
    }
}
