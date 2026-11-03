package seedu.address.testutil;

import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.security.SecureClassLoader;
import java.security.cert.Certificate;

/** Loads application-home and logging classes with a controlled launch location. */
public class ApplicationClassLoader extends SecureClassLoader {
    private final URL location;

    /** Creates a loader whose isolated classes report the supplied code-source URL. */
    public ApplicationClassLoader(URL location) {
        super(ApplicationClassLoader.class.getClassLoader());
        this.location = location;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (!name.equals("seedu.address.commons.util.ApplicationHome")
                && !name.equals("seedu.address.commons.core.LogsCenter")) {
            return super.loadClass(name, resolve);
        }
        synchronized (getClassLoadingLock(name)) {
            Class<?> loaded = findLoadedClass(name);
            if (loaded == null) {
                String resource = name.replace('.', '/') + ".class";
                try (var input = getParent().getResourceAsStream(resource)) {
                    if (input == null) {
                        throw new ClassNotFoundException(name);
                    }
                    byte[] bytes = input.readAllBytes();
                    loaded = defineClass(name, bytes, 0, bytes.length,
                            new CodeSource(location, (Certificate[]) null));
                } catch (IOException e) {
                    throw new ClassNotFoundException(name, e);
                }
            }
            if (resolve) {
                resolveClass(loaded);
            }
            return loaded;
        }
    }
}
