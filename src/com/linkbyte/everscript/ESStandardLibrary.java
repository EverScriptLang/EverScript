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
                return "<System.exit native fn>";
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
        });
    }});

    public static void importAll(Environment environment) {
        environment.define("System", _System);
    }
}
