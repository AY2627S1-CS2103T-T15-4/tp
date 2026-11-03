package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
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
    public void writeToFile_relativeSymlinkChain_updatesTargetAndPreservesLinks() throws Exception {
        Path shared = Files.createDirectory(temporaryFolder.resolve("shared"));
        Path target = Files.writeString(shared.resolve("watchdog.json"), "Original records");
        Path intermediate = createSymbolicLink(temporaryFolder.resolve("shared.json"),
                temporaryFolder.relativize(target));
        Path home = Files.createDirectory(temporaryFolder.resolve("home"));
        Path link = createSymbolicLink(home.resolve("watchdog.json"), home.relativize(intermediate));
        FileUtil.writeToFile(link, "Replacement records", (source, destination, options) -> {
            assertEquals(target.toRealPath(), destination);
            assertEquals(target.toRealPath().getParent(), source.getParent());
            assertEquals("Original records", Files.readString(target));
            assertEquals("Replacement records", Files.readString(source));
            Files.move(source, destination, options);
        });
        assertTrue(Files.isSymbolicLink(link));
        assertTrue(Files.isSymbolicLink(intermediate));
        assertEquals(home.relativize(intermediate), Files.readSymbolicLink(link));
        assertEquals("Replacement records", Files.readString(target));
        assertEquals("Replacement records", FileUtil.readFromFile(link));
        try (var files = Files.list(shared)) {
            assertEquals(List.of(target), files.toList());
        }
    }

    @Test
    public void writeToFile_symlinkReplacementFailure_preservesLinkAndTarget() throws Exception {
        Path target = Files.writeString(temporaryFolder.resolve("shared.json"), "Original records");
        Path link = createSymbolicLink(temporaryFolder.resolve("watchdog.json"), target);
        assertThrows(IOException.class, () -> FileUtil.writeToFile(link, "Replacement records", (
                source, destination, options) -> {
                    throw new IOException("Replacement refused");
                }));
        assertTrue(Files.isSymbolicLink(link));
        assertEquals(target, Files.readSymbolicLink(link));
        assertEquals("Original records", Files.readString(target));
        try (var files = Files.list(temporaryFolder)) {
            assertEquals(2, files.count());
        }
    }

    @Test
    public void writeToFile_danglingSymlink_rejectsWithoutReplacingLink() throws Exception {
        Path target = temporaryFolder.resolve("missing.json");
        Path link = createSymbolicLink(temporaryFolder.resolve("watchdog.json"), target);
        assertThrows(NoSuchFileException.class, () -> FileUtil.writeToFile(link, "Replacement records"));
        assertTrue(Files.isSymbolicLink(link));
        assertEquals(target, Files.readSymbolicLink(link));
        try (var files = Files.list(temporaryFolder)) {
            assertEquals(List.of(link), files.toList());
        }
    }

    private Path createSymbolicLink(Path link, Path target) throws IOException {
        try {
            return Files.createSymbolicLink(link, target);
        } catch (UnsupportedOperationException e) {
            assumeTrue(false, "Symbolic links are unsupported: " + e);
            throw e;
        } catch (java.nio.file.FileSystemException e) {
            assumeTrue(!System.getProperty("os.name").startsWith("Windows")
                    || e.getReason() == null || !e.getReason().contains("privilege"),
                    "Windows symbolic-link privilege is unavailable: " + e);
            throw e;
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
