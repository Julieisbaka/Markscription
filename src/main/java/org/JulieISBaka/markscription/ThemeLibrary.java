package org.JulieISBaka.markscription;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

public final class ThemeLibrary {
    private final Path directory;

    public ThemeLibrary(Path directory) {
        this.directory = directory.toAbsolutePath().normalize();
    }

    public static boolean validName(String name) {
        return name != null && name.matches("[A-Za-z0-9][A-Za-z0-9_-]{0,63}\\.json");
    }

    public SyntaxTheme importTheme(String name) throws IOException {
        return SyntaxTheme.fromJson(JsonFiles.read(resolve(name)));
    }

    public void exportTheme(String name, SyntaxTheme theme) throws IOException {
        JsonFiles.write(resolve(name), theme.toJson(), false);
    }

    private Path resolve(String name) throws IOException {
        if (!validName(name)) {
            throw new IOException(
                    "Use a filename such as my-theme.json (letters, digits, underscores, and hyphens only)");
        }
        for (Path parent = directory; parent != null; parent = parent.getParent()) {
            if (Files.isSymbolicLink(parent)) {
                throw new IOException("Theme folders must not contain symbolic links: " + parent);
            }
        }
        Path path = directory.resolve(name);
        if (Files.isSymbolicLink(path)
                || (Files.exists(path, LinkOption.NOFOLLOW_LINKS)
                        && !Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))) {
            throw new IOException("Theme must be a regular JSON file: " + path);
        }
        return path;
    }
}
