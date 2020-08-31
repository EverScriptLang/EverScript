package com.linkbyte.everscript;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

public class EverScript {
  private static final Interpreter interpreter = new Interpreter();
  static boolean hadError = false;
  static boolean hadRuntimeError = false;
  private static int errors = 0;
  private static String source;

  public static void main(String[] args) throws IOException {
    if (args.length > 1) {
      System.out.println("Usage: cscript [script]");
      System.exit(64);
    } else if (args.length == 1) {
      runFile(args[0]);
    } else {
      runPrompt();
    }
  }

  private static void runFile(String path) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path));

    // Run native classes
    new ESNativeClasses().declareClasses();

    run(new String(bytes, Charset.defaultCharset()));
    if (errors > 0) {
      System.out.print("The EasyScript interpreter found a total of " + errors + " error");
      if (errors > 1) System.out.println("s");
      else System.out.println();
      if (hadError) System.exit(65);
      if (hadRuntimeError) System.exit(70);
    }
  }

  private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);

    // Run native classes
    new ESNativeClasses().declareClasses();

    System.out.println("EasyScript REPL [31st of August, 2020]");
    System.out.println("Press CTRL + C to exit");

    //noinspection InfiniteLoopStatement
    while (true) {
      System.out.print("~> ");
      run(reader.readLine());
      hadError = false;
      errors = 0;
    }
  }

  public static void run(String input) {
    source = input;
    Scanner scanner = new Scanner(input);
    List<Token> tokens = scanner.scanTokens();
    Parser parser = new Parser(tokens);
    List<Stmt> statements = parser.parse();
    if (errors != 0) return;
    if (hadError) return;
    Resolver resolver = new Resolver(interpreter);
    resolver.resolve(statements);
    if (hadError) return;
    if (errors != 0) return;
    interpreter.interpret(statements);
  }

  static void error(Token token, String errorType, String message) {
    error(token.line, token.col, errorType, message);
  }

  static void error(int line, int col, String errorType, String message) {
    errors++;
    System.out.println(source.split("\n")[line - 1]);
    System.out.println(repeat(col - 1) + String.format("^ [line %d, col %d]: %s: %s", line, col, errorType, message));
    System.out.println();
    hadError = true;
  }

  static void runtimeError(RuntimeError error) {
    System.out.printf("[line %d, col %d]: error: %s%n", error.token.line, error.token.col, error.getMessage());
    System.out.println();
    hadRuntimeError = true;
  }

  private static String repeat(int n) {
    if (n <= 0) return "";
    return new String(new char[n]).replace('\0', ' ');
  }
}
