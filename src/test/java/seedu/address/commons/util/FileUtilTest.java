package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileUtilTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void writeToFile_missingParentsThenReplacement_preservesCompleteUnicodeContent() throws Exception {
        Path destination = temporaryFolder.resolve("data with spaces").resolve("watchdog.json");
        FileUtil.writeToFile(destination, "Original data");
        assertEquals("Original data", FileUtil.readFromFile(destination));
        String replacement = "{\"name\":\"Cookie 🐕\"}\n";
        FileUtil.writeToFile(destination, replacement);
        assertEquals(replacement, Files.readString(destination));
        try (var files = Files.list(destination.getParent())) {
            assertEquals(List.of(destination), files.toList());
        }
    }

    @Test
    public void writeToFile_replacementFailure_preservesExistingContentsAndCleansTemporaryFile() throws Exception {
        Path destination = Files.createDirectory(temporaryFolder.resolve("occupied"));
        Path original = destination.resolve("original.json");
        Files.writeString(original, "Original records");
        assertThrows(IOException.class, () -> FileUtil.writeToFile(destination, "Replacement records"));
        assertEquals("Original records", Files.readString(original));
        try (var files = Files.list(temporaryFolder)) {
            assertEquals(List.of(destination), files.toList());
        }
    }

    @Test
    public void writeToFile_parentIsFile_preservesOriginal() throws Exception {
        Path parent = temporaryFolder.resolve("parent");
        Files.writeString(parent, "Original records");
        assertThrows(IOException.class, () -> FileUtil.writeToFile(parent.resolve("data.json"), "Replacement"));
        assertEquals("Original records", Files.readString(parent));
    }

    @Test
    public void writeToFile_atomicMoveUnsupported_fallsBackOnlyAfterCompleteWrite() throws Exception {
        Path destination = temporaryFolder.resolve("watchdog.json");
        Files.writeString(destination, "Original records");
        List<List<CopyOption>> attempts = new ArrayList<>();
        FileUtil.writeToFile(destination, "Replacement records", (source, target, options) -> {
            attempts.add(Arrays.asList(options));
            assertEquals(destination, target);
            assertEquals("Original records", Files.readString(target));
            assertEquals("Replacement records", Files.readString(source));
            if (Arrays.asList(options).contains(StandardCopyOption.ATOMIC_MOVE)) {
                throw new AtomicMoveNotSupportedException(source.toString(), target.toString(), "Test file system");
            }
            Files.move(source, target, options);
        });
        assertEquals(List.of(List.of(StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING),
                List.of(StandardCopyOption.REPLACE_EXISTING)), attempts);
        assertEquals("Replacement records", Files.readString(destination));
        try (var files = Files.list(temporaryFolder)) {
            assertEquals(List.of(destination), files.toList());
        }
    }

    @Test
    public void writeToFile_atomicReplacementRejected_retriesOrdinaryReplacement() throws Exception {
        Path destination = temporaryFolder.resolve("watchdog.json");
        Files.writeString(destination, "Original records");
        List<List<CopyOption>> attempts = new ArrayList<>();
        FileUtil.writeToFile(destination, "Replacement records", (source, target, options) -> {
            attempts.add(Arrays.asList(options));
            assertEquals(destination, target);
            assertEquals("Original records", Files.readString(target));
            assertEquals("Replacement records", Files.readString(source));
            if (Arrays.asList(options).contains(StandardCopyOption.ATOMIC_MOVE)) {
                throw new FileAlreadyExistsException(target.toString());
            }
            Files.move(source, target, options);
        });
        assertEquals(List.of(List.of(StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING),
                List.of(StandardCopyOption.REPLACE_EXISTING)), attempts);
        assertEquals("Replacement records", Files.readString(destination));
        try (var files = Files.list(temporaryFolder)) {
            assertEquals(List.of(destination), files.toList());
        }
    }

    @Test
    public void writeToFile_ordinaryReplacementRejected_propagatesFailureAndCleansTemporaryFile() throws Exception {
        Path destination = temporaryFolder.resolve("watchdog.json");
        Files.writeString(destination, "Original records");
        List<List<CopyOption>> attempts = new ArrayList<>();
        IOException failure = new IOException("Ordinary replacement refused");
        IOException actual = assertThrows(IOException.class, () -> FileUtil.writeToFile(destination,
                "Replacement records", (source, target, options) -> {
                    attempts.add(Arrays.asList(options));
                    assertEquals("Original records", Files.readString(target));
                    assertEquals("Replacement records", Files.readString(source));
                    if (Arrays.asList(options).contains(StandardCopyOption.ATOMIC_MOVE)) {
                        throw new FileAlreadyExistsException(target.toString());
                    }
                    throw failure;
                }));
        org.junit.jupiter.api.Assertions.assertSame(failure, actual);
        assertEquals(List.of(List.of(StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING),
                List.of(StandardCopyOption.REPLACE_EXISTING)), attempts);
        assertEquals("Original records", Files.readString(destination));
        try (var files = Files.list(temporaryFolder)) {
            assertEquals(List.of(destination), files.toList());
        }
    }

    @Test
    public void writeToFile_otherMoveFailure_keepsOriginalWithoutRetryAndRemovesTemporaryFile() throws Exception {
        Path destination = temporaryFolder.resolve("watchdog.json");
        Files.writeString(destination, "Original records");
        List<Path> attempts = new ArrayList<>();
        IOException failure = new IOException("Replacement refused");
        IOException actual = assertThrows(IOException.class, () -> FileUtil.writeToFile(destination,
                "Replacement records", (source, target, options) -> {
                    attempts.add(source);
                    assertEquals("Replacement records", Files.readString(source));
                    throw failure;
                }));
        org.junit.jupiter.api.Assertions.assertSame(failure, actual);
        assertEquals(1, attempts.size());
        assertEquals("Original records", Files.readString(destination));
        try (var files = Files.list(temporaryFolder)) {
            assertEquals(List.of(destination), files.toList());
        }
    }
}
