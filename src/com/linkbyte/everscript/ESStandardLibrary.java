package com.linkbyte.everscript;

import java.util.*;

public class ESStandardLibrary {
    public static final ESNativeInstance _System = new ESNativeInstance("System", new HashMap<>() {{
        put("exit", new ESCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                java.lang.System.exit(((Double) arguments.get(0)).intValue());
                return null;
            }

            @Override
            public String toString() {
                return "System#exit()";
            }
        });

        put("getProperty", new ESCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return java.lang.System.getProperty((((String) arguments.get(0))));
            }

            @Override
            public String toString() {
                return "System#getProperty()";
            }
        });

        put("getEnv", new ESCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return java.lang.System.getenv((((String) arguments.get(0))));
            }

            @Override
            public String toString() {
                return "System#getEnv()";
            }
        });
    }});

    public static final ESNativeInstance _Math = new ESNativeInstance("Math", new HashMap<>() {{
        put("abs", new ESCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return Math.abs((Double) arguments.get(0));
            }

            @Override
            public String toString() {
                return "Math#abs()";
            }
        });

        put("random", new ESCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return Math.random();
            }

            @Override
            public String toString() {
                return "Math#random()";
            }
        });

        put("floor", new ESCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return Math.floor((Double) arguments.get(0));
            }

            @Override
            public String toString() {
                return "Math#floor()";
            }
        });

        put("ceiling", new ESCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return Math.ceil((Double) arguments.get(0));
            }

            @Override
            public String toString() {
                return "Math#ceiling()";
            }
        });
    }});

    public static void importAll(Environment environment) {
        environment.define("System", _System);
    }
}
