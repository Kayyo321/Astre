package Parsing;

import java.util.List;

import LexicalAnalysis.*;

public abstract sealed class Expr {
    public interface Visitor<R> {
        R visitAssignExpr(Assign expr);
        R visitBinaryExpr(Binary expr);
        R visitCallExpr(Call expr);
        R visitGetExpr(Get expr);
        R visitGroupingExpr(Grouping expr);
        R visitLiteralExpr(Literal expr);
        R visitLogicalExpr(Logical expr);
        R visitSetExpr(Set expr);
        R visitSuperExpr(Super expr);
        R visitSelfExpr(Self expr);
        R visitUnaryExpr(Unary expr);
        R visitVariable(Variable expr);
    }

    public final String classType;

    public Expr(final String classType) {
        this.classType = classType;
    }

    public static final class Assign extends Expr {
        public Assign(final Token name, final Expr value) {
            super("Assign");
            this.name = name;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }

        public final Token name;
        public final Expr value;
    }

    public static final class Binary extends Expr {
        public Binary(Expr left, Token operator, Expr right) {
            super("Binary");
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }

        public final Expr left;
        public final Token operator;
        public final Expr right;
    }

    public static final class Call extends Expr {
        public Call(Expr callee, Token paren, List<Expr> arguments) {
            super("Call");
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }

        public final Expr callee;
        public final Token paren;
        public final List<Expr> arguments;
    }

    public static final class Get extends Expr {
        public Get(Expr obj, Token name) {
            super("Get");
            this.obj = obj;
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetExpr(this);
        }

        public final Expr obj;
        public final Token name;
    }

    public static final class Grouping extends Expr {
        public Grouping(Expr expression) {
            super("Grouping");
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }

        public final Expr expression;
    }

    public static final class Literal extends Expr {
        public Literal(Object value) {
            super("Literal");
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }

        public final Object value;
    }

    public static final class Logical extends Expr {
        public Logical(Expr left, Token operator, Expr right) {
            super("Logical");
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }

        public final Expr left;
        public final Token operator;
        public final Expr right;
    }

    public static final class Set extends Expr {
        public Set(Expr obj, Token name, Expr value) {
            super("Set");
            this.obj = obj;
            this.name = name;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSetExpr(this);
        }

        public final Expr obj;
        public final Token name;
        public final Expr value;
    }

    public static final class Super extends Expr {
        public Super(Token keyword, Token method) {
            super("Super");
            this.keyword = keyword;
            this.method = method;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSuperExpr(this);
        }

        public final Token keyword;
        public final Token method;
    }

    public static final class Self extends Expr {
        public Self(Token keyword) {
            super("Self");
            this.keyword = keyword;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSelfExpr(this);
        }

        public final Token keyword;
    }

    public static final class Unary extends Expr {
        public Unary(Token operator,  Expr right) {
            super("Unary");
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }

        public final Token operator;
        public final  Expr right;
    }

    public static final class Variable extends Expr {
        public Variable(Token name) {
            super("Variable");
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariable(this);
        }

        public final Token name;
    }

    public abstract <R> R accept(Visitor<R> visitor);
}