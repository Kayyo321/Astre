package Parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import LexicalAnalysis.*;

public abstract sealed class Stmt {
    public interface Visitor<R> {
        R visitBlockStmt(Block stmt);
        R visitStructStmt(Struct stmt);
        R visitExpressionStmt(Expression stmt);
        R visitFunctionStmt(FunctionStmt stmt);
        R visitIfStmt(If stmt);
        R visitPrintStmt(Print stmt);
        R visitReturnStmt(ReturnStmt stmt);
        R visitLetStmt(Let stmt);
        R visitWhileStmt(While stmt);
        R visitForStmt(For stmt);
        R visitMatchStmt(Match stmt);
        R visitInterfaceStmt(InterfaceStmt stmt);
        R visitRangeStmt(RangeStmt stmt);
    }

    public final String classType;

    public Stmt(final String classType) {
        this.classType = classType;
    }

    public static final class Block extends Stmt {
        public Block(List<Stmt> statements) {
            super("Block");
            this.statements = statements;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }

        public final List<Stmt> statements;
    }

    public static final class Struct extends Stmt {
        public Struct(final Token name, final Expr.Variable superStruct, final List<FunctionStmt> methods, final boolean isStatic, final int status) {
            super("Struct");
            this.name = name;
            this.superStruct = superStruct;
            this.methods = methods;
            this.isStatic = isStatic;
            this.status = status;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitStructStmt(this);
        }

        public final Token name;
        public final Expr.Variable superStruct;
        public final int status;
        public final List<FunctionStmt> methods;
        public final boolean isStatic;

        public static final int NOTHING = 0, DERIVES = 1, IMPLEMENTS = 2;
    }

    public static final class Expression extends Stmt {
        public Expression(Expr expression) {
            super("Expression");
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }

        public final Expr expression;
    }

    public static final class FunctionStmt extends Stmt {
        public FunctionStmt(final Token name, final List<Token> params, final List<Stmt> body, final boolean isStatic) {
            super("FunctionStmt");
            this.name = name;
            this.params = params;
            this.body = body;
            this.isStatic = isStatic;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionStmt(this);
        }

        public final Token name;
        public final List<Token> params;
        public final List<Stmt> body;
        public final boolean isStatic;
    }

    public static final class If extends Stmt {
        public If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            super("If");
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
        public Print(final Expr expression, final boolean newLine) {
            super("Print");
            this.expression = expression;
            this.newLine = newLine;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }

        public final Expr expression;
        public final boolean newLine;
    }

    public static final class ReturnStmt extends Stmt {
        public ReturnStmt(Token keyword, Expr value) {
            super("ReturnStmt");
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
            super("Let");
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
            super("While");
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
            super("For");
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

    public static final class Match extends Stmt {
        public Match(final Expr matchOn, final List<Case> possibilities, final Stmt ifAllElseFails, final boolean isStatic) {
            super("Match");
            this.matchOn = matchOn;
            this.possibilities = possibilities;
            this.ifAllElseFails = ifAllElseFails;
            this.isStatic = isStatic;
            this.statics = new ArrayList<>();
        }

        @Override
        public <R> R accept(Visitor<R> visitor) { return visitor.visitMatchStmt(this); }

        public final Expr matchOn;
        public final List<Case> possibilities;
        public final Stmt ifAllElseFails;
        public final boolean isStatic;
        public List<Object> statics;
    }

    public static final class Case {
        public Case(final Expr possibility, final Stmt toRun) {
            this.possibility = possibility;
            this.toRun = toRun;
        }

        public final Expr possibility;
        public final Stmt toRun;
    }

    public static final class InterfaceStmt extends Stmt {
        public InterfaceStmt(final Token name, final Map<Token, Expr> methods, final boolean isStatic) {
            super("InterfaceStmt");
            this.name = name;
            this.methods = methods;
            this.isStatic = isStatic;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) { return visitor.visitInterfaceStmt(this); }

        public final Token name;
        public final Map<Token, Expr> methods;
        public final boolean isStatic;
    }

    public static final class RangeStmt extends Stmt {
        public RangeStmt(final Token iterator, final Expr stop, final Stmt body) {
            super("RangeStmt");
            this.iterator = iterator;
            this.stop = stop;
            this.start = this.step = null;
            this.body = body;
            this.oneArg = true;
        }

        public RangeStmt(final Token iterator, final Expr start, final Expr stop, final Expr step, final Stmt body) {
            super("RangeStmt");
            this.iterator = iterator;
            this.start = start;
            this.stop = stop;
            this.step = step;
            this.body = body;
            this.oneArg = false;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitRangeStmt(this);
        }

        public final Token iterator;
        public final boolean oneArg;
        public final Expr start, stop, step;
        public final Stmt body;
    }

    public abstract <R> R accept(Visitor<R> visitor);
}