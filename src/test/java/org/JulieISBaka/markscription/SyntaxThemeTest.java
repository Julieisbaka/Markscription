package org.JulieISBaka.markscription;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.EnumMap;
import org.JulieISBaka.markscription.CodeHighlighter.Token;
import org.junit.jupiter.api.Test;

class SyntaxThemeTest {
    @Test
    void defaultPalettePreservesExistingColors() throws IOException {
        for (Token token : Token.values()) {
            assertEquals(token.color(), SyntaxTheme.DEFAULT.color(token));
        }
        assertEquals(SyntaxTheme.DEFAULT, SyntaxTheme.fromJson(SyntaxTheme.DEFAULT.toJson()));
    }

    @Test
    void colorsAreImmutableAndRgbBoundariesAreValid() throws IOException {
        EnumMap<Token, Integer> mutable = new EnumMap<>(Token.class);
        mutable.putAll(SyntaxTheme.DEFAULT.colors());
        SyntaxTheme theme = new SyntaxTheme(mutable);
        mutable.put(Token.KEYWORD, 0);
        assertEquals(Token.KEYWORD.color(), theme.color(Token.KEYWORD));
        assertThrows(UnsupportedOperationException.class, () -> theme.colors().clear());
        assertThrows(IllegalArgumentException.class, () -> theme.with(Token.PLAIN, -1));
        assertThrows(IllegalArgumentException.class, () -> theme.with(Token.PLAIN, 0x1000000));
        SyntaxTheme extremes = theme.with(Token.PLAIN, 0).with(Token.STRING, 0xFFFFFF);
        assertEquals("#000000", extremes.toJson().get("plain").getAsString());
        assertEquals("#FFFFFF", extremes.toJson().get("string").getAsString());
        assertEquals(extremes, SyntaxTheme.fromJson(extremes.toJson()));
    }

    @Test
    void rejectsMissingExtraAndInvalidColorValues() {
        JsonObject missing = SyntaxTheme.DEFAULT.toJson();
        missing.remove("comment");
        assertThrows(IOException.class, () -> SyntaxTheme.fromJson(missing));
        JsonObject extra = SyntaxTheme.DEFAULT.toJson();
        extra.addProperty("unknown", "#123456");
        assertThrows(IOException.class, () -> SyntaxTheme.fromJson(extra));
        for (String value : new String[] { "red", "#fff", "123456", "#12345678", "#GGGGGG", " #123456" }) {
            JsonObject invalid = SyntaxTheme.DEFAULT.toJson();
            invalid.addProperty("plain", value);
            assertThrows(IOException.class, () -> SyntaxTheme.fromJson(invalid), value);
        }
        JsonObject numeric = SyntaxTheme.DEFAULT.toJson();
        numeric.addProperty("plain", 123456);
        assertThrows(IOException.class, () -> SyntaxTheme.fromJson(numeric));
        assertThrows(IOException.class, () -> SyntaxTheme.fromJson(null));
    }

    @Test
    void lowercaseHexAndOptionCopiesPreserveTheme() throws IOException {
        JsonObject object = SyntaxTheme.DEFAULT.toJson();
        object.addProperty("keyword", "#abcdef");
        SyntaxTheme theme = SyntaxTheme.fromJson(object);
        assertEquals(0xABCDEF, theme.color(Token.KEYWORD));
        RenderOptions options = RenderOptions.DEFAULT.withTheme(theme);
        assertEquals(theme, options.with(RenderOptions.Feature.BOLD, false).theme());
        assertEquals(SyntaxTheme.DEFAULT, RenderOptions.DEFAULT.theme());
    }
}
