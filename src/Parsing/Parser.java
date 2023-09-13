package Parsing;

import Astre.*;
import LexicalAnalysis.*;
import static LexicalAnalysis.TokenType.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            return statement();
        } catch (ParseError err) {
            synchronize();
            return null;
        }
    }

    private Stmt letDeclaration() {
        final Token keyword = eat();

        Stmt.Modifier mod = Stmt.Modifier.None;

        if (tokens.get(current).type == Bang) {
            eat();
            mod = Stmt.Modifier.Constant;
        }
        if (tokens.get(current).type == QuestionMark) {
            eat();
            mod = (mod == Stmt.Modifier.Constant) ? Stmt.Modifier.Both : Stmt.Modifier.Nullable;
        }

        final Token name = consume(Identifier, "Expect variable name.");

        final Expr init;
        if (match(Equal)) {
            init = expression();
        } else {
            init = null;
        }

        consume(Semicolon, "Expect `;` after variable-declaration.");

        return new Stmt.Let(keyword, mod, name, init);
    }

    private Stmt statement() {
        return switch (this.peek().type) {
            case Let -> letDeclaration();
            case Struct -> structStmt();
            case Interface -> interfaceStmt();
            case Print -> printStmt();
            case If -> ifStmt();
            case While -> whileStmt();
            case For -> forStmt();
            case Function -> function(false);
            case Return -> returnStmt();
            case LBrace -> new Stmt.Block(block());
            case Match -> matchStmt();
            case Range -> rangeStmt();
            default -> expressionStmt(false);
        };
    }

    private Stmt structStmt() {
        eat();
        final Token name = consume(Identifier, "Expect struct-name after struct declaration.");
        final Expr.Variable structSuper;
        final int structStatus;
        if (match(Derives)) {
            consume(Identifier, "Expect super-class name.");
            structSuper = new Expr.Variable(previous());
            structStatus = Stmt.Struct.DERIVES;
        } else if (match(Implements)) {
            consume(Identifier, "Expect super-interface name.");
            structSuper = new Expr.Variable(previous());
            structStatus = Stmt.Struct.IMPLEMENTS;
        } else {
            structSuper = null;
            structStatus = Stmt.Struct.NOTHING;
        }

        final boolean isStatic;
        if (match(LParen)) {
            isStatic = true;
            consume(Static, "Expect `static` after `(`");
            consume(RParen, "Expect `)` after `static`");
        } else {
            isStatic = false;
        }

        final List<Stmt.FunctionStmt> methods = new ArrayList<>();

        consume(LBrace, "Expect `{` after struct-name");

        while (!check(RBrace) && !isAtEnd()) {
            methods.add((Stmt.FunctionStmt)function(true));
        }

        consume(RBrace, "Expect `}` after struct body.");

        return new Stmt.Struct(name, structSuper, methods, isStatic, structStatus);
    }

    private Stmt interfaceStmt() {
        eat();
        final Token name = consume(Identifier, "Expect interface-name after interface declaration.");
        final boolean isStatic;
        if (match(LParen)) {
            isStatic = true;
            consume(Static, "Expect `static` after `(`");
            consume(RParen, "Expect `)` after `static`");
        } else {
            isStatic = false;
        }

        consume(LBrace, "Expect `{` after interface-name");

        final Map<Token, Expr> methods = new HashMap<>();
        Token methodName;
        Expr methodArgsSize;

        while (!match(RBrace)) {
            methodName = consume(Identifier, "Expect method identifier inside of interface body");
            consume(LParen, "Expect `(` for method args");
            if (match(RParen)) {
                methodArgsSize = new Expr.Literal(0.0);
            } else {
                methodArgsSize = expression();
                consume(RParen, "Expect `)` after method args");
            }
            consume(Semicolon, "Expect `;` after interface-method");

            methods.put(methodName, methodArgsSize);
        }

        return new Stmt.InterfaceStmt(name, methods, isStatic);
    }

    private Stmt printStmt() {
        eat();
        final boolean newLine = !match(Bang); // ! == no-newline
        Expr value = expression();
        consume(Semicolon, "Expect `;` after value.");
        return new Stmt.Print(value, newLine);
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
        } else if (check(Let)) {
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

    private Stmt function(final boolean method) {
        if (!method) eat();
        final Token name = consume(Identifier, "Expect " + "func" + " name.");
        consume(LParen, "Expect `(` after " + "func" + " name.");
        final List<Token> params = new ArrayList<>();

        if (!check(RParen)) {
            do {
                params.add(consume(Identifier, "Expect param name."));
            } while (match(Comma));
        }

        consume(RParen, "Expect `)` after " + "func" + " arguments.");

        final boolean isStatic;
        if (match(LParen)) {
            isStatic = true;
            consume(Static, "Expect `static` after `(`");
            consume(RParen, "Expect `)` after `static`");
        } else {
            isStatic = false;
        }

        return new Stmt.FunctionStmt(name, params, block(), isStatic);
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

        consume(Semicolon, "Expect `;` after return value");

        return new Stmt.ReturnStmt(keyword, value);
    }

    private Stmt matchStmt() {
        eat();
        consume(LParen, "Expect `(` after `match`");
        final Expr matchOn = expression();
        consume(RParen, "Expect `)` after match's expression");

        final boolean isStatic;
        if (match(LParen)) {
            isStatic = true;
            consume(Static, "Expect `static` after match's static declaration");
            consume(RParen, "Expect `)` after match's `static`");
        } else {
            isStatic = false;
        }

        consume(LBrace, "Expect `{` after match's expression");

        Expr possibility;
        Stmt toRun;

        List<Stmt.Case> cases = new ArrayList<>();

        while (peek().type == Case) {
            eat();
            possibility = expression();
            toRun = statement();
            cases.add(new Stmt.Case(possibility, toRun));
        }

        consume(RBrace, "Expect `}` after match's body");

        final Stmt ifAllElseFails;

        if (peek().type == Else) {
            eat();
            ifAllElseFails = statement();
        } else {
            ifAllElseFails = null;
        }

        return new Stmt.Match(matchOn, cases, ifAllElseFails, isStatic);
    }

    private Stmt rangeStmt() {
        eat();
        consume(LParen, "Expect `(` after range");
        final Token iterator = consume(Identifier, "Expect iterator-name in range statement");
        consume(Colon, "Expect `:` after range-iterator");

        final Expr first = expression();
        final Stmt body;

        if (match(RParen)) {
            body = statement();
            return new Stmt.RangeStmt(iterator, first, body);
        } else {
            consume(Comma, "Expect `,` after `start` in range-statement");
            final Expr second = expression();
            consume(Comma, "Expect `,` after `stop` in range-statement");
            final Expr third = expression();
            consume(RParen, "Expect `)` after `step` in range-statement");
            body = statement();
            return new Stmt.RangeStmt(iterator, first, second, third, body);
        }
    }

    private Stmt expressionStmt(boolean forgive) {
        Expr value = expression();
        if (!forgive) {
            consume(Semicolon, "Expect `;` after expression-statement.");
        }
        return new Stmt.Expression(value);
    }

    private Expr expression() {
        Expr expr = or();

        if (match(Equal)) {
            final Token equals = previous();
            final Expr value = expression();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            } else if (expr instanceof final Expr.Get get) {
                return new Expr.Set(get.obj, get.name, value);
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

        while (match(BangEqual, EqualEqual, Derives, Implements)) {
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
        boolean leave = false;
        Token name;

        while (!leave) {
            switch (tokens.get(current).type) {
                case LParen -> expr = finishCall(expr);
                case Dot -> {
                    eat();
                    name = consume(Identifier, "Expect property name after `.`");
                    expr = new Expr.Get(expr, name);
                }
                default -> leave = true;
            }
        }

        return expr;
    }

    private Expr finishCall(Expr callee) {
        final List<Expr> args = new ArrayList<>();

        if (tokens.get(current).type == LParen) {
            consume(LParen, "NEVER HERE");
        }

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
                return new Expr.Variable(eat());
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
                return new Expr.Literal(eat().literal);
            }
            case Self -> {
                return new Expr.Self(eat());
            }
            case Super -> {
                final Token keyword = eat();
                consume(Dot, "Expect `.` after `super`.");
                final Token method = consume(Identifier, "Expect superclass method-name");
                return new Expr.Super(keyword, method);
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