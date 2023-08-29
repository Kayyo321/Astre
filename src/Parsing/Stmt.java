package Parsing;

import java.util.List;

import LexicalAnalysis.*;

public abstract sealed class Stmt {
    public interface Visitor<R> {
        R visitBlockStmt(Block stmt);
        R visitStructStmt(Struct stmt);
        R visitExpressionStmt(Expression stmt);
        R visitFunctionStmt(Function stmt);
        R visitIfStmt(If stmt);
        R visitPrintStmt(Print stmt);
        R visitReturnStmt(ReturnStmt stmt);
        R visitLetStmt(Let stmt);
        R visitWhileStmt(While stmt);
        R visitForStmt(For stmt);
    }

    public static final class Block extends Stmt {
        public Block(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }

        public final List<Stmt> statements;
    }

    public static final class Struct extends Stmt {
        public Struct(Token name, Expr.Variable superStruct, List<Stmt.Function> methods) {
            this.name = name;
            this.superStruct = superStruct;
            this.methods = methods;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitStructStmt(this);
        }

        public final Token name;
        public final Expr.Variable superStruct;
        public final List<Stmt.Function> methods;
    }

    public static final class Expression extends Stmt {
        public Expression(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }

        public final Expr expression;
    }

    public static final class Function extends Stmt {
        public Function(Token name, List<Token> params, List<Stmt> body) {
            this.name = name;
            this.params = params;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionStmt(this);
        }

        public final Token name;
        public final List<Token> params;
        public final List<Stmt> body;
    }

    public static final class If extends Stmt {
        public If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }

        public final Expr condition;
        public final Stmt thenBranch;
        public final Stmt elseBranch;
    }

    public static final class Print extends Stmt {
        public Print(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }

        public final Expr expression;
    }

    public static final class ReturnStmt extends Stmt {
        public ReturnStmt(Token keyword, Expr value) {
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStmt(this);
        }

        public final Token keyword;
        public final Expr value;
    }

    public enum Modifier {
        None,
        Constant,
        Nullable,
        Both
    }

    public static final class Let extends Stmt {
        public Let(Token keyword, Modifier mod, Token name, Expr init) {
            this.keyword = keyword;
            this.mod = mod;
            this.name = name;
            this.init = init;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLetStmt(this);
        }

        public final Token name;
        public final Expr init;
        public final Modifier mod;
        public final Token keyword;
    }

    public static final class While extends Stmt {
        public While(Expr condition, Stmt body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }

        public final Expr condition;
        public final Stmt body;
    }

    public static final class For extends Stmt {
        public For(Stmt init, Expr condition, Expression inc, Stmt body) {
            this.init = init;
            this.condition = condition;
            this.inc = inc;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitForStmt(this);
        }

        public Stmt init;
        public Expr condition;
        public Expression inc;
        public Stmt body;
    }

    public abstract <R> R accept(Visitor<R> visitor);
}