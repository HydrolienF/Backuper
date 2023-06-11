package ru.dvdishka.backuper.common.classes;

import ru.dvdishka.backuper.common.Common;
import ru.dvdishka.backuper.common.ConfigVariables;

public class Logger {

    public static Logger getLogger() {
        return new Logger();
    }

    public void log(String text) {
        Common.plugin.getLogger().info(text);
    }

    public void devLog(String text) {
        if (ConfigVariables.betterLogging) {
            Common.plugin.getLogger().info(text);
        }
    }

    public void warn(String text) {
        Common.plugin.getLogger().warning(text);
    }

    public void devWarn(String text) {
        if (ConfigVariables.betterLogging) {
            Common.plugin.getLogger().warning(text);
        }
    }
}