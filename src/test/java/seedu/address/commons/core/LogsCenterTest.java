package seedu.address.commons.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.testutil.ApplicationClassLoader;

public class LogsCenterTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void initialization_missingLogDirectory_warnsAndKeepsConsoleLogging() throws Exception {
        Path missingHome = temporaryFolder.resolve("missing");
        ClassLoader loader = new ApplicationClassLoader(missingHome.resolve("WatchDog.jar").toUri().toURL());
        Logger baseLogger = Logger.getLogger("watchdog");
        Handler[] originalHandlers = baseLogger.getHandlers();
        Level originalLevel = baseLogger.getLevel();
        boolean originalUseParentHandlers = baseLogger.getUseParentHandlers();
        var originalFilter = baseLogger.getFilter();
        List<LogRecord> records = new ArrayList<>();
        try {
            baseLogger.setFilter(record -> {
                records.add(record);
                return true;
            });
            Class<?> logsCenter = Class.forName(LogsCenter.class.getName(), true, loader);
            assertFalse(Files.exists(missingHome));
            assertEquals(1, baseLogger.getHandlers().length);
            assertTrue(baseLogger.getHandlers()[0] instanceof ConsoleHandler);
            assertTrue(records.stream().anyMatch(record -> record.getLevel() == Level.WARNING
                    && record.getMessage().contains("Could not create an application log in " + missingHome)));
            Logger logger = (Logger) logsCenter.getMethod("getLogger", String.class).invoke(null, "recovery");
            assertEquals(baseLogger, logger.getParent());
            assertTrue(logger.isLoggable(Level.INFO));
        } finally {
            for (Handler handler : baseLogger.getHandlers()) {
                baseLogger.removeHandler(handler);
                handler.close();
            }
            for (Handler handler : originalHandlers) {
                baseLogger.addHandler(handler);
            }
            baseLogger.setFilter(originalFilter);
            baseLogger.setLevel(originalLevel);
            baseLogger.setUseParentHandlers(originalUseParentHandlers);
        }
    }
}
