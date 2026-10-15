package org.JulieISBaka.markscription;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.JulieISBaka.markscription.CodeHighlighter.Token;

public record SyntaxTheme(Map<Token, Integer> colors) {
    public static final SyntaxTheme DEFAULT = defaults();

    public SyntaxTheme {
        colors = Map.copyOf(colors);
        for (Token token : Token.values()) {
            Integer color = colors.get(token);
            if (color == null || color < 0 || color > 0xFFFFFF) {
                throw new IllegalArgumentException("Theme must define a 24-bit RGB color for " + key(token));
            }
        }
    }

    public int color(Token token) {
        return colors.get(token);
    }

    public SyntaxTheme with(Token token, int color) {
        EnumMap<Token, Integer> updated = new EnumMap<>(Token.class);
        updated.putAll(colors);
        updated.put(token, color);
        return new SyntaxTheme(updated);
    }

    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        for (Token token : Token.values()) {
            object.addProperty(key(token), String.format(Locale.ROOT, "#%06X", color(token)));
        }
        return object;
    }

    public static SyntaxTheme fromJson(JsonElement element) throws IOException {
        if (element == null || !element.isJsonObject()) {
            throw new IOException("A theme must be an object containing five #RRGGBB colors");
        }
        JsonObject object = element.getAsJsonObject();
        if (object.size() != Token.values().length) {
            throw new IOException("A theme must contain exactly plain, keyword, string, comment, and number");
        }
        EnumMap<Token, Integer> colors = new EnumMap<>(Token.class);
        for (Token token : Token.values()) {
            JsonElement value = object.get(key(token));
            if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()
                    || !value.getAsString().matches("#[0-9A-Fa-f]{6}")) {
                throw new IOException("Theme color '" + key(token) + "' must be a string in #RRGGBB format");
            }
            colors.put(token, Integer.parseInt(value.getAsString().substring(1), 16));
        }
        return new SyntaxTheme(colors);
    }

    public static String key(Token token) {
        return Objects.requireNonNull(token).name().toLowerCase(Locale.ROOT);
    }

    private static SyntaxTheme defaults() {
        EnumMap<Token, Integer> colors = new EnumMap<>(Token.class);
        for (Token token : Token.values()) {
            colors.put(token, token.color());
        }
        return new SyntaxTheme(colors);
    }
}
