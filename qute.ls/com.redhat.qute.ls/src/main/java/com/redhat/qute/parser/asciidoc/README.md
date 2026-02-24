# AsciiDoc Scanner & Parser

This package contains a fault-tolerant scanner and parser for AsciiDoc documents, similar to the YAML implementation.

## Architecture

### Scanner (`AsciidocScanner`)
- **Type**: Lexical analyzer (tokenizer)
- **Extends**: `AbstractScanner<AsciidocTokenType, AsciidocScannerState>`
- **Features**:
  - Fault-tolerant scanning (continues on errors)
  - Recognizes AsciiDoc structures (titles, lists, blocks, tables)
  - Handles inline formatting (bold, italic, monospace, etc.)
  - Supports comments, links, and macros
  - Tracks scanner state for context-aware tokenization

### Parser (`AsciidocParser`)
- **Type**: Syntax analyzer (builds AST)
- **Features**:
  - Fault-tolerant parsing
  - Builds Abstract Syntax Tree (AST)
  - Handles nested structures (sections, lists, blocks)
  - Supports all major AsciiDoc elements

## Token Types (`AsciidocTokenType`)

### Document Structure
- `DocumentTitle` - Document title (= Title)
- `SectionTitle` - Section headings (==, ===, etc.)

### Lists
- `UnorderedListItem` - Bullet lists (*, **, etc.)
- `OrderedListItem` - Numbered lists (., .., etc.)
- `DescriptionListTerm` - Description list terms (term::)

### Blocks
- `BlockDelimiter` - Delimited blocks (----, ****, etc.)
- `TableDelimiter` - Table markers (|===)

### Inline Formatting
- `Bold` - Bold text (*text* or **text**)
- `Italic` - Italic text (_text_ or __text__)
- `Monospace` - Monospace text (`text`)
- `Superscript` - Superscript (^text^)
- `Subscript` - Subscript (~text~)

### Links & References
- `Link` - URLs and link macros
- `CrossReference` - Internal references (<<id>>)
- `Anchor` - Anchor definitions ([[id]])

### Attributes & Macros
- `AttributeEntry` - Attribute definitions (:attr:)
- `AttributeReference` - Attribute usage ({attr})
- `BlockMacro` - Block-level macros (image::)
- `InlineMacro` - Inline macros (kbd:[], btn:[])

### Other
- `Admonition` - NOTE:, TIP:, IMPORTANT:, WARNING:, CAUTION:
- `Include` - Include directives (include::)
- `LineComment` - Single-line comments (//)
- `Text` - Plain text content

## Scanner States (`AsciidocScannerState`)

- `WithinContent` - Normal content scanning
- `WithinTitle` - Inside a title
- `WithinAttributeEntry` - Inside attribute definition
- `WithinBlockDelimiter` - Inside delimited block
- `WithinListItem` - Inside list item
- `WithinLineComment` - Inside comment
- `WithinInlineFormatting` - Inside formatted text
- `WithinLink` - Inside link
- `WithinMacro` - Inside macro
- `WithinTable` - Inside table
- `WithinAdmonition` - Inside admonition
- `WithinPassthrough` - Inside passthrough block
- `AfterNewline` - Just after newline (detects block structures)

## AST Nodes

### Node Hierarchy
All nodes extend `AsciidocNode` which extends `NodeBase<AsciidocNode>`.

### Node Types (`AsciidocNodeKind`)
- `AsciidocDocument` - Root node
- `AsciidocSection` - Section with heading
- `AsciidocParagraph` - Paragraph
- `AsciidocList` - Ordered or unordered list
- `AsciidocListItem` - List item
- `AsciidocBlock` - Delimited block
- `AsciidocTable` - Table
- `AsciidocText` - Plain text
- `AsciidocComment` - Comment

### Visitor Pattern
Use `AsciidocASTVisitor` to traverse the AST:

```java
public class MyVisitor extends AsciidocASTVisitor {
    @Override
    public boolean visit(AsciidocSection node) {
        // Process section
        return true; // Visit children
    }
    
    @Override
    public void endVisit(AsciidocSection node) {
        // Cleanup after visiting section
    }
}
```

## Usage Examples

### Parse AsciiDoc Document
```java
// From string
AsciidocDocument doc = AsciidocParser.parse(content, uri);

// From TextDocument
AsciidocDocument doc = AsciidocParser.parse(textDocument);

// With cancel checker
AsciidocDocument doc = AsciidocParser.parse(
    textDocument, 
    0, 
    textDocument.getText().length(), 
    cancelChecker
);
```

### Scan Tokens
```java
Scanner<AsciidocTokenType, AsciidocScannerState> scanner = 
    AsciidocScanner.createScanner(content);

AsciidocTokenType token = scanner.scan();
while (token != AsciidocTokenType.EOS) {
    int offset = scanner.getTokenOffset();
    int end = scanner.getTokenEnd();
    String text = scanner.getTokenText();
    
    // Process token
    
    token = scanner.scan();
}
```

### Traverse AST
```java
AsciidocDocument doc = AsciidocParser.parse(content, uri);

doc.accept(new AsciidocASTVisitor() {
    @Override
    public boolean visit(AsciidocSection node) {
        System.out.println("Section level: " + node.getLevel());
        System.out.println("Section title: " + node.getTitle());
        return true;
    }
    
    @Override
    public boolean visit(AsciidocParagraph node) {
        System.out.println("Paragraph: " + node.getText());
        return true;
    }
});
```

## Supported AsciiDoc Features

### ✅ Fully Supported
- Document and section titles
- Paragraphs
- Unordered lists (*, **, etc.)
- Ordered lists (., .., etc.)
- Delimited blocks (----, ****, ====, etc.)
- Tables (|===)
- Line comments (//)
- Inline formatting (bold, italic, monospace)
- Links (http://, link:)
- Attributes (:attr:, {attr})
- Admonitions (NOTE:, TIP:, etc.)
- Include directives

### ⚠️ Partially Supported
- Cross-references (<<id>>)
- Anchors ([[id]])
- Macros (image::, kbd:, etc.)
- Description lists
- Superscript/subscript

### ❌ Not Yet Supported
- Block comments (////)
- Complex table formatting
- Conditional directives
- Custom macros
- Bibliography
- Footnotes

## Design Principles

1. **Fault Tolerance**: Parser continues even with syntax errors
2. **Incremental Parsing**: Can parse document fragments
3. **Cancellation Support**: Respects cancel checkers for long operations
4. **Memory Efficient**: Streaming scanner, lazy AST construction
5. **LSP Ready**: Designed for Language Server Protocol features

## Comparison with YAML Parser

| Feature | YAML | AsciiDoc |
|---------|------|----------|
| Scanner States | 9 | 15 |
| Token Types | 30+ | 40+ |
| AST Node Types | 7 | 11 |
| Complexity | Medium | High |
| Use Case | Configuration | Documentation |

## Future Enhancements

- [ ] Support for more inline macros
- [ ] Better table cell parsing
- [ ] Conditional directives
- [ ] Custom macro definitions
- [ ] Bibliography support
- [ ] Footnote support
- [ ] Better error recovery
- [ ] Performance optimizations

## References

- [AsciiDoc Language Documentation](https://docs.asciidoctor.org/asciidoc/latest/)
- [AsciiDoc Syntax Quick Reference](https://docs.asciidoctor.org/asciidoc/latest/syntax-quick-reference/)