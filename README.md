<p align="center">
  <img src="https://raw.githubusercontent.com/Julieisbaka/Markscription/main/src/main/resources/assets/markscription/icon.png" alt="Markscription: the Markdown symbol on a Minecraft book" width="128">
</p>

# Markscription

**Give your Minecraft books a little more character.**

Markscription brings Markdown to written books. Create journals with bold headings,
organize guides with lists, add links, or share snippets of colorful code.
Write in a book and quill as usual, then sign it to see your formatting.

## Features

- **Expressive text:** bold, italic, and strikethrough.
- **Organized pages:** headings, numbered lists, bullet lists, and quotations.
- **Code-friendly books:** inline code and code blocks, with syntax highlighting for
  Java, JavaScript, Python, and JSON.
- **Clickable web links:** link readers to websites using HTTP or HTTPS.
- **Read wherever you like:** works with books in your hand and on lecterns.
- **Your preferred style:** turn individual formatting features on or off for local reading.
- **Flexible multiplayer support:** use it on your client, your server, or both.

## Installation

For **Minecraft Java Edition 26.3** with **Fabric Loader 0.19.5 or newer** and **Java 25**.

1. Install Fabric for Minecraft 26.3.
2. Put the Markscription mod JAR in your Minecraft instance's `mods` folder.
3. Launch the game and grab a book and quill!

Markscription itself does **not** require Fabric API.

Want an in-game settings screen? Also install compatible versions of
[Mod Menu](https://modrinth.com/mod/modmenu) and
[Cloth Config](https://modrinth.com/mod/cloth-config), along with their required
dependencies.

## Writing your first book

Enter Markdown in a book and quill. You'll see the original text while editing;
the formatting appears when you read the signed book.

| Write this | Get this |
| --- | --- |
| `**bold**` | **bold** |
| `*italic*` | *italic* |
| `~~strikethrough~~` | ~~strikethrough~~ |
| `# A heading` | A bold heading |
| `- An item` | A bullet-list item |
| `1. An item` | A numbered-list item |
| `> A quotation` | A quotation |
| `` `code` `` | Monospaced code |
| `[Minecraft](https://minecraft.net)` | A clickable link |

Start headings, list items, and quotations on a new line. Leave a blank line between
paragraphs. Headings use bold text rather than a larger font.

### Colorful code blocks

Surround a code snippet with three backticks and put its language after the opening
backticks:

````markdown
```java
int count = 42; // A comment
```
````

Supported labels: `java`, `javascript` or `js`, `python` or `py`, and `json`.
Other languages still appear as plain code blocks.

## Single player and multiplayer

| Where you install it | What you get |
| --- | --- |
| **Single player** | Formatting when you sign books, plus local formatting for older plain-text Markdown books. |
| **Client only** | Read plain-text Markdown books with formatting, even on vanilla servers. Other players need the mod to see that formatting too. |
| **Server only** | Newly signed books keep their formatting for everyone, including vanilla players. Existing books aren't changed. |
| **Both** | The best coverage: locally formatted older books and newly signed books that everyone can read with formatting. |

On a modded server, formatting in newly signed books stays with the book, even if
the mod is removed later.

## Make it yours

With Mod Menu and Cloth Config installed, open **Mods > Markscription > Configure**.
Choose whether to show bold, italic, strikethrough, heading styles, code styles,
clickable links, and syntax highlighting. You can also switch off local Markdown
rendering entirely to read the original text.

Without the settings-screen mods, edit `config/markscription-client.json` in your
Minecraft instance and restart the game. Set an option to `false` to disable it.
When changing settings in-game, close and reopen your book to see the changes.

These preferences only affect local reading of plain-text Markdown books. They don't
change formatting when signing or remove formatting already saved by a server.

The settings screen supports English, Spanish, French, German, Brazilian Portuguese,
Japanese, Simplified Chinese, Korean, and Russian, following your Minecraft language.

## Good to know

- **Keep pages short.** Minecraft's usual book space still applies; long content
  doesn't automatically flow onto another page.
- **Signing is permanent.** Keep an unsigned copy if you'd like to save your Markdown source.
- **Unicode text is welcome.** Character appearance depends on Minecraft's fonts and
  your resource packs.
- **Existing styled books stay as they are.** Markscription leaves their formatting alone.
- **No embedded images, tables, or interactive checkboxes.** Image descriptions appear
  as text, and HTML appears literally.
- **Keep examples literal.** Use inline code or escape Markdown characters with `\`
  when you want the symbols themselves to appear.
