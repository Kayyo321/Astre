package ASTAnalysis;

import java.util.Stack;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import Astre.*;
import LexicalAnalysis.*;
import Parsing.*;
import Parsing.Expr.*;
import Parsing.Stmt.*;
import Runtime.*;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private enum FunctionType {
        None,
        Func,
        Anew,
        Method
    }

    private enum StructType {
        None,
        Struct,
        SubStruct
    }

    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes;
    private FunctionType currentFunction = FunctionType.None;
    private StructType currentStruct = StructType.None;

    public Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
        this.scopes = new Stack<>();
    }

    @Override
    public Void visitBlockStmt(Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    public void resolve(List<Stmt> statements) {
        for (final Stmt statement : statements) {
            resolve(statement);
        }
    }

    private void resolve(Stmt statement) {
        statement.accept(this);
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    private void endScope() {
        scopes.pop();
    }

    @Override
    public Void visitStructStmt(Struct stmt) {
        final StructType enclosingStruct = currentStruct;
        currentStruct = StructType.Struct;

        declare(stmt.name);
        define(stmt.name);

        if (stmt.superStruct != null) {
            currentStruct = StructType.SubStruct;

            if (stmt.name.lexeme.equals(stmt.superStruct.name.lexeme)) {
                Astre.error(stmt.superStruct.name, "A struct can't derive itself");
            }
            resolve(stmt.superStruct);

            beginScope();
            scopes.peek().put("super", true);
        }

        beginScope();
        scopes.peek().put("self", true);

        for (final FunctionStmt method : stmt.methods) {
            resolveFunction(method, (method.name.lexeme.equals("anew")) ? FunctionType.Anew : FunctionType.Method);
        }

        endScope();

        if (stmt.superStruct != null) {
            endScope();
        }

        currentStruct = enclosingStruct;

        return null;
    }

    @Override
    public Void visitExpressionStmt(Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(FunctionStmt stmt) {
        declare(stmt.name);
        define(stmt.name);
        resolveFunction(stmt, FunctionType.Func);
        return null;
    }

    private void resolveFunction(FunctionStmt stmt, FunctionType type) {
        final FunctionType enclosingFunction = currentFunction;
        currentFunction = type;
        beginScope();
        for (final Token param : stmt.params) {
            declare(param);
            define(param);
        }
        resolve(stmt.body);
        endScope();
        currentFunction = enclosingFunction;
    }

    @Override
    public Void visitIfStmt(If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) {
            resolve(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitReturnStmt(ReturnStmt stmt) {
        if (currentFunction == FunctionType.None) {
            Astre.error(stmt.keyword, "Can't return from top-level scope (dummy).");
        }

        if (stmt.value != null) {
            if (currentFunction == FunctionType.Anew) {
                Astre.error(stmt.keyword, "Can't return a value from `anew` initializer.");
            }
            resolve(stmt.value);
        }

        return null;
    }

    @Override
    public Void visitLetStmt(Let stmt) {
        declare(stmt.name);
        if (stmt.init != null) {
            resolve(stmt.init);
        }
        define(stmt.name);
        return null;
    }

    private void declare(Token name) {
        if (scopes.isEmpty()) {
            return;
        }

        final Map<String, Boolean> scope = scopes.peek();

        if (scope.containsKey(name.lexeme)) {
            Astre.error(name, "Already a variable with this name in this scope.");
        }

        scope.put(name.lexeme, false);
    }

    private void define(Token name) {
        if (scopes.isEmpty()) {
            return;
        }

        scopes.peek().put(name.lexeme, true);
    }

    @Override
    public Void visitWhileStmt(While stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visitForStmt(For stmt) {
        beginScope();
        if (stmt.init != null) {
            resolve(stmt.init);
        }

        if (stmt.condition != null) {
            resolve(stmt.condition);
        }

        if (stmt.inc != null) {
            resolve(stmt.inc);
        }
        endScope();

        return null;
    }

    @Override
    public Void visitAssignExpr(Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitBinaryExpr(Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Call expr) {
        resolve(expr.callee);

        for (final Expr argument : expr.arguments) {
            resolve(argument);
        }

        return null;
    }

    @Override
    public Void visitGetExpr(Get expr) {
        resolve(expr.obj);
        return null;
    }

    @Override
    public Void visitGroupingExpr(Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Literal ignore) {
        return null;
    }

    @Override
    public Void visitLogicalExpr(Logical expr) {
        interpreter.visitLogicalExpr(expr);
        return null;
    }

    @Override
    public Void visitSetExpr(Set expr) {
        resolve(expr.value);
        resolve(expr.obj);
        return null;
    }

    @Override
    public Void visitSuperExpr(Super expr) {
        if (currentStruct == StructType.None) {
            Astre.error(expr.keyword, "Can't user `super` outside of a struct.");
        } else if (currentStruct != StructType.SubStruct) {
            Astre.error(expr.keyword, "Cant' use `super` in a struct with no super-struct");
        }
        resolveLocal(expr, expr.keyword);
        return null;
    }

    @Override
    public Void visitSelfExpr(Self expr) {
        if (currentStruct == StructType.None) {
            Astre.error(expr.keyword, "Can't use `self` keyword outside of a struct.");
        }
        resolveLocal(expr, expr.keyword);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitVariable(Variable expr) {
        if (!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
            Astre.error(expr.name, "Can't read local variable in its own initializer.");
        }

        resolveLocal(expr, expr.name);
        return null;
    }

    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size()-1; i >= 0; --i) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expr, (scopes.size()-1)-i);
                return;
            }
        }
    }
}