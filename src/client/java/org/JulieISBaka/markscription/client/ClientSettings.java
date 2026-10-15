package org.JulieISBaka.markscription.client;

import java.io.IOException;
import java.nio.file.Path;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.JulieISBaka.markscription.ClientConfigFile;
import org.JulieISBaka.markscription.RenderOptions;
import org.JulieISBaka.markscription.ThemeLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClientSettings implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("markscription");
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("markscription-client.json");
    private static final ClientConfigFile FILE = new ClientConfigFile(PATH);
    public static final ThemeLibrary THEMES = new ThemeLibrary(
            FabricLoader.getInstance().getConfigDir().resolve("markscription").resolve("themes"));
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

    public static void save(RenderOptions updated) throws IOException {
        FILE.save(updated);
        options = updated;
    }
}
