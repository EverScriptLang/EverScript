package com.linkbyte.everscript;

import java.util.*;

class ESEnum {
    final String name;
    final List<Token> properties;
    final List<String> props = new ArrayList<>();
    final Map<String, ESCallable> methods;

    ESEnum(String name, List<Token> properties) {

        this.name = name;
        this.properties = properties;

        for (Token property : properties) {
            props.add(property.lexeme);
        }

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

    public Object getProperty(String property, Token name) {
        if (props.contains(property)) return props.indexOf(property);
        else throw new RuntimeError(name, "Value '" + property + "' does not exist in enum '" + this.name + "'.");
    }

    @Override
    public String toString() {
        StringBuilder _enum = new StringBuilder("{ ");

        for (Token prop : properties) {
            _enum.append(String.format("%s: %d, ", prop.lexeme, properties.indexOf(prop)));
        }

        _enum.append("}");

        return _enum.toString();
    }
}
