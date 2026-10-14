package org.JulieISBaka.markscription;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.JulieISBaka.markscription.CodeHighlighter.Token;
import org.junit.jupiter.api.Test;

class CodeHighlighterTest {
    @Test
    void javaRecognizesWholeKeywordsAndKeepsStringsAndCommentsOpaque() {
        List<Run> runs = highlight("int integer = 42; String s = \"return 99\"; // false 23\nreturn;", "java");
        assertContains(runs, "int", Token.KEYWORD);
        assertContains(runs, " integer = ", Token.PLAIN);
        assertContains(runs, "42", Token.NUMBER);
        assertContains(runs, "\"return 99\"", Token.STRING);
        assertContains(runs, "// false 23", Token.COMMENT);
        assertContains(runs, "return", Token.KEYWORD);
    }

    @Test
    void javascriptHandlesEscapedStringsTemplatesAndMultilineComments() {
        List<Run> runs = highlight("const s = \"a\\\"return\"; /* let\n42 */\nlet t = `true ${1}`;", "js");
        assertContains(runs, "const", Token.KEYWORD);
        assertContains(runs, "\"a\\\"return\"", Token.STRING);
        assertContains(runs, "/* let\n42 */", Token.COMMENT);
        assertContains(runs, "`true ${1}`", Token.STRING);
    }

    @Test
    void pythonHandlesTripleStringsAndHashComments() {
        List<Run> runs = highlight("def f():\n  return '''True\n42''' # None\n", "py");
        assertContains(runs, "def", Token.KEYWORD);
        assertContains(runs, "return", Token.KEYWORD);
        assertContains(runs, "'''True\n42'''", Token.STRING);
        assertContains(runs, "# None", Token.COMMENT);
    }

    @Test
    void jsonOnlyHighlightsItsOwnKeywordsAndDoubleQuotedStrings() {
        List<Run> runs = highlight("{\"class\": true, \"v\": -1.2e+3, \"n\": null}", "json");
        assertContains(runs, "\"class\"", Token.STRING);
        assertContains(runs, "true", Token.KEYWORD);
        assertContains(runs, "1.2e+3", Token.NUMBER);
        assertContains(runs, "null", Token.KEYWORD);
        assertTrue(highlight("class // hi\n# hi", "json").stream().allMatch(run -> run.token() == Token.PLAIN));
    }

    @Test
    void numericTokensDoNotConsumeFollowingOperators() {
        List<Run> runs = highlight("42+7 0xFF 0b101 1_000 1.5e-2", "java");
        assertContains(runs, "42", Token.NUMBER);
        assertContains(runs, "+", Token.PLAIN);
        assertContains(runs, "7", Token.NUMBER);
        assertContains(runs, "0xFF", Token.NUMBER);
        assertContains(runs, "0b101", Token.NUMBER);
        assertContains(runs, "1_000", Token.NUMBER);
        assertContains(runs, "1.5e-2", Token.NUMBER);
    }

    @Test
    void labelsAreCaseInsensitiveAndIgnoreFenceMetadata() {
        for (String label : List.of("java", "JAVA", "java title=demo", "JavaScript", "js", "python", "py", "json")) {
            String code = label.toLowerCase().startsWith("py") ? "True" : "true";
            assertContains(highlight(code, label), code, Token.KEYWORD);
        }
        for (String label : List.of("", "text", "rust", "typescript", "java-ish")) {
            assertFalse(
                    CodeHighlighter.highlight("return", label, (text, token) -> fail("Unknown language emitted text")));
        }
    }

    @Test
    void preservesUnicodeUnclosedTokensAndAllWhitespace() {
        for (String source : List.of("", "\n \t\n", "int \u53d8\u91cf = 3; \ud83d\ude00",
                "\"unterminated\\", "/* unterminated", "'''unterminated", "\"a\\\"\ud83d\ude00\"")) {
            for (String language : List.of("java", "javascript", "python", "json")) {
                highlight(source, language);
            }
        }
    }

    @Test
    void scansBookSizedInputsQuicklyAndCoalescesPlainText() {
        String source = "int value = 42; // comment\n".repeat(1000);
        assertTimeout(Duration.ofSeconds(10), () -> {
            for (int i = 0; i < 100; i++) {
                assertTrue(CodeHighlighter.highlight(source, "java", (text, token) -> {
                }));
            }
        });
        assertEquals(1, highlight("ordinary identifiers and punctuation + = ; ".repeat(500), "java").size());
    }

    private static List<Run> highlight(String source, String language) {
        List<Run> runs = new ArrayList<>();
        assertTrue(CodeHighlighter.highlight(source, language, (text, token) -> runs.add(new Run(text, token))));
        assertEquals(source, String.join("", runs.stream().map(Run::text).toList()));
        return runs;
    }

    private static void assertContains(List<Run> runs, String text, Token token) {
        assertTrue(runs.contains(new Run(text, token)), () -> "Missing " + token + " run " + text + " in " + runs);
    }

    private record Run(String text, Token token) {
    }
}
