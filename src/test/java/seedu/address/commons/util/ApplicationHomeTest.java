package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.testutil.ApplicationClassLoader;

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

    @Test
    public void initialization_invalidCodeSourceUri_preservesCause() throws Exception {
        // URI permits escaped spaces; URL also permits raw spaces, which toURI must reject.
        URL location = URI.create("file:/invalid%20location/WatchDog.jar").toURL();
        URL invalidLocation = new URL(location.getProtocol(), location.getHost(),
                location.getFile().replace("%20", " "));
        ClassLoader loader = new ApplicationClassLoader(invalidLocation);
        ExceptionInInitializerError error = assertThrows(ExceptionInInitializerError.class, () ->
                Class.forName(ApplicationHome.class.getName(), true, loader));
        assertInstanceOf(URISyntaxException.class, error.getCause());
    }
}
