package org.JulieISBaka.markscription;

import static org.junit.jupiter.api.Assertions.*;

import com.mojang.serialization.JsonOps;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.Filterable;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.component.WrittenBookContent;
import org.JulieISBaka.markscription.RenderOptions.Feature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

class MarkdownBooksTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void rendersNestedInlineStyles() {
        Component result = render("**bold and *nested***, ~~gone~~, ***both***");
        assertEquals("bold and nested, gone, both", result.getString());
        assertTrue(styleOf(result, "bold and ").isBold());
        assertTrue(styleOf(result, "nested").isBold());
        assertTrue(styleOf(result, "nested").isItalic());
        assertTrue(styleOf(result, "gone").isStrikethrough());
        assertTrue(styleOf(result, "both").isBold());
        assertTrue(styleOf(result, "both").isItalic());
        assertFalse(styleOf(result, ", ").isBold());
    }

    @Test
    void rendersBlocksAndKeepsLineBreaks() {
        Component result = render("# Title\n\nParagraph\nnext  \nline\n\n> quote\n\n---");
        assertEquals("Title\n\nParagraph\nnext\nline\n\n> quote\n\n---", result.getString());
        assertTrue(styleOf(result, "Title").isBold());
    }

    @Test
    void rendersListsIncludingNestedAndNonDefaultStart() {
        assertEquals("- one\n- two\n  - nested", render("- one\n- two\n  - nested").getString());
        assertEquals("3. three\n4. four", render("3. three\n4. four").getString());
        assertEquals("- first\n\n- second", render("- first\n\n- second").getString());
        assertEquals("> first\n\n> second", render("> first\n>\n> second").getString());
    }

    @Test
    void codeIsLiteralAndUsesUniformFont() {
        Component inline = render("`**not bold**`");
        assertEquals("**not bold**", inline.getString());
        assertFalse(styleOf(inline, "**not bold**").isBold());
        assertEquals(new FontDescription.Resource(Identifier.withDefaultNamespace("uniform")),
                styleOf(inline, "**not bold**").getFont());
        assertEquals("*literal*\nnext", render("```java\n*literal*\nnext\n```").getString());
        assertEquals("**literal**", render("    **literal**").getString());
    }

    @Test
    void httpLinksUseVanillaOpenUrlActions() {
        Component result = render("[site](https://example.com/path?q=1)");
        assertEquals("site", result.getString());
        Style style = styleOf(result, "site");
        assertTrue(style.isUnderlined());
        assertEquals(new ClickEvent.OpenUrl(URI.create("https://example.com/path?q=1")), style.getClickEvent());
        assertNotNull(style.getHoverEvent());
        assertInstanceOf(ClickEvent.OpenUrl.class,
                styleOf(render("[site](http://example.com)"), "site").getClickEvent());
    }

    @Test
    void unsafeAndRelativeLinksNeverCreateActions() {
        for (String target : List.of("javascript:alert", "file:///tmp/test", "data:text/plain,test",
                "run_command:/op", "/relative", "https:missing-host")) {
            assertNull(styleOf(render("[label](" + target + ")"), "label").getClickEvent(), target);
        }
    }

    @Test
    void supportsReferenceLinksAndEscapesWithoutLoadingImagesOrHtml() {
        Component reference = render("[site][ref]\n\n[ref]: https://example.com");
        assertEquals("site", reference.getString());
        assertInstanceOf(ClickEvent.OpenUrl.class, styleOf(reference, "site").getClickEvent());
        assertEquals("**literal**", render("\\*\\*literal\\*\\*").getString());
        Component image = render("![alt text](https://example.com/image.png)");
        assertEquals("alt text", image.getString());
        assertNull(styleOf(image, "alt text").getClickEvent());
        assertEquals("<b>HTML</b>", render("<b>HTML</b>").getString());
    }

    @Test
    void bakedPagesAreIdempotentEvenWithEscapedMarkdown() {
        for (String source : List.of("**bold**", "\\*\\*literal\\*\\*", "`*code*`", "- item", "plain")) {
            Component baked = render(source);
            assertSame(baked, MarkdownBooks.renderPage(baked), source);
        }
    }

    @Test
    void preservesRichTextAndTranslatableComponents() {
        Component rich = Component.literal("**leave alone**").withStyle(style -> style.withBold(true));
        assertSame(rich, MarkdownBooks.renderPage(rich));
        Component siblings = Component.empty().append("**plain**")
                .append(Component.literal("styled").withStyle(style -> style.withItalic(true)));
        assertSame(siblings, MarkdownBooks.renderPage(siblings));
        Component translated = Component.translatable("book.pageIndicator", 1, 2);
        assertSame(translated, MarkdownBooks.renderPage(translated));
    }

    @Test
    void handlesUnstyledLiteralSiblingsAsOnePage() {
        Component page = Component.empty().append("**bo").append("ld**");
        Component result = MarkdownBooks.renderPage(page);
        assertEquals("bold", result.getString());
        assertTrue(styleOf(result, "bold").isBold());
    }

    @Test
    void preservesRawAndFilteredVariantsSeparately() {
        Filterable<Component> page = new Filterable<>(
                Component.literal("**raw**"), Optional.of(Component.literal("*filtered*")));
        List<Filterable<Component>> result = MarkdownBooks.formatPages(List.of(page));
        assertEquals("raw", result.getFirst().raw().getString());
        assertEquals("filtered", result.getFirst().get(true).getString());
        assertTrue(styleOf(result.getFirst().raw(), "raw").isBold());
        assertTrue(styleOf(result.getFirst().get(true), "filtered").isItalic());
        Filterable<Component> unfiltered = MarkdownBooks.formatPages(
                List.of(Filterable.passThrough(Component.literal("**safe**")))).getFirst();
        assertTrue(unfiltered.filtered().isEmpty());
        assertTrue(MarkdownBooks.formatPages(List.of()).isEmpty());
    }

    @Test
    void eachPageIsIndependentAndEmptyPagesRemainEmpty() {
        List<Filterable<Component>> pages = MarkdownBooks.formatPages(List.of(
                Filterable.passThrough(Component.literal("**open")),
                Filterable.passThrough(Component.literal("close**")),
                Filterable.passThrough(Component.empty())));
        assertEquals(List.of("**open", "close**", ""), pages.stream().map(page -> page.raw().getString()).toList());
    }

    @Test
    void respectsInputAndNestingLimits() {
        Component tooLong = Component.literal("x".repeat(WrittenBookContent.PAGE_LENGTH + 1));
        assertSame(tooLong, MarkdownBooks.renderPage(tooLong));
        Component tooDeep = Component.literal("> ".repeat(80) + "**deep**");
        assertSame(tooDeep, MarkdownBooks.renderPage(tooDeep));
    }

    @Test
    void emittedComponentsRoundTripThroughVanillaBookCodec() {
        Component page = render("# Heading\n\n**bold** *italic* ~~gone~~ `code` [link](https://example.com)");
        var encoded = WrittenBookContent.CONTENT_CODEC.encodeStart(JsonOps.INSTANCE, page).getOrThrow();
        Component decoded = WrittenBookContent.CONTENT_CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(page, decoded);
        assertSame(decoded, MarkdownBooks.renderPage(decoded));
    }

    @Test
    void preservesUnicodeTextAndNativeFontThroughMarkdown() {
        for (String text : List.of(
                "caf\u00e9 e\u0301",
                "\u65e5\u672c\u8a9e \u4e2d\u6587 \ud55c\uad6d\uc5b4",
                "\u041f\u0440\u0438\u0432\u0435\u0442",
                "\u0645\u0631\u062d\u0628\u0627 \u05e9\u05dc\u05d5\u05dd",
                "\u2665 \u2192 \ud83d\ude00 \ud83d\udc69\u200d\ud83d\udcbb \ud840\udc00")) {
            assertEquals(text, render(text).getString());
            Component bold = render("**" + text + "**");
            assertEquals(text, bold.getString());
            assertTrue(styleOf(bold, text).isBold());
            assertEquals(FontDescription.DEFAULT, styleOf(bold, text).getFont());
            assertEquals(text, render("`" + text + "`").getString());
            assertEquals("- " + text, render("- " + text).getString());
            assertEquals(text, render("[" + text + "](https://example.com)").getString());
        }
    }

    @Test
    void unicodeRawAndFilteredPagesSurviveVanillaSerialization() {
        String raw = "\u65e5\u672c\u8a9e \ud83d\ude00 e\u0301";
        String filtered = "\u0645\u0631\u062d\u0628\u0627 \u2665";
        List<Filterable<Component>> pages = MarkdownBooks.formatPages(List.of(new Filterable<>(
                Component.literal("**" + raw + "**"),
                Optional.of(Component.literal("*" + filtered + "*")))));
        WrittenBookContent book = new WrittenBookContent(
                Filterable.passThrough("\u65e5\u8a18"), "JulieISBaka", 0, pages, true);
        var encoded = WrittenBookContent.CODEC.encodeStart(JsonOps.INSTANCE, book).getOrThrow();
        WrittenBookContent decoded = WrittenBookContent.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(book, decoded);
        assertEquals(raw, decoded.pages().getFirst().raw().getString());
        assertEquals(filtered, decoded.pages().getFirst().get(true).getString());
        assertSame(decoded.pages().getFirst().raw(),
                MarkdownBooks.renderPage(decoded.pages().getFirst().raw()));
    }

    @Test
    void preservesSupplementaryCharactersAcrossLiteralSiblings() {
        Component page = Component.empty().append("**\ud83d").append("\ude00**");
        Component result = MarkdownBooks.renderPage(page);
        assertEquals("\ud83d\ude00", result.getString());
        assertEquals(1, result.getString().codePointCount(0, result.getString().length()));
        assertTrue(styleOf(result, "\ud83d\ude00").isBold());
    }

    @Test
    void labeledFencesHighlightButInlineAndUnknownCodeStayPlain() {
        Component page = render("```java\nreturn 42; // comment\n```");
        assertEquals("return 42; // comment", page.getString());
        assertEquals(CodeHighlighter.Token.KEYWORD.color(), styleOf(page, "return").getColor().getValue());
        assertEquals(CodeHighlighter.Token.NUMBER.color(), styleOf(page, "42").getColor().getValue());
        assertEquals(CodeHighlighter.Token.COMMENT.color(), styleOf(page, "// comment").getColor().getValue());
        assertEquals(0x333333, styleOf(render("`return`"), "return").getColor().getValue());
        for (String label : List.of("", "unknown")) {
            Component plain = render("```" + label + "\nreturn 42;\n```");
            assertEquals(0x333333, styleOf(plain, "return 42;").getColor().getValue());
        }
    }

    @Test
    void renderingSwitchesIndependentlyDisableStyles() {
        Component source = Component.literal("**bold**");
        assertSame(source, MarkdownBooks.renderPage(source, RenderOptions.DEFAULT.with(Feature.MARKDOWN, false)));
        Component bold = MarkdownBooks.renderPage(source, RenderOptions.DEFAULT.with(Feature.BOLD, false));
        assertEquals("bold", bold.getString());
        assertFalse(styleOf(bold, "bold").isBold());
        Component italic = MarkdownBooks.renderPage(Component.literal("*italic*"),
                RenderOptions.DEFAULT.with(Feature.ITALIC, false));
        assertFalse(styleOf(italic, "italic").isItalic());
        Component strike = MarkdownBooks.renderPage(Component.literal("~~strike~~"),
                RenderOptions.DEFAULT.with(Feature.STRIKETHROUGH, false));
        assertFalse(styleOf(strike, "strike").isStrikethrough());
        Component heading = MarkdownBooks.renderPage(Component.literal("# Title\n\n**bold**"),
                RenderOptions.DEFAULT.with(Feature.HEADINGS, false));
        assertFalse(styleOf(heading, "Title").isBold());
        assertTrue(styleOf(heading, "bold").isBold());
        Component link = MarkdownBooks.renderPage(Component.literal("[label](https://example.com)"),
                RenderOptions.DEFAULT.with(Feature.LINKS, false));
        assertEquals("label", link.getString());
        assertNull(styleOf(link, "label").getClickEvent());
        assertNull(styleOf(link, "label").getHoverEvent());
        assertFalse(styleOf(link, "label").isUnderlined());
    }

    @Test
    void codeAndHighlightingSwitchesAreIndependent() {
        Component source = Component.literal("```java\nreturn 42;\n```");
        Component noHighlight = MarkdownBooks.renderPage(source,
                RenderOptions.DEFAULT.with(Feature.SYNTAX_HIGHLIGHTING, false));
        assertEquals("return 42;", noHighlight.getString());
        assertEquals(0x333333, styleOf(noHighlight, "return 42;").getColor().getValue());
        Component noCode = MarkdownBooks.renderPage(source, RenderOptions.DEFAULT.with(Feature.CODE, false));
        assertEquals(FontDescription.DEFAULT, styleOf(noCode, "return 42;").getFont());
        assertNull(styleOf(noCode, "return 42;").getColor());
    }

    @Test
    void clientOptionsNeverRewriteServerFormattedComponents() {
        Component baked = render("```java\nreturn 42;\n```");
        assertSame(baked, MarkdownBooks.renderPage(baked, new RenderOptions(java.util.Set.of())));
        assertSame(baked, MarkdownBooks.renderPage(baked,
                RenderOptions.DEFAULT.with(Feature.SYNTAX_HIGHLIGHTING, false)));
        Component raw = Component.literal("```java\nreturn 42;\n```");
        assertEquals(baked, MarkdownBooks.formatPages(List.of(Filterable.passThrough(raw))).getFirst().raw());
    }

    @Test
    void highlightOverflowFallsBackToCodeWithoutLosingMarkdownFormatting() {
        String code = "int a=1;\n".repeat(200);
        Component source = Component.literal("# Heading\n\n```java\n" + code + "```");
        Component page = MarkdownBooks.renderPage(source);
        assertNotSame(source, page);
        assertEquals("Heading\n\n" + code.stripTrailing(), page.getString());
        assertTrue(styleOf(page, "Heading").isBold());
        assertEquals(page, MarkdownBooks.renderPage(source,
                RenderOptions.DEFAULT.with(Feature.SYNTAX_HIGHLIGHTING, false)));
        var encoded = WrittenBookContent.CONTENT_CODEC.encodeStart(JsonOps.INSTANCE, page).getOrThrow();
        assertEquals(page, WrittenBookContent.CONTENT_CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());
    }

    @Test
    void highlightedFilteredPagesRemainIndependentAndRoundTrip() {
        List<Filterable<Component>> pages = MarkdownBooks.formatPages(List.of(new Filterable<>(
                Component.literal("```java\nreturn 42;\n```"),
                Optional.of(Component.literal("```python\nreturn True\n```")))));
        assertEquals("return 42;", pages.getFirst().raw().getString());
        assertEquals("return True", pages.getFirst().get(true).getString());
        for (Component page : List.of(pages.getFirst().raw(), pages.getFirst().get(true))) {
            var encoded = WrittenBookContent.CONTENT_CODEC.encodeStart(JsonOps.INSTANCE, page).getOrThrow();
            assertEquals(page, WrittenBookContent.CONTENT_CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());
        }
    }

    @Test
    void minecraftSnbtAndTomlFencesRespectSettingsAndBookSerialization() {
        for (String[] example : List.of(
                new String[] { "mcfunction", "give @p minecraft:stone 2", "give", "2" },
                new String[] { "snbt", "{Flag:true,Count:2b}", "true", "2b" },
                new String[] { "toml", "enabled = true\ncount = 2", "true", "2" })) {
            Component source = Component.literal("```" + example[0] + "\n" + example[1] + "\n```");
            Component page = MarkdownBooks.renderPage(source);
            assertEquals(example[1], page.getString());
            assertEquals(CodeHighlighter.Token.KEYWORD.color(), styleOf(page, example[2]).getColor().getValue());
            assertEquals(CodeHighlighter.Token.NUMBER.color(), styleOf(page, example[3]).getColor().getValue());
            var encoded = WrittenBookContent.CONTENT_CODEC.encodeStart(JsonOps.INSTANCE, page).getOrThrow();
            Component decoded = WrittenBookContent.CONTENT_CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
            assertEquals(page, decoded);
            assertSame(decoded, MarkdownBooks.renderPage(decoded));
            Component plain = MarkdownBooks.renderPage(source,
                    RenderOptions.DEFAULT.with(Feature.SYNTAX_HIGHLIGHTING, false));
            assertEquals(example[1], plain.getString());
            plain.visit((style, text) -> {
                if (!text.isEmpty() && !text.equals("\n")) {
                    assertEquals(0x333333, style.getColor().getValue());
                }
                return Optional.empty();
            }, Style.EMPTY);
        }
    }

    @Test
    void serializationOverflowKeepsOriginalRatherThanLosingThePage() {
        Component source = Component.literal("**x** ".repeat(5000));
        assertSame(source, MarkdownBooks.renderPage(source));
    }

    private static Component render(String source) {
        return MarkdownBooks.renderPage(Component.literal(source));
    }

    private static Style styleOf(Component component, String text) {
        List<Style> styles = new ArrayList<>();
        component.visit((style, value) -> {
            if (value.equals(text)) {
                styles.add(style);
            }
            return Optional.empty();
        }, Style.EMPTY);
        assertFalse(styles.isEmpty(), "No text run found for: " + text);
        return styles.getFirst();
    }
}
