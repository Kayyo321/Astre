package Astre;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import ASTAnalysis.Resolver;
import LexicalAnalysis.*;
import Parsing.*;
import Runtime.*;

public class Astre {
    private static boolean hadError = false, hadRuntimeError = false;
    private static final Interpreter astre = new Interpreter();

    public static boolean traceTokens=false, traceStmt=false;

    public static String[] cmdLnArgs;

    public static void main(final String[] args) throws IOException {
        cmdLnArgs = args;

        if (args.length == 0) {
            runPrompt();
            return;
        }

        final List<String> files = new ArrayList<>(), flags = new ArrayList<>();

        for (final String argument : args) {
            if (argument.charAt(0) == '-') {
                flags.add(argument);
            } else {
                files.add(argument);
            }
        }

        if (!flags.isEmpty()) {
            parseFlags(flags);
        }

        if (files.isEmpty()) {
            System.err.println("Astre cannot run nothing! (no files given)");
            System.exit(1);
        }

        runFiles(files);
    }

    private static void parseFlags(final List<String> flags) {
        for (final String flag : flags) {
            switch (flag) {
                case "-tokentrace" -> traceTokens = true;
                case "-tracestmt" -> traceStmt = true;
                default -> {
                    System.err.println("Didn't expect flag: " + flag);
                    System.exit(1);
                }
            }
        }
    }

    public static void error(int line, String message) {
        report(line, "", message);
    }

    public static void error(Token token, String errMsg) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end-of-file", errMsg);
        } else {
            report(token.line, "at `" + token.lexeme + "`", errMsg);
        }
    }

    public static void error(RuntimeError err) {
        System.err.println(err.getMessage() + "\n[line " + err.token.line + "]");
        hadRuntimeError = true;
    }

    private static void runFiles(List<String> files) throws IOException {
        for (final String file: files) {
            runFile(file);
        }
    }

    private static void runFile(String file) throws IOException {
        final byte[] bytes = Files.readAllBytes(Paths.get(file));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) {
            System.exit(65);
        } else if (hadRuntimeError) {
            System.exit(70);
        }
    }

    private static void runPrompt() throws IOException {
        final InputStreamReader input = new InputStreamReader(System.in);
        final BufferedReader reader = new BufferedReader(input);
        String ln;

        while (true) {
            System.out.print("> ");
            ln = reader.readLine();

            if (ln == null || ln.isEmpty() || ln.equals("exit")) {
                break;
            }

            run(ln);
            hadError = hadRuntimeError = false;
        }
    }

    private static void run(String code) {
        final Scanner lexer = new Scanner(code);
        final List<Token> tokens = lexer.scan();

        if (traceTokens) {
            for (final Token trace : tokens) {
                System.out.println(trace);
            }
        }

        final Parser parser = new Parser(tokens);
        final List<Stmt> ast = parser.parse();
        if (hadError) {
            return;
        }

        final Resolver resolver = new Resolver(astre);
        resolver.resolve(ast);
        if (hadError) {
            return;
        }

        astre.interpret(ast);
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error " + where + ": " + message);
        hadError = true;
    }
}