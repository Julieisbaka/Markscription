package org.JulieISBaka.markscription;

import java.util.Locale;
import java.util.Set;
import java.util.function.BiConsumer;

public final class CodeHighlighter {
    private CodeHighlighter() {
    }

    public enum Token {
        PLAIN, KEYWORD, STRING, COMMENT, NUMBER
    }

    public static boolean highlight(String source, String info, BiConsumer<String, Token> output) {
        String label = info.strip().split("\\s+", 2)[0].toLowerCase(Locale.ROOT);
        Language language = switch (label) {
            case "java" -> Language.JAVA;
            case "javascript", "js" -> Language.JAVASCRIPT;
            case "python", "py" -> Language.PYTHON;
            case "json" -> Language.JSON;
            case "mcfunction", "minecraft", "mccommand" -> Language.MINECRAFT;
            case "snbt" -> Language.SNBT;
            case "toml" -> Language.TOML;
            default -> null;
        };
        if (language == null) {
            return false;
        }
        int start = 0;
        int position = 0;
        Token previous = Token.PLAIN;
        boolean lineStart = true;
        boolean commandExpected = true;
        while (position < source.length()) {
            int end = position + 1;
            char current = source.charAt(position);
            Token token = Token.PLAIN;
            if (((language == Language.PYTHON || language == Language.TOML
                    || (language == Language.MINECRAFT && lineStart)) && current == '#')
                    || (language.slashComments && source.startsWith("//", position))) {
                token = Token.COMMENT;
                int newline = source.indexOf('\n', position);
                end = newline < 0 ? source.length() : newline;
            } else if (language.slashComments && source.startsWith("/*", position)) {
                token = Token.COMMENT;
                int closing = source.indexOf("*/", position + 2);
                end = closing < 0 ? source.length() : closing + 2;
            } else if (current == '"' || (current == '\'' && language != Language.JSON)
                    || (current == '`' && language == Language.JAVASCRIPT)) {
                token = Token.STRING;
                boolean triple = (language == Language.PYTHON || language == Language.JAVA)
                        && source.startsWith(String.valueOf(current).repeat(3), position);
                triple |= language == Language.TOML
                        && source.startsWith(String.valueOf(current).repeat(3), position);
                end = stringEnd(source, position, current, triple,
                        language != Language.TOML || current != '\'');
            } else if (language == Language.MINECRAFT && current == '@'
                    && position + 1 < source.length() && "paresn".indexOf(source.charAt(position + 1)) >= 0) {
                end = position + 2;
                if (end == source.length() || !Character.isUnicodeIdentifierPart(source.codePointAt(end))) {
                    token = Token.KEYWORD;
                }
            } else if (Character.isUnicodeIdentifierStart(source.codePointAt(position))
                    || current == '_' || current == '$') {
                end = position + Character.charCount(source.codePointAt(position));
                while (end < source.length()) {
                    int next = source.codePointAt(end);
                    if (!Character.isUnicodeIdentifierPart(next) && next != '$'
                            && !((language == Language.MINECRAFT || language == Language.SNBT
                                    || language == Language.TOML)
                                    && (next == '/' || next == '-'
                                            || next == '.'))
                            && !(language == Language.MINECRAFT && next == ':')) {
                        break;
                    }
                    end += Character.charCount(next);
                }
                String word = source.substring(position, end);
                if (language.keywords.contains(word)) {
                    token = Token.KEYWORD;
                }
                if (language == Language.MINECRAFT) {
                    if (commandExpected && COMMANDS.contains(word)) {
                        token = Token.KEYWORD;
                    }
                    commandExpected = word.equals("run");
                }
            } else if (current >= '0' && current <= '9') {
                token = Token.NUMBER;
                end = numberEnd(source, position);
                if (language == Language.SNBT && end < source.length()
                        && "bBsSlLfFdD".indexOf(source.charAt(end)) >= 0) {
                    end++;
                }
            } else if (Character.isHighSurrogate(current) && position + 1 < source.length()
                    && Character.isLowSurrogate(source.charAt(position + 1))) {
                end++;
            }
            for (int index = position; index < end; index++) {
                char consumed = source.charAt(index);
                if (consumed == '\n') {
                    lineStart = true;
                    commandExpected = true;
                } else if (!Character.isWhitespace(consumed)) {
                    lineStart = false;
                }
            }
            if (token != previous) {
                if (position > start) {
                    output.accept(source.substring(start, position), previous);
                }
                start = position;
                previous = token;
            }
            position = end;
        }
        if (position > start) {
            output.accept(source.substring(start, position), previous);
        }
        return true;
    }

    private static int stringEnd(String source, int start, char quote, boolean triple, boolean escapes) {
        int delimiterLength = triple ? 3 : 1;
        String delimiter = String.valueOf(quote).repeat(delimiterLength);
        int position = start + delimiterLength;
        while (position < source.length()) {
            if (escapes && source.charAt(position) == '\\') {
                position = Math.min(position + 2, source.length());
            } else if (source.startsWith(delimiter, position)) {
                return position + delimiterLength;
            } else {
                position++;
            }
        }
        return position;
    }

    private static final Set<String> COMMANDS = Set.of(
            "advancement", "attribute", "ban", "ban-ip", "banlist", "bossbar", "clear", "clone",
            "damage", "data", "datapack", "debug", "defaultgamemode", "deop", "difficulty", "effect",
            "enchant", "execute", "experience", "fill", "fillbiome", "forceload", "function", "gamemode",
            "gamerule", "give", "help", "item", "kick", "kill", "list", "locate", "loot", "me", "msg",
            "op", "pardon", "particle", "place", "playsound", "publish", "random", "recipe", "reload",
            "return", "ride", "say", "schedule", "scoreboard", "seed", "setblock", "setworldspawn",
            "spawnpoint", "spectate", "spreadplayers", "stopsound", "summon", "tag", "team", "teammsg",
            "teleport", "tell", "tellraw", "tick", "time", "title", "tp", "trigger", "w", "weather",
            "whitelist", "worldborder", "xp");

    private static int numberEnd(String source, int start) {
        int position = start;
        boolean hex = source.startsWith("0x", start) || source.startsWith("0X", start);
        boolean binary = source.startsWith("0b", start) || source.startsWith("0B", start);
        if (hex || binary) {
            position += 2;
        }
        while (position < source.length()) {
            char next = source.charAt(position);
            if (Character.digit(next, hex ? 16 : binary ? 2 : 10) >= 0 || next == '_') {
                position++;
            } else if (!hex && !binary && next == '.' && position + 1 < source.length()
                    && Character.isDigit(source.charAt(position + 1))) {
                position++;
            } else if (!hex && !binary && (next == 'e' || next == 'E')) {
                position++;
                if (position < source.length() && (source.charAt(position) == '+' || source.charAt(position) == '-')) {
                    position++;
                }
            } else {
                break;
            }
        }
        return position;
    }

    private enum Language {
        JAVA(true, Set.of("abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
                "class", "const", "continue", "default", "do", "double", "else", "enum", "extends",
                "final", "finally", "float", "for", "if", "implements", "import", "instanceof", "int",
                "interface", "long", "native", "new", "package", "private", "protected", "public",
                "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
                "throw", "throws", "transient", "try", "void", "volatile", "while", "true", "false",
                "null", "var", "record", "sealed", "permits", "yield")),
        JAVASCRIPT(true, Set.of("async", "await", "break", "case", "catch", "class", "const", "continue",
                "debugger", "default", "delete", "do", "else", "export", "extends", "false", "finally",
                "for", "function", "if", "import", "in", "instanceof", "let", "new", "null", "of",
                "return", "static", "super", "switch", "this", "throw", "true", "try", "typeof",
                "undefined", "var", "void", "while", "yield")),
        PYTHON(false, Set.of("False", "None", "True", "and", "as", "assert", "async", "await", "break",
                "class", "continue", "def", "del", "elif", "else", "except", "finally", "for", "from",
                "global", "if", "import", "in", "is", "lambda", "nonlocal", "not", "or", "pass",
                "raise", "return", "try", "while", "with", "yield", "match", "case")),
        JSON(false, Set.of("true", "false", "null")),
        MINECRAFT(false, Set.of()),
        SNBT(false, Set.of("true", "false")),
        TOML(false, Set.of("true", "false", "inf", "nan"));

        private final boolean slashComments;
        private final Set<String> keywords;

        Language(boolean slashComments, Set<String> keywords) {
            this.slashComments = slashComments;
            this.keywords = keywords;
        }
    }
}
