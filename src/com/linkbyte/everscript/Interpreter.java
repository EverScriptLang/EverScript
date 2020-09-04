package com.linkbyte.everscript;

import java.io.*;
import java.util.*;
import java.util.stream.*;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    final Environment globals = new Environment();
    private final Map<Expr, Integer> locals = new HashMap<>();
    private Environment environment = globals;

    Interpreter() {
        globals.define("clock", new ESCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double) System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        globals.define("typeof", new ESCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                Object value = arguments.get(0);
                return checkType(value);
            }
        });
    }

    private String checkType(Object value) {
        if (value instanceof String) return "string";
        else if (value instanceof Double) return "number";
        else if (value instanceof Boolean) return "boolean";
        else if (value instanceof ESFunction) return "function";
        else if (value instanceof ESClass) return "class";
        else if (value instanceof ESArray) return "array";
        else if (value instanceof ESDictionary) return "object";
        else if (value instanceof ESInstance) return "instance";
        else if (value instanceof ESNativeInstance) return "native instance";
        return null;
    }

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            EverScript.runtimeError(error);
        }
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        environment.define(stmt.name.lexeme, null);
        Object superclass = null;

        if (stmt.superclass != null) {
            superclass = evaluate(stmt.superclass);
            if (!(superclass instanceof ESClass)) {
                throw new RuntimeError(stmt.name, "The inherited class must be a class.");
            }
            environment = new Environment(environment);
            environment.define("super", superclass);
        }

        Map<String, ESFunction> staticMethods = new HashMap<>();
        for (Stmt.Function method : stmt.staticMethods) {
            ESFunction function = new ESFunction(method.name.lexeme, method.function, environment, method.name.lexeme.equals("constructor"));
            staticMethods.put(method.name.lexeme, function);
        }

        Map<String, ESFunction> methods = new HashMap<>();
        for (Stmt.Function method : stmt.methods) {
            ESFunction function = new ESFunction(method.name.lexeme, method.function, environment, method.name.lexeme.equals("constructor"));
            methods.put(method.name.lexeme, function);
        }
        ESClass klass = new ESClass(stmt.name.lexeme, (ESClass) superclass, methods, staticMethods);

        if (superclass != null) {
            environment = environment.enclosing;
        }
        environment.assign(stmt.name, klass);
        return null;
    }

    @Override
    public Void visitEnumStmt(Stmt.Enum stmt) {
        ESEnum _enum = new ESEnum(stmt.name.lexeme, stmt.properties);
        environment.define(stmt.name.lexeme, _enum);
        return null;
    }

    @Override
    public Void visitThrowStmt(Stmt.Throw stmt) {
        Object thrown = evaluate(stmt.thrown);
        Token runtimeError = new Token(TokenType.IDENTIFIER, "Error", null, stmt.keyword.line, stmt.keyword.col);

        if (!(thrown instanceof ESInstance && ((ESInstance) thrown).klass().inherits(runtimeError))) {
            throw new RuntimeError(stmt.keyword, "Only objects that inherit 'Error' can be thrown.");
        }

        Token messageToken = new Token(TokenType.IDENTIFIER, "message", null, 0, 0);
        Object message = ((ESCallable) ((ESInstance) thrown).get(messageToken)).call(this, new ArrayList<>());

        throw new UserRuntimeError((ESInstance) thrown, stringify(message), stmt.keyword);
    }

    private boolean errorMatches(Stmt.Catch stmt, RuntimeError error) {
        for (Token errorType : stmt.errors) {
            if (errorType.lexeme.equals("Error")) return true;
            if (error instanceof RuntimeError.InterpreterRuntimeError && errorType.lexeme.equals("RuntimeError")) return true;
            if (error instanceof RuntimeError.InterpreterRuntimeError) continue;
            if (((UserRuntimeError) error).instance.klass().inherits(errorType)) return true;
        }
        return false;
    }

    @Override
    public Void visitTryStmt(Stmt.Try stmt) {
        try {
            execute(stmt.body);
        } catch (RuntimeError error) {
            if (error instanceof RuntimeError.InterpreterRuntimeError && !((RuntimeError.InterpreterRuntimeError) error).catchable) throw error;

            for (Stmt.Catch catchStmt : stmt.catches) {
                if (errorMatches(catchStmt, error)) {
                    Environment enclosing = environment;
                    environment = new Environment(enclosing);

                    if (error instanceof UserRuntimeError) {
                        environment.define(catchStmt.identifier.lexeme, ((UserRuntimeError) error).instance);
                    } else {
                        Map<String, ESFunction> methods = new HashMap<>();
                        List<Stmt> body = new ArrayList<>();
                        Token name = new Token(TokenType.IDENTIFIER, "message", null, 0, 0);
                        List<Token> params = new ArrayList<>();
                        body.add(new Stmt.Return(new Token(TokenType.RETURN, "return", null, 0, 0), new Expr.Literal(error.getMessage())));
                        Stmt.Function function = new Stmt.Function(name, new Expr.Function(params, body));
                        methods.put("message", new ESFunction(name.lexeme, function.function, environment, false));
                        body = new ArrayList<>();
                        name = new Token(TokenType.IDENTIFIER, "getType", null, 0, 0);
                        params = new ArrayList<>();
                        body.add(new Stmt.Return(new Token(TokenType.RETURN, "return", null, 0, 0), new Expr.Literal("RuntimeError")));
                        function = new Stmt.Function(name, new Expr.Function(params, body));
                        methods.put("getType", new ESFunction(name.lexeme, function.function, environment, false));
                        Token superclass = new Token(TokenType.IDENTIFIER, "RuntimeError", null, catchStmt.identifier.line, catchStmt.identifier.col);
                        ESClass runtimeError = new ESClass("RuntimeError", (ESClass) globals.get(superclass), methods, null);
                        ESInstance errorInstance = new ESInstance(runtimeError);
                        environment.define(catchStmt.identifier.lexeme, errorInstance);
                    }
                    execute(catchStmt);
                    environment = enclosing;
                }
            }
        } finally {
            if (stmt.finallyStmt != null) execute(stmt.finallyStmt);
        }
        return null;
    }

    @Override
    public Void visitCatchStmt(Stmt.Catch stmt) {
        execute(stmt.body);
        return null;
    }

    @Override
    public Object visitDictionaryExpr(Expr.Dictionary expr) {
        Map<String, Object> properties = new HashMap<>();

        for (Map.Entry<Token, Object> property : expr.properties.entrySet()) {
            Token key = property.getKey();
            Object value = evaluate((Expr) property.getValue());
            properties.put(key.lexeme, value);
        }

        ESDictionary dictionary = new ESDictionary(expr.name, properties);
        environment.define(expr.name.lexeme, dictionary);
        return dictionary;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        ESFunction function = new ESFunction(stmt.name.lexeme, stmt.function, environment, false);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Object visitFunctionExpr(Expr.Function expr) {
        return new ESFunction(null, expr, environment, false);
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) value = evaluate(stmt.value);

        throw new Return(value);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Void visitImportStmt(Stmt.Import stmt) {
        Object module = evaluate(stmt.module);

        if (!(module instanceof String)) {
            throw new RuntimeError(stmt.keyword, "Expected type 'string' for the 'module' argument, got '" + checkType(module) + "' instead.");
        }

        String name = (String) module;

        if (name.startsWith("EverScript.")) {
            String library = name.split("\\.")[1];
            if (library.equals("System")) {
                globals.define(stmt.namespace.lexeme, ESStandardLibrary._System);
            } else if (library.equals("**")) {
                ESStandardLibrary.importAll(globals);
            }

            return null;
        }

        StringBuilder source = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader((String) module));
            String curLine;
            while ((curLine = br.readLine()) != null) {
                source.append(curLine).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeError(stmt.keyword, "There was an error while trying to import '" + module + "'.");
        }

        EverScript.run(source.toString());

        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        Integer distance = locals.get(expr);
        if (distance != null) {
            environment.assignAt(distance, expr.name, value);
        } else {
            globals.assign(expr.name, value);
        }
        return value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        switch (expr.operator.type) {
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    return left + (String) right;
                }
                if (left instanceof Double && right instanceof String) {
                    return stringify(left) + right;
                }
                if (left instanceof String && right instanceof Double) {
                    return left + stringify(right);
                }
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double) left / (double) right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
        }
        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof ESCallable)) {
            throw new RuntimeError(expr.paren, "Only functions and classes can be called.");
        }

        ESCallable function = (ESCallable) callee;

        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected " + function.arity() + " arguments but got " + arguments.size() + " instead.");
        }

        return function.call(this, arguments);
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        Object object = evaluate(expr.object);
        if (object instanceof ESInstance) {
            Object result = ((ESInstance) object).get(expr.name);
            if (result instanceof ESFunction && ((ESFunction) result).isGetter()) {
                System.out.println(object);
                result = ((ESFunction) result).call(this, null);
            }

            return result;
        }

        if (object instanceof ESArray) {
            return ((ESArray) object).getMethod(expr.name);
        }

        if (object instanceof ESEnum) {
            if (((ESEnum) object).methods.containsKey(expr.name.lexeme)) {
                return ((ESEnum) object).getMethod(expr.name);
            } else return ((ESEnum) object).getProperty(expr.name);
        }

        if (object instanceof ESNativeInstance) {
            return ((ESNativeInstance) object).findMethod(expr.name.lexeme);
        }

        if (object instanceof ESDictionary) {
            if (expr.operator.type == TokenType.OPTIONAL_CHAINING) return ((ESDictionary) object).getProperty(expr.name, true);
            else return ((ESDictionary) object).getProperty(expr.name, false);
        }

        throw new RuntimeError(expr.name, "Only instances have properties.");
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);
        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }
        return evaluate(expr.right);
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        Object object = evaluate(expr.object);
        if (object instanceof ESInstance) {
            Object value = evaluate(expr.value);
            ((ESInstance) object).set(expr.name, value);
            return value;
        } else if (object instanceof ESDictionary) {
            Object value = evaluate(expr.value);
            ((ESDictionary) object).setProperty(expr.name, value);
            return value;
        }

        throw new RuntimeError(expr.name, "Can only set properties for instances or object literals.");
    }

    @Override
    public Object visitSuperExpr(Expr.Super expr) {
        int distance = locals.get(expr);

        ESClass superclass = (ESClass) environment.getAt(distance, "super");
        ESInstance object = (ESInstance) environment.getAt(distance - 1, "this");
        ESFunction method = superclass.findMethod(object, expr.method.lexeme);

        if (method == null) {
            throw new RuntimeError(expr.method, "Undefined property '" + expr.method.lexeme + "'.");
        }

        return method.bind(object);
    }

    @Override
    public Object visitThisExpr(Expr.This expr) {
        return lookUpVariable(expr.keyword, expr);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);
        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double) right;
        }
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return lookUpVariable(expr.name, expr);
    }

    @Override
    public Object visitIndexSetExpr(Expr.IndexSet expr) {
        Object indexee = evaluate(expr.indexee);
        if (!(indexee instanceof ESIndexable)) {
            throw new RuntimeError(expr.bracket, "Variable not indexable.");
        }
        Object index = evaluate(expr.index);
        Object value = evaluate(expr.value);
        ((ESIndexable) indexee).set(expr.bracket, index, value);
        return value;
    }

    @Override
    public Object visitIndexGetExpr(Expr.IndexGet expr) {
        Object indexee = evaluate(expr.indexee);
        Object index = evaluate(expr.index);
        if (indexee instanceof ESIndexable) {
            return ((ESIndexable) indexee).get(expr.bracket, index);
        }
        return null;
    }

    @Override
    public Object visitArrayExpr(Expr.Array expr) {
        return new ESArray(expr.elements.stream().map(this::evaluate).collect(Collectors.toList()));
    }

    private Object lookUpVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);

        if (distance != null) return environment.getAt(distance, name.lexeme);
        else return globals.get(name);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    private String stringify(Object object) {
        if (object == null) return "null";
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        if (object instanceof ESArray) {
            List<String> elementsString = ((ESArray) object).elements.stream().map(this::stringify).collect(Collectors.toList());
            return "[" + String.join(", ", elementsString) + "]";
        }
        return object.toString();
    }
}
