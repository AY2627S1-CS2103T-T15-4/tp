package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ApplicationHomeTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void fromLocation_jarWithSpacesAndPercent_usesJarDirectory() throws Exception {
        Path home = Files.createDirectory(temporaryFolder.resolve("Daycare 100%"));
        Path jar = Files.createFile(home.resolve("WatchDog final.jar"));
        assertEquals(home, ApplicationHome.fromLocation(jar.toUri()));
    }

    @Test
    public void fromLocation_classDirectory_usesDevelopmentWorkingDirectory() {
        assertEquals(Path.of("").toAbsolutePath().normalize(),
                ApplicationHome.fromLocation(temporaryFolder.toUri()));
        assertEquals(Path.of("").toAbsolutePath().normalize(), ApplicationHome.getHome());
    }
}
