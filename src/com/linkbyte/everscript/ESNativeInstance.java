package com.linkbyte.everscript;

import java.util.*;

class ESNativeInstance implements ESCallable {
    final String name;
    private final Map<String, ESCallable> methods;

    ESNativeInstance(String name, Map<String, ESCallable> methods) {
        this.name = name;
        this.methods = methods;
    }

    ESCallable findMethod(String name) {
        if (methods.containsKey(name)) return methods.get(name);
        return null;
    }

    @Override
    public String toString() {
        return "<native instance " + name + ">";
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        ESCallable initializer = methods.get("constructor");
        if (initializer != null) initializer.call(interpreter, arguments);

        return this;
    }

    @Override
    public int arity() {
        ESCallable initializer = methods.get("constructor");
        if (initializer == null) return 0;
        return initializer.arity();
    }
}
