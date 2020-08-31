package com.linkbyte.everscript;

public class ESNativeClasses {
    private final String newLine = System.getProperty("line.separator");

    private String ErrorHandler() {
        return "class Error: {" +
                "  message() => {" +
                "    return \"No message defined.\";" +
                "  }" +
                newLine +
                "  getType() => {" +
                "    return \"Error\";" +
                "  }" +
                "}" +
                newLine +
                "class UnhandledException inherits Error: {" +
                "  constructor(exception) => {" +
                "    this.exception = exception;" +
                "  }" +
                newLine +
                "  message() => {" +
                "    return \"UnhandledException: \" + this.exception;" +
                "  }" +
                newLine +
                "  getType() => {" +
                "    return \"UnhandledException\";" +
                "  }" +
                "}" +
                newLine +
                "class Exception inherits Error: {" +
                "  constructor(type, msg) => {" +
                "    this.type = type;" +
                "    this.msg = msg;" +
                "  }" +
                newLine +
                "  message() => {" +
                "    return this.type + \": \" + this.msg;" +
                "  }" +
                newLine +
                "  getType() => {" +
                "    return \"Exception:\" + this.type;" +
                "  }" +
                "}";
    }

    private String Utils() {
        return "class Utils: {" +
                "  static equals(a, b) => {" +
                "    if (a == null or b == null) return -1;" +
                "    else if (a == b) return 1;" +
                "    else return 0;" +
                "  }" +
                "}";
    }

    public void declareClasses() {
        EverScript.run(ErrorHandler());
        EverScript.run(Utils());
    }
}
