package org.JulieISBaka.markscription;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.JulieISBaka.markscription.RenderOptions.Feature;

public final class ClientConfigFile {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
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
        JsonElement root;
        try (JsonReader reader = new JsonReader(new StringReader(Files.readString(path, StandardCharsets.UTF_8)))) {
            reader.setStrictness(Strictness.STRICT);
            root = JsonParser.parseReader(reader);
            if (reader.peek() != JsonToken.END_DOCUMENT) {
                throw new IOException("Unexpected content after the JSON object in " + path);
            }
        } catch (JsonParseException exception) {
            throw new IOException("Invalid JSON in " + path, exception);
        }
        if (!root.isJsonObject()) {
            throw new IOException("Expected a JSON object in " + path);
        }
        JsonObject object = root.getAsJsonObject();
        for (String key : object.keySet()) {
            if (!KEYS.contains(key)) {
                throw new IOException("Unknown setting '" + key + "' in " + path);
            }
        }
        RenderOptions options = RenderOptions.DEFAULT;
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
        Files.createDirectories(path.getParent());
        Path temporary = Files.createTempFile(path.getParent(), path.getFileName().toString(), ".tmp");
        try {
            Files.writeString(temporary, GSON.toJson(object) + "\n", StandardCharsets.UTF_8);
            try {
                Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
