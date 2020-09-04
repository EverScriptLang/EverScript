package com.linkbyte.everscript;

import java.util.*;

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
  private final Interpreter interpreter;
  private final Stack<Map<String, Boolean>> scopes = new Stack<>();
  private FunctionType currentFunction = FunctionType.NONE;

  Resolver(Interpreter interpreter) {
    this.interpreter = interpreter;
  }
  private enum FunctionType {
    NONE,
    FUNCTION,
    INITIALIZER,
    METHOD,
  }

  private enum ClassType {
    NONE,
    CLASS,
    SUBCLASS,
  }

  private enum FunctionCtx {
    DYNAMIC,
    STATIC
  }

  private ClassType currentClass = ClassType.NONE;
  private FunctionCtx currentContext = FunctionCtx.DYNAMIC;

  void resolve(List<Stmt> statements) {
    for (Stmt statement : statements) {
      resolve(statement);
    }
  }

  @Override
  public Void visitBlockStmt(Stmt.Block stmt) {
    beginScope();
    resolve(stmt.statements);
    endScope();
    return null;
  }

  @Override
  public Void visitClassStmt(Stmt.Class stmt) {
    declare(stmt.name);
    define(stmt.name);

    ClassType enclosingClass = currentClass;
    currentClass = ClassType.CLASS;

    if (stmt.superclass != null) {
      currentClass = ClassType.SUBCLASS;
      resolve(stmt.superclass);
      beginScope();
      scopes.peek().put("super", true);
    }

    beginScope();
    scopes.peek().put("this", true);

    for (Stmt.Function method : stmt.methods) {
      FunctionType declaration = FunctionType.METHOD;
      if (method.name.lexeme.equals("constructor")) {
        declaration = FunctionType.INITIALIZER;
      }
      resolveFunction(method, declaration, FunctionCtx.DYNAMIC);
    }

    for (Stmt.Function method : stmt.staticMethods) {
      FunctionType declaration = FunctionType.METHOD;
      if (method.name.lexeme.equals("init")) {
        declaration = FunctionType.INITIALIZER;
      }
      resolveFunction(method, declaration, FunctionCtx.STATIC);
    }

    endScope();

    if (stmt.superclass != null) endScope();

    currentClass = enclosingClass;

    return null;
  }

  @Override
  public Void visitEnumStmt(Stmt.Enum stmt) {
    declare(stmt.name);
    define(stmt.name);

    for (Map.Entry<Token, String> property : stmt.properties.entrySet()) {
      declare(property.getKey());
      define(property.getKey());
    }

    return null;
  }

  @Override
  public Void visitDictionaryExpr(Expr.Dictionary expr) {
    declare(expr.name);
    define(expr.name);

    for (Map.Entry<Token, Object> property : expr.properties.entrySet()) {
      declare(property.getKey());
      define(property.getKey());
    }

    return null;
  }

  @Override
  public Void visitExpressionStmt(Stmt.Expression stmt) {
    resolve(stmt.expression);
    return null;
  }

  @Override
  public Void visitFunctionStmt(Stmt.Function stmt) {
    declare(stmt.name);
    define(stmt.name);
    resolveFunction(stmt, FunctionType.FUNCTION, FunctionCtx.STATIC);
    return null;
  }

  @Override
  public Void visitThrowStmt(Stmt.Throw stmt) {
    resolve(stmt.thrown);
    return null;
  }

  @Override
  public Void visitFunctionExpr(Expr.Function expr) {
    resolveFunction(expr, FunctionType.FUNCTION, FunctionCtx.STATIC);
    return null;
  }

  @Override
  public Void visitIfStmt(Stmt.If stmt) {
    resolve(stmt.condition);
    resolve(stmt.thenBranch);
    if (stmt.elseBranch != null) resolve(stmt.elseBranch);
    return null;
  }

  @Override
  public Void visitPrintStmt(Stmt.Print stmt) {
    resolve(stmt.expression);
    return null;
  }

  @Override
  public Void visitReturnStmt(Stmt.Return stmt) {
    if (currentFunction == FunctionType.NONE) {
      EverScript.error(stmt.keyword, "SyntaxError", "Illegal return statement.");
    }
    if (stmt.value != null) {
      if (currentFunction == FunctionType.INITIALIZER) {
        EverScript.error(stmt.keyword, "SyntaxError", "Cannot return a value from an initializer.");
      }
      resolve(stmt.value);
    }
    return null;
  }

  @Override
  public Void visitVarStmt(Stmt.Var stmt) {
    declare(stmt.name);
    if (stmt.initializer != null) {
      resolve(stmt.initializer);
    }
    define(stmt.name);
    return null;
  }

  @Override
  public Void visitWhileStmt(Stmt.While stmt) {
    resolve(stmt.condition);
    resolve(stmt.body);
    return null;
  }

  @Override
  public Void visitImportStmt(Stmt.Import stmt) {
    declare(stmt.namespace);
    define(stmt.namespace);

    resolve(stmt.module);
    return null;
  }

  @Override
  public Void visitTryStmt(Stmt.Try stmt) {
    resolve(stmt.body);
    for (Stmt.Catch catchStmt : stmt.catches) {
      resolve(catchStmt);
    }
    if (stmt.finallyStmt != null) resolve(stmt.finallyStmt);
    return null;
  }

  @Override
  public Void visitCatchStmt(Stmt.Catch stmt) {
    beginScope();
    declare(stmt.identifier);
    define(stmt.identifier);
    resolve(stmt.body);
    endScope();
    return null;
  }

  @Override
  public Void visitAssignExpr(Expr.Assign expr) {
    resolve(expr.value);
    resolveLocal(expr, expr.name);
    return null;
  }

  @Override
  public Void visitBinaryExpr(Expr.Binary expr) {
    resolve(expr.left);
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visitCallExpr(Expr.Call expr) {
    resolve(expr.callee);
    for (Expr argument : expr.arguments) {
      resolve(argument);
    }
    return null;
  }

  @Override
  public Void visitGetExpr(Expr.Get expr) {
    resolve(expr.object);
    return null;
  }

  @Override
  public Void visitGroupingExpr(Expr.Grouping expr) {
    resolve(expr.expression);
    return null;
  }

  @Override
  public Void visitLiteralExpr(Expr.Literal expr) {
    return null;
  }

  @Override
  public Void visitLogicalExpr(Expr.Logical expr) {
    resolve(expr.left);
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visitSetExpr(Expr.Set expr) {
    resolve(expr.value);
    resolve(expr.object);
    return null;
  }

  @Override
  public Void visitSuperExpr(Expr.Super expr) {
    if (currentClass == ClassType.NONE) {
      EverScript.error(expr.keyword, "SyntaxError", "Cannot use 'super' outside of a class.");
    } else if (currentClass != ClassType.SUBCLASS) {
      EverScript.error(expr.keyword, "SyntaxError", "Cannot use 'super' in a class with no superclass.");
    }
    resolveLocal(expr, expr.keyword);
    return null;
  }

  @Override
  public Void visitThisExpr(Expr.This expr) {
    if (currentClass == ClassType.NONE) {
      EverScript.error(expr.keyword, "SyntaxError", "Cannot use 'this' outside of a class.");
      return null;
    }

    if (currentContext == FunctionCtx.STATIC) {
      EverScript.error(expr.keyword, "SyntaxError", "Cannot use 'this' in a static context.");
    }

    resolveLocal(expr, expr.keyword);
    return null;
  }

  @Override
  public Void visitUnaryExpr(Expr.Unary expr) {
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visitVariableExpr(Expr.Variable expr) {
    if (!scopes.isEmpty() &&
        scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
      EverScript.error(expr.name, "SyntaxError", "Cannot read local variable in its own initializer.");
    }

    resolveLocal(expr, expr.name);
    return null;
  }

  @Override
  public Void visitArrayExpr(Expr.Array expr) {
    expr.elements.forEach(this::resolve);
    return null;
  }

  @Override
  public Void visitIndexGetExpr(Expr.IndexGet expr) {
    resolve(expr.indexee);
    resolve(expr.index);
    return null;
  }

  @Override
  public Void visitIndexSetExpr(Expr.IndexSet expr) {
    resolve(expr.value);
    resolve(expr.indexee);
    resolve(expr.index);
    return null;
  }

  private void resolve(Stmt stmt) {
    stmt.accept(this);
  }

  private void resolve(Expr expr) {
    expr.accept(this);
  }

  private void resolveFunction(Object function, FunctionType type, FunctionCtx ctx) {
    FunctionType enclosingFunction = currentFunction;
    currentFunction = type;
    FunctionCtx enclosingContext = currentContext;
    currentContext = ctx;

    List<Token> params;
    List<Stmt> body;

    if (function instanceof Stmt.Function) {
      params = ((Stmt.Function) function).function.params;
      body = ((Stmt.Function) function).function.body;

      if (currentFunction == FunctionType.INITIALIZER && currentContext == FunctionCtx.STATIC && params.size() > 0) {
        EverScript.error(((Stmt.Function) function).name, "ArityError", "Static initializers cannot contain parameters.");
      }
    } else if (function instanceof Expr.Function) {
      params = ((Expr.Function) function).params;
      body = ((Expr.Function) function).body;

      if (currentFunction == FunctionType.INITIALIZER && currentContext == FunctionCtx.STATIC && params.size() > 0) {
        EverScript.error(null, "ArityError", "Static initializers cannot contain parameters.");
      }
    } else {
      params = new ArrayList<>();
      body = new ArrayList<>();
    }

    beginScope();
    for (Token param : params) {
      declare(param);
      define(param);
    }

    resolve(body);
    endScope();

    currentFunction = enclosingFunction;
    currentContext = enclosingContext;
  }

  private void beginScope() {
    scopes.push(new HashMap<>());
  }

  private void endScope() {
    scopes.pop();
  }

  private void declare(Token name) {
    if (scopes.isEmpty()) return;
    Map<String, Boolean> scope = scopes.peek();
    if (scope.containsKey(name.lexeme)) {
      EverScript.error(name, "SyntaxError", "Identifier '" + name.lexeme + "' has already been declared in this scope.");
    }
    scope.put(name.lexeme, false);
  }

  private void define(Token name) {
    if (scopes.isEmpty()) return;
    scopes.peek().put(name.lexeme, true);
  }

  private void resolveLocal(Expr expr, Token name) {
    for (int i = scopes.size() - 1; i >= 0; i--) {
      if (scopes.get(i).containsKey(name.lexeme)) {
        interpreter.resolve(expr, scopes.size() - 1 - i);
        return;
      }
    }
  }
}
