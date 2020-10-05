package com.linkbyte.everscript;

import java.util.*;

class ESClass extends ESInstance implements ESCallable {
  final String name;
  final ESClass superclass;
  private final Map<String, ESFunction> methods;
  private final Map<String, ESFunction> _staticMethods = new HashMap<>();

  private ESClass(String name, Map<String, ESFunction> staticMethods) {
    super(null);

    this.name = name;
    this.methods = staticMethods;
    this.superclass = null;
  }

  ESClass(String name, ESClass superclass, Map<String, ESFunction> methods, Map<String, ESFunction> staticMethods) {
    super(superclass);
    this.superclass = superclass;
    this.name = name;
    this.methods = methods;
    this.klass = new ESClass(name + " (metaclass)", staticMethods);

    for (Map.Entry<String, ESFunction> staticMethod : staticMethods.entrySet()) {
      _staticMethods.put(staticMethod.getKey(), staticMethod.getValue());
    }
  }

  ESFunction findMethod(ESInstance instance, String name) {
    if (methods.containsKey(name)) {
      return methods.get(name).bind(instance);
    } else if (_staticMethods.containsKey(name)) {
      return _staticMethods.get(name).bind(instance);
    }

    if (superclass != null) {
      return superclass.findMethod(instance, name);
    }

    return null;
  }

  @Override
  public String toString() {
    if (methods.containsKey("toString")) {
      return (String) methods.get("toString").call(new Interpreter(), new ArrayList<>());
    } else return this.name + " {}";
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    ESInstance instance = new ESInstance(this);
    ESFunction initializer = methods.get(name);
    if (initializer != null) {
      initializer.bind(instance).call(interpreter, arguments);
    }

    return instance;
  }

  @Override
  public int arity() {
    ESFunction initializer = methods.get(name);
    if (initializer == null) return 0;
    return initializer.arity();
  }

  boolean inherits(Token klass) {
    if (klass.lexeme.equals(name)) return true;
    if (superclass != null) return superclass.inherits(klass);
    return false;
  }
}
