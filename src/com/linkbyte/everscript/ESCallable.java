package com.linkbyte.everscript;

import java.util.List;

interface ESCallable {
  int arity();
  Object call(Interpreter interpreter, List<Object> arguments);
}
