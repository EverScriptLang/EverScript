package com.linkbyte.everscript;

import java.util.*;

abstract class Expr {
  interface Visitor<R> {
    R visitAssignExpr(Assign expr);
    R visitBinaryExpr(Binary expr);
    R visitCallExpr(Call expr);
    R visitGetExpr(Get expr);
    R visitGroupingExpr(Grouping expr);
    R visitLiteralExpr(Literal expr);
    R visitLogicalExpr(Logical expr);
    R visitSetExpr(Set expr);
    R visitSuperExpr(Super expr);
    R visitThisExpr(This expr);
    R visitUnaryExpr(Unary expr);
    R visitVariableExpr(Variable expr);
    R visitArrayExpr(Array expr);
    R visitIndexGetExpr(IndexGet expr);
    R visitIndexSetExpr(IndexSet expr);
    R visitFunctionExpr(Function expr);
    R visitDictionaryExpr(Dictionary expr);
  }

  static class Assign extends Expr {
    Assign(Token name, Expr value) {
      this.name = name;
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitAssignExpr(this);
    }

    final Token name;
    final Expr value;
  }

  static class Binary extends Expr {
    Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryExpr(this);
    }

    final Expr left;
    final Token operator;
    final Expr right;
  }

  static class Call extends Expr {
    Call(Expr callee, Token paren, List<Expr> arguments) {
      this.callee = callee;
      this.paren = paren;
      this.arguments = arguments;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitCallExpr(this);
    }

    final Expr callee;
    final Token paren;
    final List<Expr> arguments;
  }

  static class Get extends Expr {
    Get(Expr object, Token name, Token operator) {
      this.object = object;
      this.name = name;
      this.operator = operator;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGetExpr(this);
    }

    final Expr object;
    final Token name;
    final Token operator;
  }

  static class Grouping extends Expr {
    Grouping(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpr(this);
    }

    final Expr expression;
  }

  static class Literal extends Expr {
    Literal(Object value) {
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpr(this);
    }

    final Object value;
  }

  static class Logical extends Expr {
    Logical(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLogicalExpr(this);
    }

    final Expr left;
    final Token operator;
    final Expr right;
  }

  static class Set extends Expr {
    Set(Expr object, Token name, Expr value) {
      this.object = object;
      this.name = name;
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitSetExpr(this);
    }

    final Expr object;
    final Token name;
    final Expr value;
  }

  static class Super extends Expr {
    Super(Token keyword, Token method) {
      this.keyword = keyword;
      this.method = method;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitSuperExpr(this);
    }

    final Token keyword;
    final Token method;
  }

  static class This extends Expr {
    This(Token keyword) {
      this.keyword = keyword;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitThisExpr(this);
    }

    final Token keyword;
  }

  static class Unary extends Expr {
    Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpr(this);
    }

    final Token operator;
    final Expr right;
  }

  static class Variable extends Expr {
    Variable(Token name) {
      this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVariableExpr(this);
    }

    final Token name;
  }

  static class Array extends Expr {
    Array(Token bracket, List<Expr> elements) {
      this.bracket = bracket;
      this.elements = elements;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitArrayExpr(this);
    }

    final Token bracket;
    final List<Expr> elements;
  }

  static class IndexGet extends Expr {
    IndexGet(Expr indexee, Token bracket, Expr index) {
      this.indexee = indexee;
      this.bracket = bracket;
      this.index = index;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitIndexGetExpr(this);
    }

    final Expr indexee;
    final Token bracket;
    final Expr index;
  }

  static class IndexSet extends Expr {
    IndexSet(Expr indexee, Token bracket, Expr index, Expr value) {
      this.indexee = indexee;
      this.bracket = bracket;
      this.index = index;
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitIndexSetExpr(this);
    }

    final Expr indexee;
    final Token bracket;
    final Expr index;
    final Expr value;
  }

  static class Function extends Expr {
    Function(List<Token> params, List<Stmt> body) {
      this.params = params;
      this.body = body;
    }

    <R> R accept(Visitor<R> visitor) { return visitor.visitFunctionExpr(this); }

    final List<Token> params;
    final List<Stmt> body;
  }

  static class Dictionary extends Expr {
    Dictionary(Token name, Map<Token, Object> properties) {
      this.name = name;
      this.properties = properties;
    }

    <R> R accept(Visitor<R> visitor) { return visitor.visitDictionaryExpr(this); }

    final Token name;
    final Map<Token, Object> properties;
  }

  abstract <R> R accept(Visitor<R> visitor);
}

