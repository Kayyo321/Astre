package Parsing;

import java.util.List;

import LexicalAnalysis.*;

public abstract sealed class Expr {
    public interface Visitor<R> {
        public R visitAssignExpr(Assign expr);
        public R visitBinaryExpr(Binary expr);
        public R visitCallExpr(Call expr);
        public R visitGetExpr(Get expr);
        public R visitGroupingExpr(Grouping expr);
        public R visitLiteralExpr(Literal expr);
        public R visitLogicalExpr(Logical expr);
        public R visitSetExpr(Set expr);
        public R visitSuperExpr(Super expr);
        public R visitSelfExpr(Self expr);
        public R visitUnaryExpr(Unary expr);
        public R visitVariable(Variable expr);
    }

    public static final class Assign extends Expr {
        public Assign(Token name, Expr value) {
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