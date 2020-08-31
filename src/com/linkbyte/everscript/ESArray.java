package com.linkbyte.everscript;

import java.util.*;

class ESArray implements ESIndexable {
    final List<Object> elements;
    private final Map<String, ESCallable> methods;

    ESArray(List<Object> elements) {
        this.elements = elements;
        methods = createMethods(this);
    }

    private static Map<String, ESCallable> createMethods(ESArray array) {
        Map<String, ESCallable> methods = new HashMap<>();
        methods.put("push", new ESCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                array.elements.addAll(arguments);
                return null;
            }
        });
        methods.put("pop", new ESCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                try {
                    return array.elements.remove(0);
                } catch (IndexOutOfBoundsException e) {
                    throw new NativeError("Array is empty.");
                }
            }
        });
        methods.put("remove", new ESCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                try {
                    int i = ((Double) arguments.get(0)).intValue();
                    return array.elements.remove(i);
                } catch (NumberFormatException e) {
                    throw new NativeError("Array index must be an integer value.");
                } catch (IndexOutOfBoundsException e) {
                    throw new NativeError("Array index out of bounds.");
                }
            }
        });
        methods.put("length", new ESCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return array.length();
            }
        });
        methods.put("isEmpty", new ESCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return array.length() == 0;
            }
        });
        return methods;
    }

    ESCallable getMethod(Token name) {
        if (methods.containsKey(name.lexeme)) return methods.get(name.lexeme);
        throw new RuntimeError(name, "No such method exists '" + name.lexeme + "' exists in 'array' type.");
    }

    @Override
    public Object get(Token token, Object index) {
        int i = indexToInteger(token, index);
        try {
            return elements.get(i);
        } catch (IndexOutOfBoundsException e) {
            throw new RuntimeError(token, "Array index out of bounds.");
        }
    }

    @Override
    public void set(Token token, Object index, Object item) {
        int i = indexToInteger(token, index);
        try {
            elements.set(i, item);
        } catch (IndexOutOfBoundsException e) {
            throw new RuntimeError(token, "Array index out of bounds.");
        }
    }

    @Override
    public int length() {
        return elements.size();
    }

    private int indexToInteger(Token token, Object index) {
        if (index instanceof Double) {
            double i = (Double) index;
            if (i == Math.floor(i)) {
                return (i < 0) ? Math.floorMod((int) i, elements.size()) : (int) i;
            }
        }
        throw new RuntimeError(token, "Array index must be an integer value.");
    }
}
