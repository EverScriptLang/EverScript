package com.linkbyte.everscript;

class Token {
  final String directory;
  final String file;
  final TokenType type;
  final String lexeme;
  final Object literal;
  final int line, col;

  Token(String directory, String file, TokenType type, String lexeme, Object literal, int line, int col) {
    this.directory = directory;
    this.file = file;
    this.type = type;
    this.lexeme = lexeme;
    this.literal = literal;
    this.line = line;
    this.col = col;
  }

  public String toString() {
    return "< " + type + ", " + lexeme + ", " + literal + " >";
  }
}
