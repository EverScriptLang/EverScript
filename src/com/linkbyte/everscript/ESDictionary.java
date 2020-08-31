package com.linkbyte.everscript;

import java.util.*;

class ESDictionary {
    final Token name;
    final Map<String, Object> properties;

    ESDictionary(Token name, Map<String, Object> properties) {
        this.name = name;
        this.properties = properties;
    }

    public Object getProperty(Token key, boolean safeNavigation) {
        if (safeNavigation) {
            if (properties.containsKey(key.lexeme)) {
                return properties.get(key.lexeme);
            }
            return null;
        } else {
            if (properties.containsKey(key.lexeme)) {
                return properties.get(key.lexeme);
            }
            throw new RuntimeError(key, "Property '" + key.lexeme + "' does not exist in object literal '" + name.lexeme + "'.");
        }
    }

    public void setProperty(Token key, Object value) {
        properties.put(key.lexeme, value);
    }

    @Override
    public String toString() {
        String dict = "{ ";

        for (Map.Entry<String, Object> props : properties.entrySet()) {
            dict += props.getKey() + ": " + props.getValue() + ", ";
        }

        dict += " }";

        return dict;
    }
}
