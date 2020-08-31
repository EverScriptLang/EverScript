package com.linkbyte.everscript;

import java.util.*;

class ESEnum {
    final String name;
    final Map<Token, String> properties;
    final Map<String, ESCallable> methods;

    ESEnum(String name, Map<Token, String> properties) {

        this.name = name;
        this.properties = properties;

        methods = createMethods(this);
    }

    static Map<String, ESCallable> createMethods(ESEnum _enum) {
        Map<String, ESCallable> methods = new HashMap<>();
        methods.put("length", new ESCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return _enum.properties.size();
            }
        });

        return methods;
    }

    ESCallable getMethod(Token name) {
        if (methods.containsKey(name.lexeme)) return methods.get(name.lexeme);
        throw new RuntimeError(name, "No such method exists '" + name.lexeme + "' exists in 'enum' type.");
    }

    public Object getProperty(Token property) {
        if (properties.containsValue(property.lexeme)) return property.lexeme;
        throw new RuntimeError(property, "Value '" + property.lexeme + "' does not exist in enum '" + name + "'.");
    }

    @Override
    public String toString() {
        String _enum = "{ ";

        for (Map.Entry<Token, String> values : properties.entrySet()) {
            _enum += values.getValue() + ", ";
        }

        _enum += "}";

        return _enum;
    }
}
