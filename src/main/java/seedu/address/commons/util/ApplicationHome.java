package seedu.address.commons.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Locates application files beside the JAR, or in the working directory for development runs. */
public final class ApplicationHome {
    private static final Path HOME = locateHome();

    private ApplicationHome() {}

    public static Path getHome() {
        return HOME;
    }

    private static Path locateHome() {
        try {
            return fromLocation(ApplicationHome.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /** Uses the class-directory launch convention only when the code source is a directory. */
    static Path fromLocation(URI source) {
        Path location = Path.of(source).toAbsolutePath().normalize();
        return Files.isDirectory(location) ? Path.of("").toAbsolutePath().normalize() : location.getParent();
    }
}
