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
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

final class JsonFiles {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final long MAX_BYTES = 65536;

    private JsonFiles() {
    }

    static JsonObject read(Path path) throws IOException {
        if (Files.size(path) > MAX_BYTES) {
            throw new IOException("Configuration file exceeds 64 KiB: " + path);
        }
        return read(new StringReader(Files.readString(path, StandardCharsets.UTF_8)), path.toString());
    }

    static JsonObject read(Reader input, String source) throws IOException {
        try (JsonReader reader = new JsonReader(input)) {
            reader.setStrictness(Strictness.STRICT);
            JsonElement root = JsonParser.parseReader(reader);
            if (reader.peek() != JsonToken.END_DOCUMENT || !root.isJsonObject()) {
                throw new IOException("Expected one JSON object in " + source);
            }
            return root.getAsJsonObject();
        } catch (JsonParseException exception) {
            throw new IOException("Invalid JSON in " + source, exception);
        }
    }

    static void write(Path path, JsonObject object, boolean replace) throws IOException {
        path = path.toAbsolutePath();
        Files.createDirectories(path.getParent());
        Path temporary = Files.createTempFile(path.getParent(), path.getFileName().toString(), ".tmp");
        try {
            Files.writeString(temporary, GSON.toJson(object) + "\n", StandardCharsets.UTF_8);
            if (replace) {
                try {
                    Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (AtomicMoveNotSupportedException exception) {
                    Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                Files.move(temporary, path);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
