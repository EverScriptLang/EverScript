package com.linkbyte.everscript;

import java.util.*;

abstract class Stmt {
  interface Visitor<R> {
    R visitBlockStmt(Block stmt);
    R visitClassStmt(Class stmt);
    R visitExpressionStmt(Expression stmt);
    R visitFunctionStmt(Function stmt);
    R visitIfStmt(If stmt);
    R visitReturnStmt(Return stmt);
    R visitVarStmt(Var stmt);
    R visitWhileStmt(While stmt);
    R visitImportStmt(Import stmt);
    R visitEnumStmt(Enum stmt);
    R visitThrowStmt(Throw stmt);
    R visitTryStmt(Try stmt);
    R visitCatchStmt(Catch stmt);
  }

  static class Block extends Stmt {
    Block(List<Stmt> statements) {
      this.statements = statements;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBlockStmt(this);
    }

    final List<Stmt> statements;
  }

  static class Class extends Stmt {
    Class(Token name, Expr.Variable superclass, List<Stmt.Function> methods, List<Stmt.Function> staticMethods) {
      this.name = name;
      this.superclass = superclass;
      this.methods = methods;
      this.staticMethods = staticMethods;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitClassStmt(this);
    }

    final Token name;
    final Expr.Variable superclass;
    final List<Stmt.Function> methods;
    final List<Stmt.Function> staticMethods;
  }

  static class Expression extends Stmt {
    Expression(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }

    final Expr expression;
  }

  static class Function extends Stmt {
    Function(Token name, Expr.Function function) {
      this.name = name;
      this.function = function;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitFunctionStmt(this);
    }

    final Token name;
    final Expr.Function function;
  }

  static class If extends Stmt {
    If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
      this.condition = condition;
      this.thenBranch = thenBranch;
      this.elseBranch = elseBranch;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitIfStmt(this);
    }

    final Expr condition;
    final Stmt thenBranch;
    final Stmt elseBranch;
  }

  static class Return extends Stmt {
    Return(Token keyword, Expr value) {
      this.keyword = keyword;
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitReturnStmt(this);
    }

    final Token keyword;
    final Expr value;
  }

  static class Var extends Stmt {
    Var(Token name, Expr initializer) {
      this.name = name;
      this.initializer = initializer;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVarStmt(this);
    }

    final Token name;
    final Expr initializer;
  }

  static class While extends Stmt {
    While(Expr condition, Stmt body) {
      this.condition = condition;
      this.body = body;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitWhileStmt(this);
    }

    final Expr condition;
    final Stmt body;
  }

  static class Import extends Stmt {
    Import(Token keyword, Expr module, Token namespace) {
      this.keyword = keyword;
      this.module = module;
      this.namespace = namespace;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitImportStmt(this);
    }

    final Token keyword;
    final Expr module;
    final Token namespace;
  }

  static class Enum extends Stmt {
    Enum(Token name, List<Token> properties) {
      this.name = name;
      this.properties = properties;
    }

    <R> R accept(Visitor<R> visitor) { return visitor.visitEnumStmt(this); }

    final Token name;
    final List<Token> properties;
  }

  static class Throw extends Stmt {
    Throw(Token keyword, Expr thrown) {
      this.keyword = keyword;
      this.thrown = thrown;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitThrowStmt(this);
    }

    final Token keyword;
    final Expr thrown;
  }

  static class Try extends Stmt {
    Try(Token keyword, Stmt body, List<Stmt.Catch> catches, Stmt finallyStmt) {
      this.keyword = keyword;
      this.body = body;
      this.catches = catches;
      this.finallyStmt = finallyStmt;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitTryStmt(this);
    }

    final Token keyword;
    final Stmt body;
    final List<Stmt.Catch> catches;
    Stmt finallyStmt;
  }

  static class Catch extends Stmt {
    Catch(Token keyword, List<Token> errors, Token identifier, Stmt body) {
      this.keyword = keyword;
      this.errors = errors;
      this.identifier = identifier;
      this.body = body;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitCatchStmt(this);
    }

    final Token keyword;
    final List<Token> errors;
    final Token identifier;
    final Stmt body;
  }

  abstract <R> R accept(Visitor<R> visitor);
}