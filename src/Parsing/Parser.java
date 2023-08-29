package Parsing;

import Astre.*;
import LexicalAnalysis.*;
import static LexicalAnalysis.TokenType.*;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        final List<Stmt> statements = new ArrayList<>();

        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    private Stmt declaration() {
        try {
            if (match(Let)) {
                return letDeclaration();
            }

            return statement();
        } catch (ParseError err) {
            synchronize();
            return null;
        }
    }

    private Stmt letDeclaration() {
        final Token name = consume(Identifier, "Expect variable name.");

        final Expr init;
        if (match(Equal)) {
            init = expression();
        } else {
            init = null;
        }

        consume(Semicolon, "Expect `;` after variable-declaration.");

        return new Stmt.Let(name, init);
    }

    private Stmt statement() {
        switch (tokens.get(current).type) {
            case Print -> {
                return printStmt();
            }
            case If -> {
                return ifStmt();
            }
            case While -> {
                return whileStmt();
            }
            case For -> {
                return forStmt();
            }
            case Function -> {
                return function();
            }
            case Return -> {
                return returnStmt();
            }
            case LBrace -> {
                return new Stmt.Block(block());
            }
            default -> {
            }
        }

        return expressionStmt(false);
    }

    private Stmt printStmt() {
        eat();
        Expr value = expression();
        consume(Semicolon, "Expect `;` after value.");
        return new Stmt.Print(value);
    }

    private List<Stmt> block() {
        consume(LBrace, "Expect `{` to begin block statement."); // Should never fail

        final List<Stmt> statements = new ArrayList<>();

        while (!check(RBrace) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RBrace, "Expect `}` to end block statement.");

        return statements;
    }

    private Stmt ifStmt() {
        eat();
        consume(LParen, "Expect `(` after `if`.");
        final Expr condition = expression();
        consume(RParen, "Expect `)` after `if`.");
        final Stmt thenBranch = statement();
        final Stmt elseBranch;
        if (match(Else)) {
            elseBranch = statement();
        } else {
            elseBranch = null;
        }
        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt whileStmt() {
        eat();
        consume(LParen, "Expect `(` after `while`");
        final Expr condition = expression();
        consume(RParen, "Expect `)` after `while`");
        final Stmt body = statement();

        return new Stmt.While(condition, body);
    }

    private Stmt forStmt() {
        eat();
        consume(LParen, "Expect `(` after `for`");

        final Stmt init;
        if (match(Semicolon)) {
            init = null;
        } else if (match(Let)) {
            init = letDeclaration();
        } else {
            init = expressionStmt(false);
        }

        final Expr condition;
        if (!check(Semicolon)) {
            condition = expression();
        } else {
            condition = null;
        }

        consume(Semicolon, "Expect `;` after condition in `for`");

        final Stmt.Expression increment;
        if (!check(RParen)) {
            increment = (Stmt.Expression)expressionStmt(true);
        } else {
            increment = null;
        }

        consume(RParen, "Expect `)` after increment/decrement in `for`");

        final Stmt body = statement();

        return new Stmt.For(init, condition, increment, body);
    }

    private Stmt function() {
        eat();
        final Token name = consume(Identifier, "Expect " + "func" + " name.");
        consume(LParen, "Expect `(` after " + "func" + " name.");
        final List<Token> params = new ArrayList<>();

        if (!check(RParen)) {
            do {
                if (params.size() >= 255) {
                    throw error(peek(), "Can't have more than 255 parameters");
                }

                params.add(consume(Identifier, "Expect param name."));
            } while (match(Comma));
        }

        consume(RParen, "Expect `)` after " + "func" + " arguments.");
        return new Stmt.Function(name, params, block());
    }

    private Stmt returnStmt() {
        eat();
        final Token keyword = previous();
        final Expr value;
        if (!check(Semicolon)) {
            value = expression();
        } else {
            value = null;
        }

        consume(Semicolon, "Expected `;` after return value");

        return new Stmt.ReturnStmt(keyword, value);
    }

    private Stmt expressionStmt(boolean forgive) {
        Expr value = expression();
        if (!forgive) {
            consume(Semicolon, "Expect `;` after expression-statement.");
        }
        return new Stmt.Expression(value);
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = or();

        if (match(Equal)) {
            final Token equals = previous();
            final Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }

            throw error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();

        Token operator;
        Expr right;

        while (match(Or)) {
            operator = previous();
            right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        Token operator;
        Expr right;

        while (match(And)) {
            operator = previous();
            right = equality();

            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();
        Token operator;
        Expr right;

        while (match(BangEqual, EqualEqual)) {
            operator = previous();
            right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        Token operator;
        Expr right;

        while (match(Greater, GreaterEqual, Less, LessEqual)) {
            operator = previous();
            right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        Token operator;
        Expr right;

        while (match(Minus, Plus)) {
            operator = previous();
            right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        Token operator;
        Expr right;

        while(match(Slash, Star, Modulo, Power)) {
            operator = previous();
            right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(Bang, Minus)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return call();
    }

    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(LParen)) {
                expr = finishCall(expr);
            } else {
                break;
            }
        }

        return expr;
    }

    private Expr finishCall(Expr callee) {
        final List<Expr> args = new ArrayList<>();

        if (!check(RParen)) {
            do {
                args.add(expression());
            } while (match(Comma));
        }

        if (args.size() >= 255) {
            throw error(peek(), "Can't have more than 255 arguments.");
        }

        return new Expr.Call(callee, consume(RParen, "Expected `)` after arguments"), args);
    }

    private Expr primary() {
        switch (tokens.get(current).type) {
            case Identifier -> {
                eat();
                return new Expr.Variable(previous());
            }
            case False -> {
                eat();
                return new Expr.Literal(false);
            }
            case True -> {
                eat();
                return new Expr.Literal(true);
            }
            case Nothing -> {
                eat();
                return new Expr.Literal(null);
            }
            case Number, String -> {
                eat();
                return new Expr.Literal(previous().literal);
            }
            case LParen -> {
                eat();
                final Expr expr = expression();
                consume(RParen, "Expect `)` after expression");
                return new Expr.Grouping(expr);
            }
            default -> throw error(peek(), "Expect expression.");
        }
    }

    private void synchronize() {
        eat();

        while (!isAtEnd() && previous().type != Semicolon) {
            switch (peek().type) {
                case Struct, Function, Let, For, If, While, Print, Return -> {
                    return;
                }
                default -> {
                }
            }

            eat();
        }
    }

    private Token consume(TokenType expect, String errMsg) {
        if (check(expect)) {
            return eat();
        }

        throw error(peek(), errMsg);
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                eat();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }

        return peek().type == type;
    }

    private Token eat() {
        if (!isAtEnd()) {
            ++current;
        }

        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current-1);
    }

    private ParseError error(Token token, String errMsg) {
        Astre.error(token, errMsg);
        return new ParseError();
    }
}