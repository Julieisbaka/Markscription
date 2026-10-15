# Changelog

## 0.3.0 - 10/14/26

- Added configurable syntax colors for plain code, keywords, strings, comments, and numbers.
- Added a Syntax theme tab with color controls in the optional Mod Menu/Cloth Config screen.
- Added client theme import and export using JSON files in `config/markscription/themes`.
  Choose a filename and an import/export action, then save. Exports never overwrite files.
- Added independent server themes in `config/markscription-server-theme.json`.
  Server colors are saved when signing, including in single player, and work for vanilla readers.
- Existing client configs keep their settings and default palette; previously formatted
  books retain their saved colors.
- Theme preferences respect disabled highlighting and the existing book-size fallback.
- The default palette is bundled as a JSON theme using the same format as shared themes.
- Added translated theme controls and feedback in all nine supported languages.

## 0.2.1 - 10/14/26

- Added Minecraft command highlighting using `mcfunction`, `minecraft`, or
  `mccommand` code fences: command names, selectors, strings, numbers, and full-line comments.
- Added SNBT highlighting for quoted strings, booleans, and numeric type suffixes.
- Added TOML highlighting for strings, multiline strings, booleans, numbers, and comments.
- Updated syntax-highlighting tooltips in all supported languages.

## 0.2.0 - 10/14/26

- Syntax highlighting
- Mod menu integration
- More configuration options

## 0.1.0 - 10/14/26

- Initial release