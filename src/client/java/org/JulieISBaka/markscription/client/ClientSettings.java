package org.JulieISBaka.markscription.client;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.JulieISBaka.markscription.ClientConfigFile;
import org.JulieISBaka.markscription.RenderOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClientSettings implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("markscription");
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("markscription-client.json");
    private static final ClientConfigFile FILE = new ClientConfigFile(PATH);
    private static volatile RenderOptions options = RenderOptions.DEFAULT;

    @Override
    public void onInitializeClient() {
        try {
            options = FILE.load();
        } catch (IOException exception) {
            LOGGER.error("Cannot load {}. Using default rendering settings; the existing file was not replaced.",
                    PATH, exception);
        }
    }

    public static RenderOptions options() {
        return options;
    }

    public static void save(RenderOptions updated) {
        try {
            FILE.save(updated);
            options = updated;
        } catch (IOException exception) {
            LOGGER.error("Cannot save rendering settings to {}; active settings were not changed", PATH, exception);
            throw new UncheckedIOException("Could not save Markscription client settings to " + PATH, exception);
        }
    }
}
