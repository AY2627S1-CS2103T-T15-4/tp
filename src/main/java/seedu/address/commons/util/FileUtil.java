package seedu.address.commons.util;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Writes and reads files
 */
public class FileUtil {

    private static final String CHARSET = "UTF-8";

    /**
     * Creates a file if it does not exist along with its missing parent directories.
     * @throws IOException if the file or directory cannot be created.
     */
    public static void createIfMissing(Path file) throws IOException {
        if (Files.exists(file)) {
            return;
        }

        createParentDirsOfFile(file);

        Files.createFile(file);
    }

    /**
     * Creates parent directories of file if it has a parent directory
     */
    private static void createParentDirsOfFile(Path file) throws IOException {
        Path parentDir = file.getParent();

        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
    }

    /**
     * Assumes file exists
     */
    public static String readFromFile(Path file) throws IOException {
        return new String(Files.readAllBytes(file), CHARSET);
    }

    /**
     * Writes a complete sibling file before replacing the destination, atomically where supported.
     * A non-atomic replacement is used only when the file system rejects atomic moves.
     * Existing symbolic links are preserved; dangling links fail without replacement.
     */
    public static void writeToFile(Path file, String content) throws IOException {
        writeToFile(file, content, Files::move);
    }

    /** Allows deterministic testing of file-system replacement failures. */
    static void writeToFile(Path file, String content, FileMover mover) throws IOException {
        Path destination = Files.isSymbolicLink(file) ? file.toRealPath() : file.toAbsolutePath();
        createParentDirsOfFile(destination);
        Path temporary = Files.createTempFile(destination.getParent(), ".watchdog-", ".tmp");
        try {
            Files.write(temporary, content.getBytes(CHARSET));
            try {
                mover.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException | FileAlreadyExistsException e) {
                mover.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    @FunctionalInterface
    interface FileMover {
        void move(Path source, Path destination, CopyOption... options) throws IOException;
    }
}
