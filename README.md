<p align="center">
  <img src="src/main/resources/assets/markscription/icon.png" alt="Markscription: the Markdown symbol on a Minecraft book" width="128">
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
  Java, JavaScript, Python, JSON, Minecraft commands, SNBT, and TOML.
- **Clickable web links:** link readers to websites using HTTP or HTTPS.
- **Read wherever you like:** works with books in your hand and on lecterns.
- **Your preferred style:** turn individual formatting features on or off for local reading.
- **Shareable code themes:** choose your own syntax colors, import a friend's palette,
  or export yours. Servers can choose colors for newly signed books too.
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

Supported labels: `java`, `javascript` or `js`, `python` or `py`, `json`,
`mcfunction` (also `minecraft` or `mccommand`), `snbt`, and `toml`.
Other languages still appear as plain code blocks.

Use `mcfunction` for command examples, `snbt` for Minecraft data, and `toml` for
configuration snippets. Highlighting is intentionally simple, not a syntax validator;
unrecognized syntax remains readable and is never executed.

## Single player and multiplayer

| Where you install it | Book formatting | Settings and code themes |
| --- | --- | --- |
| **Single player** | Newly signed books keep their formatting. Older plain-text Markdown books are formatted locally when you read them. | Choose local formatting features and colors, and import or export themes. A separate server theme controls colors saved when signing. |
| **Client only** | Read plain-text Markdown books with formatting, even on vanilla servers. Other players need the mod to see that formatting too. | Choose local formatting features and colors, and import or export themes. Your preferences only affect your own view. |
| **Server only** | Newly signed books keep their formatting for everyone, including vanilla players. Existing books aren't changed. | Server owners choose the colors saved in newly signed books. Vanilla readers have no local formatting controls or theme import/export screen. |
| **Both** | Newly signed books keep their formatting for everyone. The client also formats older plain-text Markdown books locally. | Server colors apply to newly signed books; client feature switches, colors, and theme import/export apply only to local formatting of plain-text books. |

On a modded server, formatting in newly signed books stays with the book, even if
the mod is removed later. Installing the client mod doesn't let you disable or
recolor formatting already saved by the server. The in-game client settings and
theme import/export screen require Mod Menu and Cloth Config.

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

### Code themes

In **Configure > Syntax theme**, choose colors for plain code, keywords, strings,
comments, and numbers. Pick colors that are easy to read against book pages.
The plain-code color also applies to inline code and blocks without highlighting.

To share a theme:

1. Choose a filename such as `my-theme.json`.
2. Set **Theme file action on Save** to **Export theme**, then **Save & Done**.
3. Share the file from `config/markscription/themes` in your Minecraft instance.

To use someone else's theme, put their JSON file in that folder, enter its filename,
select **Import theme**, then **Save & Done**. Import replaces the color choices, not
your other settings. Reopen settings to view the imported colors and reopen your book
to see them. Export requires a new filename so it won't overwrite an existing theme.
File actions reset to **None** each time you open the screen.

Without the settings-screen mods, edit the `theme` object in
`config/markscription-client.json`. Its five colors use the same format as theme files:

```json
{
  "plain": "#333333",
  "keyword": "#6F238A",
  "string": "#24662D",
  "comment": "#616158",
  "number": "#244F9E"
}
```

All five colors are required, written as `#RRGGBB`. Theme files contain just this object,
not the rest of your client settings. Invalid files are reported rather than imported;
check the game log for details. Existing client configs keep the original colors.

**For server owners:** after starting the mod once, edit
`config/markscription-server-theme.json` and restart. It uses exactly the theme format
above, so you can copy an exported client theme into it. The server's colors are saved
into newly signed books for everyone, including vanilla readers. The same setting is
used when signing in single player.

Client and server themes are independent: client preferences never recolor books
already formatted by a server, and changing a server theme doesn't rewrite older books.

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
