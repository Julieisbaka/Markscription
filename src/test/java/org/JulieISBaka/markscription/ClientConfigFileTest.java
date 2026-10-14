package org.JulieISBaka.markscription;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Set;
import org.JulieISBaka.markscription.RenderOptions.Feature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ClientConfigFileTest {
    @TempDir
    Path directory;

    @Test
    void createsDefaultConfigAndRoundTripsEverySetting() throws IOException {
        Path path = directory.resolve("config").resolve("markscription-client.json");
        ClientConfigFile file = new ClientConfigFile(path);
        assertEquals(RenderOptions.DEFAULT, file.load());
        assertTrue(Files.isRegularFile(path));
        for (Feature feature : Feature.values()) {
            RenderOptions options = RenderOptions.DEFAULT.with(feature, false);
            file.save(options);
            assertEquals(options, file.load());
            assertFalse(file.load().enabled(feature));
        }
        file.save(new RenderOptions(Set.of()));
        assertEquals(new RenderOptions(Set.of()), file.load());
        try (var files = Files.list(path.getParent())) {
            assertEquals(1, files.count(), "Atomic save must not leave temporary files");
        }
    }

    @Test
    void missingSettingsKeepTheirDefaults() throws IOException {
        Path path = directory.resolve("client.json");
        Files.writeString(path, "{\"syntaxHighlighting\": false}");
        RenderOptions options = new ClientConfigFile(path).load();
        assertFalse(options.enabled(Feature.SYNTAX_HIGHLIGHTING));
        for (Feature feature : Feature.values()) {
            if (feature != Feature.SYNTAX_HIGHLIGHTING) {
                assertTrue(options.enabled(feature));
            }
        }
    }

    @Test
    void invalidConfigIsReportedAndNeverOverwritten() throws IOException {
        Path path = directory.resolve("client.json");
        for (String content : new String[] {
                "null", "[]", "{", "{\"bold\":\"false\"}", "{\"code\":null}",
                "{\"links\":1}", "{\"unknown\":true}", "{\"bold\":false} true",
                "{\"bold\":false,}", "{/*comment*/\"bold\":false}"
        }) {
            Files.writeString(path, content);
            assertThrows(IOException.class, () -> new ClientConfigFile(path).load(), content);
            assertEquals(content, Files.readString(path));
        }
    }

    @Test
    void writeFailuresAreExplicitAndDoNotDestroyExistingFiles() throws IOException {
        Path parentFile = directory.resolve("not-a-directory");
        Files.writeString(parentFile, "keep");
        ClientConfigFile file = new ClientConfigFile(parentFile.resolve("client.json"));
        assertThrows(IOException.class, () -> file.save(RenderOptions.DEFAULT));
        assertEquals("keep", Files.readString(parentFile));
    }

    @Test
    void renderingOptionsAreImmutableAndDoNotChangeGlobalDefaults() {
        EnumSet<Feature> original = EnumSet.allOf(Feature.class);
        RenderOptions options = new RenderOptions(original);
        original.clear();
        assertEquals(RenderOptions.DEFAULT, options);
        assertThrows(UnsupportedOperationException.class, () -> options.features().clear());
        assertTrue(RenderOptions.DEFAULT.enabled(Feature.BOLD));
        assertFalse(options.with(Feature.BOLD, false).enabled(Feature.BOLD));
        assertTrue(options.enabled(Feature.BOLD));
    }
}
