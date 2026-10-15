package org.JulieISBaka.markscription;

import com.mojang.serialization.JsonOps;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.List;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.Filterable;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.component.WrittenBookContent;
import org.JulieISBaka.markscription.RenderOptions.Feature;
import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MarkdownBooks {
    private static final Logger LOGGER = LoggerFactory.getLogger("markscription");
    private static final Parser PARSER = Parser.builder()
            .extensions(List.of(StrikethroughExtension.create()))
            .build();
    private static final FontDescription CODE_FONT = new FontDescription.Resource(
            Identifier.withDefaultNamespace("uniform"));
    private static final int MAX_DEPTH = 64;

    private MarkdownBooks() {
    }

    public static List<Filterable<Component>> formatPages(List<Filterable<Component>> pages) {
        return formatPages(pages, RenderOptions.DEFAULT);
    }

    public static List<Filterable<Component>> formatPages(List<Filterable<Component>> pages, RenderOptions options) {
        return pages.stream().map(page -> page.map(component -> renderPage(component, options))).toList();
    }

    public static Component renderPage(Component page) {
        return renderPage(page, RenderOptions.DEFAULT);
    }

    public static Component renderPage(Component page, RenderOptions options) {
        if (!options.enabled(Feature.MARKDOWN) || !isPlain(page)) {
            return page;
        }
        String source = page.getString();
        if (source.isEmpty()) {
            return page;
        }
        if (source.length() > WrittenBookContent.PAGE_LENGTH) {
            LOGGER.warn("Leaving an oversized Markdown book page unchanged ({} characters)", source.length());
            return page;
        }
        Node document = PARSER.parse(source);
        if (!withinDepthLimit(document)) {
            LOGGER.warn("Leaving a Markdown book page unchanged: nesting exceeds {}", MAX_DEPTH);
            return page;
        }
        Renderer renderer = render(document, options);
        Component rendered = renderer.output;
        boolean fits = fitsBookPage(rendered);
        if (renderer.highlighted && !fits) {
            LOGGER.warn("Syntax highlighting exceeded the book page limit; retrying with plain code formatting");
            rendered = render(document, options.with(Feature.SYNTAX_HIGHLIGHTING, false)).output;
            fits = fitsBookPage(rendered);
        }
        if (!fits) {
            LOGGER.warn("Leaving the Markdown book page unchanged because its formatted component cannot be stored");
        }
        return fits ? rendered : page;
    }

    private static Renderer render(Node document, RenderOptions options) {
        Renderer renderer = new Renderer(options);
        renderer.render(document, Style.EMPTY);
        return renderer;
    }

    private static boolean fitsBookPage(Component rendered) {
        var encoded = WrittenBookContent.CONTENT_CODEC.encodeStart(JsonOps.INSTANCE, rendered);
        if (encoded.isError()) {
            LOGGER.warn("Cannot serialize formatted book component: {}", encoded.error().orElseThrow().message());
            return false;
        }
        if (GsonHelper.encodesLongerThan(encoded.getOrThrow(), WrittenBookContent.PAGE_LENGTH)) {
            LOGGER.warn("Formatted book component exceeds {} characters",
                    WrittenBookContent.PAGE_LENGTH);
            return false;
        }
        return true;
    }

    private static boolean isPlain(Component component) {
        ArrayDeque<Component> pending = new ArrayDeque<>();
        pending.push(component);
        while (!pending.isEmpty()) {
            Component next = pending.pop();
            if (!(next.getContents() instanceof PlainTextContents) || !next.getStyle().isEmpty()) {
                return false;
            }
            pending.addAll(next.getSiblings());
        }
        return true;
    }

    private static boolean withinDepthLimit(Node root) {
        Node node = root;
        int depth = 0;
        while (node != null) {
            if (node.getFirstChild() != null) {
                if (++depth > MAX_DEPTH) {
                    return false;
                }
                node = node.getFirstChild();
            } else {
                while (node != root && node.getNext() == null) {
                    node = node.getParent();
                    depth--;
                }
                node = node == root ? null : node.getNext();
            }
        }
        return true;
    }

    private static final class Renderer {
        // An explicit default style also prevents client-side re-parsing of baked
        // pages.
        private final MutableComponent output = Component.empty().setStyle(Style.EMPTY.withItalic(false));
        private final RenderOptions options;
        private String prefix = "";
        private boolean lineStart = true;
        private boolean highlighted;

        private Renderer(RenderOptions options) {
            this.options = options;
        }

        private void render(Node node, Style style) {
            switch (node) {
                case Text text -> append(text.getLiteral(), style);
                case Code code -> append(code.getLiteral(), codeStyle(style));
                case StrongEmphasis _ -> inline(node, options.enabled(Feature.BOLD) ? style.withBold(true) : style);
                case Emphasis _ -> inline(node, options.enabled(Feature.ITALIC) ? style.withItalic(true) : style);
                case Strikethrough _ -> inline(node,
                        options.enabled(Feature.STRIKETHROUGH) ? style.withStrikethrough(true) : style);
                case Heading _ -> inline(node, options.enabled(Feature.HEADINGS) ? style.withBold(true) : style);
                case SoftLineBreak _ -> append("\n", style);
                case HardLineBreak _ -> append("\n", style);
                case FencedCodeBlock code -> fencedCode(code, style);
                case IndentedCodeBlock code -> append(withoutFinalNewline(code.getLiteral()), codeStyle(style));
                case HtmlInline html -> append(html.getLiteral(), style);
                case HtmlBlock html -> append(withoutFinalNewline(html.getLiteral()), style);
                case ThematicBreak _ -> append("---", style.withColor(0x555555));
                case Link link -> inline(node,
                        options.enabled(Feature.LINKS) ? linkStyle(link.getDestination(), style) : style);
                case Image _ -> inline(node, style);
                case LinkReferenceDefinition _ -> {
                }
                case BlockQuote _ -> {
                    String previous = prefix;
                    prefix += "> ";
                    blocks(node, style);
                    prefix = previous;
                }
                case BulletList _ -> list(node, style, false);
                case OrderedList _ -> list(node, style, true);
                case Document _ -> blocks(node, style);
                default -> inline(node, style);
            }
        }

        private void inline(Node parent, Style style) {
            for (Node child = parent.getFirstChild(); child != null; child = child.getNext()) {
                render(child, style);
            }
        }

        private void blocks(Node parent, Style style) {
            boolean first = true;
            for (Node child = parent.getFirstChild(); child != null; child = child.getNext()) {
                if (child instanceof LinkReferenceDefinition) {
                    continue;
                }
                if (!first) {
                    append("\n\n", style);
                }
                render(child, style);
                first = false;
            }
        }

        private void list(Node list, Style style, boolean ordered) {
            int number = list instanceof OrderedList numbered ? numbered.getMarkerStartNumber() : 1;
            boolean first = true;
            for (Node item = list.getFirstChild(); item != null; item = item.getNext()) {
                if (!first) {
                    append(((ListBlock) list).isTight() ? "\n" : "\n\n", style);
                }
                String marker = ordered ? number++ + ". " : "- ";
                append(marker, style);
                String previous = prefix;
                prefix += " ".repeat(marker.length());
                boolean firstBlock = true;
                for (Node block = item.getFirstChild(); block != null; block = block.getNext()) {
                    if (!firstBlock) {
                        append(block instanceof ListBlock ? "\n" : "\n\n", style);
                    }
                    render(block, style);
                    firstBlock = false;
                }
                prefix = previous;
                first = false;
            }
        }

        private void append(String text, Style style) {
            int start = 0;
            while (start < text.length()) {
                int newline = text.indexOf('\n', start);
                int end = newline < 0 ? text.length() : newline;
                if (end > start) {
                    if (lineStart && !prefix.isEmpty()) {
                        output.append(Component.literal(prefix));
                    }
                    output.append(Component.literal(text.substring(start, end)).setStyle(style));
                    lineStart = false;
                }
                if (newline < 0) {
                    break;
                }
                output.append(Component.literal("\n"));
                lineStart = true;
                start = newline + 1;
            }
        }

        private void fencedCode(FencedCodeBlock code, Style style) {
            String text = withoutFinalNewline(code.getLiteral());
            Style base = codeStyle(style);
            if (options.enabled(Feature.CODE) && options.enabled(Feature.SYNTAX_HIGHLIGHTING)
                    && CodeHighlighter.highlight(text, code.getInfo(),
                            (part, token) -> append(part, base.withColor(options.theme().color(token))))) {
                highlighted = true;
                return;
            }
            append(text, base);
        }

        private Style codeStyle(Style style) {
            return options.enabled(Feature.CODE)
                    ? style.withFont(CODE_FONT).withColor(options.theme().color(CodeHighlighter.Token.PLAIN))
                    : style;
        }

        private static Style linkStyle(String destination, Style style) {
            try {
                URI uri = new URI(destination);
                if (uri.getHost() != null
                        && ("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))) {
                    return style.withColor(0x0000AA).withUnderlined(true)
                            .withClickEvent(new ClickEvent.OpenUrl(uri))
                            .withHoverEvent(new HoverEvent.ShowText(Component.literal(destination)));
                }
            } catch (URISyntaxException ignored) {
                // Invalid Markdown destinations remain readable labels, never executable
                // actions.
            }
            return style;
        }

        private static String withoutFinalNewline(String text) {
            return text.endsWith("\n") ? text.substring(0, text.length() - 1) : text;
        }
    }
}
