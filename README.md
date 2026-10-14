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
Tables, embedded images, task checkboxes, and syntax highlighting are not yet implemented.

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
