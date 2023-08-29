package Astre;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.List;

import ASTAnalysis.Resolver;
import LexicalAnalysis.*;
import Parsing.*;
import Runtime.*;

public class Astre {
    private static boolean hadError = false, hadRuntimeError = false;
    private static final Interpreter astre = new Interpreter();

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: astre [script?]");
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    public static void error(int line, String message) {
        report(line, "", message);
    }

    public static void error(Token token, String errMsg) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", errMsg);
        } else {
            report(token.line, " at '" + token.lexeme + "'", errMsg);
        }
    }

    public static void error(RuntimeError err) {
        System.err.println(err.getMessage() + "\n[line " + err.token.line + "]");
        hadRuntimeError = true;
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

    private static void run(String code) throws IOException {
        final Scanner lexer = new Scanner(code);
        final List<Token> tokens = lexer.scan();

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