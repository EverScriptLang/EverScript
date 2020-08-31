package com.linkbyte.everscript;

import java.util.*;

class ESInstance {
  protected ESClass klass;
  private final Map<String, Object> fields = new HashMap<>();

  ESInstance(ESClass klass) {
    this.klass = klass;
  }

  Object get(Token name) {
    if (fields.containsKey(name.lexeme)) {
      return fields.get(name.lexeme);
    }
    ESFunction method = klass.findMethod(this, name.lexeme);
    if (method != null) return method.bind(this);
    throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
  }

  void set(Token name, Object value) {
    fields.put(name.lexeme, value);
  }

  @Override
  public String toString() {
    return klass.name + " instance";
  }
  
  ESClass klass() {
    return klass;
  }
}
