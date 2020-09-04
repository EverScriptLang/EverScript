package com.linkbyte.everscript;

class RuntimeError extends RuntimeException {
  final Token token;
  final String message;

  RuntimeError(Token token, String message) {
    super(message);
    this.message = message;
    this.token = token;
  }

  public String getMessage() {
    return message;
  }

  public static class InterpreterRuntimeError extends RuntimeError {
    final Token token;
    boolean catchable;

    InterpreterRuntimeError(Token token, String message) {
      super(token, message);
      this.token = token;
      this.catchable = true;
    }

    public InterpreterRuntimeError(Token token, String message, boolean catchable) {
      this(token, message);
      this.catchable = catchable;
    }
  }
}
