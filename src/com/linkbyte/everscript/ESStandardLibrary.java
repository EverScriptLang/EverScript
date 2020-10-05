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

    public static final ESNativeInstance Internals = new ESNativeInstance("Internals", new HashMap<>() {{
        put("getDoubleConstant", new ESCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                String constant = (String) arguments.get(0);
                switch (constant) {
                    case "MAX_VALUE":
                        return Double.MAX_VALUE;

                    case "MIN_VALUE":
                        return Double.MIN_VALUE;

                    case "NaN":
                        return Double.NaN;

                    case "POSITIVE_INFINITY":
                        return Double.POSITIVE_INFINITY;

                    case "NEGATIVE_INFINITY":
                        return Double.NEGATIVE_INFINITY;

                    default:
                        throw new NativeError("Unrecognized 'Double' constant '" + constant + "'.");
                }
            }
        });

        put("getDoubleMethod", new ESCallable() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                String method = (String) arguments.get(0);
                Object value = arguments.get(1);
                switch (method) {
                    case "isNaN":
                        return Double.isNaN((Double) value);
                    case "isFinite":
                        return Double.isFinite((Double) value);
                    case "isInfinite":
                        return Double.isInfinite((Double) value);
                    case "parseDouble":
                        return Double.parseDouble((String) value);
                    case "isDouble":
                        return value instanceof Double;

                    default:
                        throw new NativeError("Unrecognized 'Double' method '" + method + "'.");
                }
            }
        });

        put("getIntegerMethod", new ESCallable() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                String method = (String) arguments.get(0);
                Object value = arguments.get(1);
                switch (method) {
                    case "parseInt":
                        return Integer.parseInt((String) value);
                    case "isInteger":
                        return value instanceof Integer;
                    default:
                        throw new NativeError("Unrecognized 'Integer' method '" + method +"'.");
                }
            }
        });

        put("", new ESCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return null;
            }
        });

        put("", new ESCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return null;
            }
        });

        put("", new ESCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return null;
            }
        });

        put("", new ESCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return null;
            }
        });

        put("", new ESCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return null;
            }
        });

        put("", new ESCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return null;
            }
        });

        put("", new ESCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return null;
            }
        });

        put("", new ESCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return null;
            }
        });

        put("", new ESCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return null;
            }
        });

        put("", new ESCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return null;
            }
        });

        put("", new ESCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return null;
            }
        });

        put("", new ESCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return null;
            }
        });

        put("", new ESCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return null;
            }
        });

        put("", new ESCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return null;
            }
        });

        put("", new ESCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return null;
            }
        });

        put("", new ESCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return null;
            }
        });

        put("", new ESCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return null;
            }
        });

        put("", new ESCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return null;
            }
        });
    }});

    public static void importAll(Environment environment) {
        environment.define("System", _System);
        environment.define("Internals", Internals);
    }
}
