package com.linkbyte.everscript;

import java.util.*;

import static com.linkbyte.everscript.TokenType.*;

class Parser {
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        return statements;
    }

    private Expr expression() {
        return assignment();
    }

    private Stmt declaration() {
        try {
            if (match(CLASS)) return classDeclaration();
            if (match(ABSTRACT)) return abstractClassDeclaration();
            if (match(FUN)) return function("function");
            if (match(VAR)) return varDeclaration();
            if (match(ENUM)) return enumDeclaration();
            if (match(IMPORT)) return importDeclaration();
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt classDeclaration() {
        Token name = consume(IDENTIFIER, "Expected class name.");

        Expr.Variable superclass = null;
        if (match(INHERITS)) {
            consume(IDENTIFIER, "Expected superclass name.");
            superclass = new Expr.Variable(previous());
        }

        consume(COLON, "Expected ':' before '{'.");
        consume(LEFT_BRACE, "Expected '{' before class body.");

        List<Stmt.Function> methods = new ArrayList<>();
        List<Stmt.Function> staticMethods = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            boolean isStatic = match(STATIC);
            (isStatic ? staticMethods : methods).add(function("method"));
        }

        consume(RIGHT_BRACE, "Expected '}' after class body.");

        return new Stmt.Class(name, superclass, methods, staticMethods);
    }

    private Stmt abstractClassDeclaration() {
        throw error(peek(), "Abstract classes are not implemented.");
    }

    private Stmt statement() {
        if (match(FOR)) return forStatement();
        if (match(IF)) return ifStatement();
        if (match(PRINT)) return printStatement();
        if (match(RETURN)) return returnStatement();
        if (match(WHILE)) return whileStatement();
        if (match(LEFT_BRACE)) return new Stmt.Block(block());
        if (match(THROW)) return throwStatement();
        return expressionStatement();
    }

    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expected '(' after 'for'.");
        Stmt initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (match(VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }
        Expr condition = null;
        if (!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expected ';' after loop condition.");
        Expr increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expected ')' after for clauses.");
        Stmt body = statement();
        if (increment != null) {
            body = new Stmt.Block(Arrays.asList(
                    body,
                    new Stmt.Expression(increment)));
        }
        if (condition == null) condition = new Expr.Literal(true);
        body = new Stmt.While(condition, body);
        if (initializer != null) {
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }
        return body;
    }

    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expected '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expected ')' after if condition.");
        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }
        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expected ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }
        consume(SEMICOLON, "Expected ';' after return value.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expected variable name.");
        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }
        consume(SEMICOLON, "Expected ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt enumDeclaration() {
        Token name = consume(IDENTIFIER, "Expected 'enum' name.");
        consume(COLON, "Expected ':' before '{'.");
        consume(LEFT_BRACE, "Expected '{' before 'enum' body.");
        Map<Token, String> properties = new HashMap<>();
        if (!check(RIGHT_BRACE)) {
            do {
                if (properties.size() >= 256) {
                    throw error(peek(), "Cannot have more than 256 properties.");
                }
                Token property = consume(IDENTIFIER, "Expected property name.");
                properties.put(property, property.lexeme);
            } while (match(COMMA));
        }
        consume(RIGHT_BRACE, "Expected '}' after 'enum' body.");
        consume(SEMICOLON, "Expected ';' after 'enum' declaration.");
        return new Stmt.Enum(name, properties);
    }

    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expected '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expected ')' after condition.");
        Stmt body = statement();
        return new Stmt.While(condition, body);
    }

    private Stmt importDeclaration() {
        Token keyword = previous();
        Expr module = expression();
        consume(AS, "Expected 'as' after module name.");
        Token namespace = consume(IDENTIFIER, "Expected a namespace identifier.");
        consume(SEMICOLON, "Expected ';' after module name.");
        return new Stmt.Import(keyword, module, namespace);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expected ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private Stmt.Function function(String kind) {
        Token name = consume(IDENTIFIER, "Expected " + kind + " name.");
        return new Stmt.Function(name, functionBody(kind));
    }

    private Expr.Function functionBody(String kind) {
        List<Token> params = null;

        if (!kind.equals("method") || check(LEFT_PAREN)) {
            consume(LEFT_PAREN, "Expected '(' after " + kind + " name.");
            params = new ArrayList<>();
            if (!check(RIGHT_PAREN)) {
                do {
                    if (params.size() >= 256) {
                        throw error(peek(), "Cannot have more than 256 parameters.");
                    }
                    params.add(consume(IDENTIFIER, "Expected parameter name"));
                } while (match(COMMA));
            }
            consume(RIGHT_PAREN, "Expected ')' after parameters.");
        }

        consume(FAT_ARROW, "Expected '=>' before '{'.");
        consume(LEFT_BRACE, "Expected '{' before " + kind + " body.");
        List<Stmt> body = block();

        return new Expr.Function(params, body);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }
        consume(RIGHT_BRACE, "Expected '}' after block.");
        return statements;
    }

    private Stmt throwStatement() {
        Token keyword = previous();
        Expr thrown = expression();
        consume(SEMICOLON, "Expected ';' after 'throw' statement.");
        return new Stmt.Throw(keyword, thrown);
    }

    private Expr assignment() {
        Expr expr = or();
        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();
            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            } else if (expr instanceof Expr.Get) {
                Expr.Get get = (Expr.Get) expr;
                return new Expr.Set(get.object, get.name, value);
            } else if (expr instanceof Expr.IndexGet) {
                Expr.IndexGet get = ((Expr.IndexGet) expr);
                return new Expr.IndexSet(get.indexee, get.bracket, get.index, value);
            }
            throw error(equals, "Invalid assignment target.");
        }
        return expr;
    }

    private Expr or() {
        Expr expr = and();
        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr and() {
        Expr expr = equality();
        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = addition();
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = addition();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr addition() {
        Expr expr = multiplication();
        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = multiplication();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr multiplication() {
        Expr expr = unary();
        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return call();
    }

    private Expr array() {
        List<Expr> elements = new ArrayList<>();
        if (!check(RIGHT_BRACKET)) {
            do {
                elements.add(expression());
            } while (match(COMMA));
        }
        Token bracket = consume(RIGHT_BRACKET, "Expected ']' after list.");
        return new Expr.Array(bracket, elements);
    }

    private Expr.Dictionary dictionary() {
        Token name = backwards(3);

        Map<Token, Object> properties = new HashMap<>();

        if (match(RIGHT_BRACE)) return new Expr.Dictionary(name, properties);

        do {
            if (match(STRING, IDENTIFIER, NUMBER)) {
                Token key = previous();
                if (match(COLON)) {
                    Expr value = expression();
                    properties.put(key, value);
                } else {
                    Expr value = new Expr.Variable(key);
                    properties.put(key, value);
                }
            } else {
                throw error(this.peek(), "Expected number, string or identifier as dictionary key, unexpected token '" + peek().lexeme + "'.");
            }
        } while (match(COMMA));

        consume(RIGHT_BRACE, "Expected '}' after object literal.");
        return new Expr.Dictionary(name, properties);
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 255) throw error(peek(), "Cannot have more than 255 arguments.");
                arguments.add(expression());
            } while (match(COMMA));
        }
        Token paren = consume(RIGHT_PAREN, "Expected ')' after arguments.");
        return new Expr.Call(callee, paren, arguments);
    }

    private Expr call() {
        Expr expr = primary();
        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if (match(DOT, OPTIONAL_CHAINING)) {
                Token operator = previous();
                Token name = consume(IDENTIFIER, "Expected property name after '.'.");
                expr = new Expr.Get(expr, name, operator);
            } else if (match(LEFT_BRACKET)) {
                expr = finishIndexGet(expr);
            } else {
                break;
            }
        }
        return expr;
    }

    private Expr finishIndexGet(Expr indexee) {
        Expr index = expression();
        Token bracket = consume(RIGHT_BRACKET, "Expected ']' after index.");
        return new Expr.IndexGet(indexee, bracket, index);
    }

    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);
        if (match(NUMBER, STRING)) return new Expr.Literal(previous().literal);
        if (match(SUPER)) {
            Token keyword = previous();
            consume(DOT, "Expected '.' after 'super'.");
            Token method = consume(IDENTIFIER, "Expected superclass method name.");
            return new Expr.Super(keyword, method);
        }
        if (match(THIS)) return new Expr.This(previous());
        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }
        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expected ')' after expression.");
            return new Expr.Grouping(expr);
        }
        if (match(LEFT_BRACKET)) {
            return array();
        }

        if (check(FUN) && !checkNext(IDENTIFIER)) {
            advance();
            return functionBody("function");
        }

        if (match(LEFT_BRACE)) {
            return dictionary();
        }

        throw error(peek(), "Expected expression.");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private boolean checkNext(TokenType type) {
        if (isAtEnd()) return false;
        if (tokens.get(current + 1).type == EOF) return false;
        return tokens.get(current + 1).type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token backwards(int range) {
        return tokens.get(current - range);
    }

    private Token forwards(int range) {
        return tokens.get(current + range);
    }

    private ParseError error(Token token, String message) {
        EverScript.error(token, "ParseError", message);
        return new ParseError();
    }

    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;
            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case CONST:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                case ENUM:
                case THROW:
                case STATIC:
                case INHERITS:
                case IMPORT:
                    return;
            }
            advance();
        }
    }
}
