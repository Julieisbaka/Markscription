package org.JulieISBaka.markscription;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ServerSettings implements ModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("markscription");
    private static RenderOptions options = RenderOptions.DEFAULT;

    @Override
    public void onInitialize() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("markscription-server-theme.json");
        try {
            if (!Files.exists(path)) {
                JsonFiles.write(path, SyntaxTheme.DEFAULT.toJson(), false);
            }
            options = RenderOptions.DEFAULT.withTheme(SyntaxTheme.fromJson(JsonFiles.read(path)));
        } catch (IOException exception) {
            LOGGER.error("Cannot load server theme from {}. Using default colors; existing files were not replaced.",
                    path, exception);
        }
    }

    public static RenderOptions options() {
        return options;
    }
}
