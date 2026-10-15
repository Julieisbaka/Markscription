package org.JulieISBaka.markscription;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.JulieISBaka.markscription.RenderOptions.Feature;

public final class ClientConfigFile {
    private static final Set<String> KEYS = Arrays.stream(Feature.values())
            .map(Feature::key).collect(Collectors.toUnmodifiableSet());
    private final Path path;

    public ClientConfigFile(Path path) {
        this.path = path.toAbsolutePath();
    }

    public RenderOptions load() throws IOException {
        if (!Files.exists(path)) {
            save(RenderOptions.DEFAULT);
            return RenderOptions.DEFAULT;
        }
        JsonObject object = JsonFiles.read(path);
        for (String key : object.keySet()) {
            if (!KEYS.contains(key) && !key.equals("theme")) {
                throw new IOException("Unknown setting '" + key + "' in " + path);
            }
        }
        RenderOptions options = object.has("theme")
                ? RenderOptions.DEFAULT.withTheme(SyntaxTheme.fromJson(object.get("theme")))
                : RenderOptions.DEFAULT;
        for (Feature feature : Feature.values()) {
            JsonElement value = object.get(feature.key());
            if (value != null) {
                if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) {
                    throw new IOException("Setting '" + feature.key() + "' must be true or false in " + path);
                }
                options = options.with(feature, value.getAsBoolean());
            }
        }
        return options;
    }

    public void save(RenderOptions options) throws IOException {
        JsonObject object = new JsonObject();
        for (Feature feature : Feature.values()) {
            object.addProperty(feature.key(), options.enabled(feature));
        }
        object.add("theme", options.theme().toJson());
        JsonFiles.write(path, object, true);
    }
}
