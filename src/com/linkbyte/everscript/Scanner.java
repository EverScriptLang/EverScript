package com.linkbyte.everscript;

import java.io.File;
import java.util.*;

import static com.linkbyte.everscript.TokenType.*;

class Scanner {
    private static final Map<String, TokenType> keywords;
    private final List<Token> tokens = new ArrayList<>();
    private final StringBuilder source;
    private int line, col, start, current;
    private final String directory, file;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fn", FUN);
        keywords.put("if", IF);
        keywords.put("null", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("let", VAR);
        keywords.put("while", WHILE);
        keywords.put("implements", IMPLEMENTS);
        keywords.put("static", STATIC);
        keywords.put("import", IMPORT);
        keywords.put("try", TRY);
        keywords.put("catch", CATCH);
        keywords.put("finally", FINALLY);
        keywords.put("throw", THROW);
        keywords.put("inherits", INHERITS);
        keywords.put("enum", ENUM);
        keywords.put("as", AS);
        keywords.put("const", CONST);
        keywords.put("trait", TRAIT);
        keywords.put("abstract", ABSTRACT);
        keywords.put("private", PRIVATE);
        keywords.put("native", NATIVE);
    }

    Scanner(String file, String source) {
        String[] fileLoc;
        if (File.separator.equals("\\")) fileLoc = file.split("\\\\");
        else fileLoc = file.split(File.separator);

        String dirName = "";
        for (int i = 0; i < fileLoc.length - 1; i++) {
            dirName += fileLoc[i] + File.separator;
        }
        String fileName = fileLoc[fileLoc.length - 1];

        this.directory = dirName;
        this.file = fileName;
        this.source = new StringBuilder(source);
        this.start = this.current = this.col = 0;
        this.line = 1;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(directory, file, EOF, null, "EOF", line, col));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(':
                addToken(LEFT_PAREN);
                break;

            case ')':
                addToken(RIGHT_PAREN);
                break;

            case '{':
                addToken(LEFT_BRACE);
                break;

            case '}':
                addToken(RIGHT_BRACE);
                break;

            case '[':
                addToken(LEFT_BRACKET);
                break;

            case ']':
                addToken(RIGHT_BRACKET);
                break;

            case ',':
                addToken(COMMA);
                break;

            case '.':
                addToken(DOT);
                break;

            case '-':
                addToken(match('-') ? MINUS_MINUS : match('>') ? ARROW : MINUS);
                break;

            case '+':
                addToken(match('+') ? PLUS_PLUS : PLUS);
                break;

            case '?':
                if (match('.')) addToken(OPTIONAL_CHAINING);
                else addToken(QUESTION);
                break;

            case ';':
                addToken(SEMICOLON);
                break;

            case '*':
                addToken(STAR);
                break;

            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;

            case '=':
                if (match('=')) addToken(EQUAL_EQUAL);
                else if (match('>')) addToken(FAT_ARROW);
                else addToken(EQUAL);
                break;

            case '<':
                if (match('=')) addToken(LESS_EQUAL);
                else if (match('-')) addToken(REVERSE_ARROW);
                else addToken(LESS);
                break;

            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;

            case ':':
                addToken(match(':') ? COLON_COLON : COLON);
                break;

            case '&':
                if (match('&')) addToken(AMPERSAND_AMPERSAND);
                else addToken(AMPERSAND);
                break;

            case '|':
                if (match('|')) addToken(PIPE_PIPE);
                else addToken(PIPE);
                break;

            case '/':
                if (match('/')) while (peek() != '\n' && !isAtEnd()) advance();
                else addToken(SLASH);
                break;

            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                col = 0;
                line++;
                break;

            case '"':
                case '\'':
                    case '`': string(c); break;

            default:
                if (isDigit(c)) number();
                else if (isAlpha(c)) identifier();
                else EverScript.error(file, line, col, "SyntaxError", "Unexpected character.");
                break;
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    private void number() {
        while (isDigit(peek())) advance();
        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) advance();
        }
        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void string(char quote) {
        while (peek() != quote && !isAtEnd()) {
            char first = peek();
            if (first == '\n') {
                line++;
                col = 0;
            }
            if (first == '\\') {
                char second = peekNext();
                if (second == '\"') {
                    source.deleteCharAt(current);
                    source.setCharAt(current, '\"');
                } else if (second == '\\') {
                    source.deleteCharAt(current);
                    source.setCharAt(current, '\\');
                } else if (second == 'b') {
                    source.deleteCharAt(current);
                    source.setCharAt(current, '\b');
                } else if (second == 'r') {
                    source.deleteCharAt(current);
                    source.setCharAt(current, '\r');
                } else if (second == 'n') {
                    source.deleteCharAt(current);
                    source.setCharAt(current, '\n');
                } else if (second == 't') {
                    source.deleteCharAt(current);
                    source.setCharAt(current, '\t');
                }
            }
            advance();
        }
        if (isAtEnd()) {
            EverScript.error(file, line, col, "SyntaxError", "Unterminated string; expected closing '" + quote + "'.");
            return;
        }
        advance();
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        col++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        current++;
        col++;
        return source.charAt(current - 1);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(directory, file, type, text, literal, line, col));
    }
}
