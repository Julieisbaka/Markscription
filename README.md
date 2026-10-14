# Markscription

Write Markdown in a book and quill, then sign it to read formatted text.

## Installation

Requires Minecraft **26.3**, Fabric Loader **0.19.5 or newer**, and Java **25**.

- **Client only:** renders Markdown in existing plain-text written books, including
  books on lecterns, without changing their stored contents. Works on vanilla servers.
- **Server only:** formats books when players sign them. Formatting is stored as vanilla
  text components, so unmodded clients can read it. Existing books are not rewritten.
- **Both:** supports both behaviors without interpreting already formatted pages twice.
- **Single player:** installing on the client also enables formatting when signing.

Book-and-quill editing stays vanilla: you see Markdown source until you sign.
The server preserves vanilla text filtering and separately formats raw and filtered pages.
Previously formatted rich-text pages (including command-created books) are left untouched.

## Supported Markdown

```markdown
# A heading
**Bold**, *italic*, ***both***, and ~~strikethrough~~.
Inline `code` and [links](https://minecraft.net).

- First item
- Second item
  - Nested item

1. Numbered item
2. Another item

> A quotation

---
```

Fenced and indented code blocks, reference links, escapes, and hard line breaks
are also supported. Single newlines remain newlines, useful for book layouts.
Headings are bold rather than larger; code uses Minecraft's uniform font.
Images show their alt text only. HTML is displayed as text, never executed.
Only absolute HTTP/HTTPS links become clickable, using Minecraft's normal link handling.
Tables, embedded images, and task checkboxes are not implemented.

### Syntax highlighting

Labeled fenced code blocks support **Java** (`java`), **JavaScript** (`javascript`, `js`),
**Python** (`python`, `py`), and **JSON** (`json`). Labels are case-insensitive.
Keywords, strings, comments, and numbers use dark colors suitable for book pages.
Inline code, indented code, and unknown/unlabeled fences keep plain code formatting.

````markdown
```java
int count = 42; // A comment
```
````

This is a small, single-pass lexical highlighter, not a full language parser:
JavaScript template strings are colored as strings without interpolation highlighting,
and regex literals and Python string prefixes/interpolation are not specially parsed.
Source text is never changed by highlighting. If colors make a page too large to store,
the renderer retries with plain code styling before falling back to the original page.
Server-side highlighting is saved as vanilla components and is visible to vanilla readers.

## Client settings

The client creates `config/markscription-client.json` in your Minecraft instance:

```json
{
  "markdown": true,
  "bold": true,
  "italic": true,
  "strikethrough": true,
  "headings": true,
  "code": true,
  "links": true,
  "syntaxHighlighting": true
}
```

Set a value to `false` to disable it. Missing settings default to `true`.
`markdown: false` shows the original page source. Individual toggles disable styling,
not Markdown parsing: for example, disabling bold shows the label without `**` markers;
disabling links shows labels without clickable actions. Heading styling is independent
of bold emphasis. Disabling code styling also disables highlighting.

For a standard settings screen, optionally install **Mod Menu 21.0.0+** and
**Cloth Config 26.3.159+** for Fabric 26.3, plus their required dependencies.
Open Mods > Markscription > Configure. The screen uses Cloth Config's controls;
neither library is bundled or required by Markscription. If either is missing,
edit the JSON file instead. File edits are loaded on client startup; screen changes
apply to books opened afterward (close and reopen an open book).
Malformed files are logged with an error, left untouched, and defaults are used.

These are **local rendering settings only**. They do not affect dedicated servers,
formatting when signing (including in single player), or existing rich-text/server-baked
pages. Server-formatted books have no original Markdown source to selectively re-render.

### Book limits

Each original page is formatted independently. There is no automatic repagination:
Minecraft's normal page size, wrapping, and clipping still apply. Keep pages short,
especially when using bold text or lists. Formatting at signing is permanent, just like
signing a vanilla book; keep a source copy if needed. Escaped Markdown and code are not
reinterpreted on modded clients because baked pages carry an explicit vanilla default style.

Pages exceeding Minecraft's component serialization limit or the renderer's nesting
limit are kept unchanged, with a warning in the game/server log.

## Development

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
.\gradlew.bat runServer
```

The build runs JUnit tests and produces the installable JAR and a sources JAR in `build/libs`.
