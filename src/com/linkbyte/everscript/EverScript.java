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
      System.out.println("Usage: everscript [script]");
      System.exit(64);
    } else if (args.length == 1) {
      if (!args[0].endsWith(".evs")) throw new NativeError("Extension not recognized by the EverScript interpreter. Accepted extensions: '.evs'");
      runFile(args[0]);
    } else {
      runPrompt();
    }
  }

  private static void runFile(String path) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path));

    new LibraryLoader().loadClasses();
    int start = Commons.clock();
    run(path, new String(bytes, Charset.defaultCharset()));
    int end = Commons.clock() - start;
    System.out.println("Program executed in " + end + "ms.");
    if (errors > 0) {
      System.out.print("The EverScript interpreter found a total of " + errors + " error");
      if (errors > 1) System.out.println("s");
      else System.out.println();
      if (hadError) System.exit(65);
      if (hadRuntimeError) System.exit(70);
    }
  }

  private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);

    new LibraryLoader().loadClasses();

    System.out.println("EverScript REPL [5th of October, 2020]");
    System.out.println("Press CTRL + C to exit");

    //noinspection InfiniteLoopStatement
    while (true) {
      System.out.print("~> ");
      run("REPL", reader.readLine() + "\n");
      hadError = false;
      errors = 0;
    }
  }

  public static void run(String file, String input) {
    source = input;
    Scanner scanner = new Scanner(file, input);
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
    error(token.file, token.line, token.col, errorType, message);
  }

  static void error(String file, int line, int col, String errorType, String message) {
    errors++;
    System.out.println(source.split("\n")[line - 1]);
    System.out.println(repeat(col - 1) + String.format("^ [file '%s', line %d, col %d]: %s: %s", file, line, col, errorType, message));
    System.out.println();
    hadError = true;
  }

  static void runtimeError(RuntimeError error) {
    System.out.printf("[file '%s', line %d, col %d]: %s%n", error.token.file, error.token.line, error.token.col, error.getMessage());
    System.out.println();
    hadRuntimeError = true;
  }

  private static String repeat(int n) {
    if (n <= 0) return "";
    return new String(new char[n]).replace('\0', ' ');
  }
}
