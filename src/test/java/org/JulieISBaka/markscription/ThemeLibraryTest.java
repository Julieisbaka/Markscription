package org.JulieISBaka.markscription;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.JulieISBaka.markscription.CodeHighlighter.Token;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ThemeLibraryTest {
    @TempDir
    Path directory;

    @Test
    void exportsAndImportsOnlyPaletteWithoutFeaturePreferences() throws IOException {
        ThemeLibrary library = new ThemeLibrary(directory.resolve("themes"));
        SyntaxTheme theme = SyntaxTheme.DEFAULT.with(Token.KEYWORD, 0x123456).with(Token.PLAIN, 0x654321);
        library.exportTheme("my-theme.json", theme);
        assertEquals(theme, library.importTheme("my-theme.json"));
        String json = Files.readString(directory.resolve("themes").resolve("my-theme.json"));
        assertFalse(json.contains("markdown"));
        assertFalse(json.contains("syntaxHighlighting"));
        try (var files = Files.list(directory.resolve("themes"))) {
            assertEquals(1, files.count());
        }
    }

    @Test
    void exportNeverOverwritesAnExistingTheme() throws IOException {
        ThemeLibrary library = new ThemeLibrary(directory);
        library.exportTheme("existing.json", SyntaxTheme.DEFAULT);
        String before = Files.readString(directory.resolve("existing.json"));
        assertThrows(IOException.class, () -> library.exportTheme("existing.json",
                SyntaxTheme.DEFAULT.with(Token.PLAIN, 0)));
        assertEquals(before, Files.readString(directory.resolve("existing.json")));
        try (var files = Files.list(directory)) {
            assertEquals(1, files.count(), "Failed export must clean its temporary file");
        }
    }

    @Test
    void filenamesCannotEscapeTheThemeFolder() {
        ThemeLibrary library = new ThemeLibrary(directory);
        for (String name : new String[] { "../escape.json", "..\\escape.json", "C:\\escape.json",
                "sub/theme.json", "theme.txt", ".json", "", "two words.json", "x".repeat(65) + ".json" }) {
            assertFalse(ThemeLibrary.validName(name));
            assertThrows(IOException.class, () -> library.exportTheme(name, SyntaxTheme.DEFAULT), name);
            assertThrows(IOException.class, () -> library.importTheme(name), name);
        }
        assertTrue(ThemeLibrary.validName("x".repeat(64) + ".json"));
        assertTrue(ThemeLibrary.validName("Theme_2-dark.json"));
    }

    @Test
    void importErrorsDoNotModifyFilesOrInventDefaults() throws IOException {
        ThemeLibrary library = new ThemeLibrary(directory);
        assertThrows(IOException.class, () -> library.importTheme("missing.json"));
        for (String input : new String[] { "{", "null", "[]", "{}", "{\"plain\":\"#ffffff\"}",
                SyntaxTheme.DEFAULT.toJson() + " true", " ".repeat(65537) }) {
            Path file = directory.resolve("invalid.json");
            Files.writeString(file, input);
            assertThrows(IOException.class, () -> library.importTheme("invalid.json"));
            assertEquals(input, Files.readString(file));
        }
        Files.createDirectory(directory.resolve("folder.json"));
        assertThrows(IOException.class, () -> library.importTheme("folder.json"));
        assertThrows(IOException.class, () -> library.exportTheme("folder.json", SyntaxTheme.DEFAULT));
    }

    @Test
    void serverThemeUsesTheSamePortableFormat() throws IOException {
        Path serverFile = directory.resolve("markscription-server-theme.json");
        SyntaxTheme theme = SyntaxTheme.DEFAULT.with(Token.COMMENT, 0x245678);
        JsonFiles.write(serverFile, theme.toJson(), false);
        assertEquals(theme, SyntaxTheme.fromJson(JsonFiles.read(serverFile)));
        ThemeLibrary library = new ThemeLibrary(directory.resolve("themes"));
        Files.createDirectories(directory.resolve("themes"));
        Files.copy(serverFile, directory.resolve("themes").resolve("server.json"));
        assertEquals(theme, library.importTheme("server.json"));
    }
}
