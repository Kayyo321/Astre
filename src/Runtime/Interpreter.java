package Runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import Astre.*;
import Parsing.*;
import Parsing.Expr.*;
import Parsing.Stmt.*;
import LexicalAnalysis.*;
import Runtime.StdLib.IO;
import Runtime.StdLib.Math;

import static java.lang.Math.pow;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    public Environment globals = new Environment();
    public Environment environment = globals;
    public final Map<Expr, Integer> locals = new HashMap<>();
    private final Map<String, Consumer<Environment>> stdLibraries = new HashMap<>();

    public Interpreter() {
        stdLibraries.put("io", IO.builder);
        stdLibraries.put("math", Math.builder);

        globals.define(null, Modifier.Constant, "clock", new AstreCallable() {
            @Override
            public int arity() { return 0; }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                return (double)System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() { return "<native fn>"; }
        });
        globals.define(null, Modifier.Constant, "import", new AstreCallable() {
            @Override
            public int arity() { return 1; }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                final String toImport = (String)args.get(0);

                if (stdLibraries.get(toImport) == null) {
                    System.err.println("Couldn't import library: " + toImport + " because it was not defined in the Astre-Standard-Library");
                    System.exit(1);
                }

                stdLibraries.get(toImport).accept(environment);

                return null;
            }

            @Override
            public String toString() { return "<native fn>"; }
        });
        globals.define(null, Modifier.Constant, "exit", new AstreCallable() {
            @Override
            public int arity() { return 1; }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                final int exitCode = (Integer)args.get(0);
                System.exit(exitCode);
                return null; // Never here.
            }

            @Override
            public String toString() { return "<native fn>"; }
        });
    }

    public void interpret(List<Stmt> ast) {
        try {
            for (final Stmt syntaxNode: ast) {
                execute(syntaxNode);
            }
        } catch (RuntimeError err) {
            Astre.error(err);
        }
    }

    private void execute(Stmt stmt) {
        final Object value = stmt.accept(this);
        if (Astre.traceStmt) {
            System.out.println(value);
        }
    }

    public void resolve(final Expr expr, final int depth) {
        locals.put(expr, depth);
    }

    public void executeBlock(List<Stmt> body, Environment environment) {
        final Environment previous = this.environment;
        try {
            this.environment = environment;

            for (final Stmt statement : body) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    private void executeFor(For stmt, Environment environment) {
        final Environment previous = this.environment;
        try {
            this.environment = environment;

            if (stmt.init != null) {
                execute(stmt.init);
            }

            while (stmt.condition == null || isTruthy(evaluate(stmt.condition))) {
                execute(stmt.body);
                if (stmt.inc != null) {
                    execute(stmt.inc);
                }
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Object visitBinaryExpr(final Binary expr) {
        final Object left = evaluate(expr.left);
        final Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case Plus -> {
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                } else if (left instanceof String && right instanceof String) {
                    return left + (String) right;
                }
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings");
            }
            case Minus -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            }
            case Star -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
            }
            case Slash -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left / (double) right;
            }
            case Modulo -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left % (double) right;
            }
            case Power -> {
                checkNumberOperands(expr.operator, left, right);
                return pow((double) left, (double) right);
            }
            case Greater -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            }
            case GreaterEqual -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            }
            case Less -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            }
            case LessEqual -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;
            }
            case BangEqual -> {
                return !equ(left, right);
            }
            case EqualEqual -> {
                return equ(left, right);
            }
            default -> {
            }
        }

        // Never here
        return null;
    }

    @Override
    public Object visitGroupingExpr(final Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(final Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(final Unary expr) {
        final Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case Minus -> {
                checkNumberOperand(expr.operator, right);
                return -(double) right;
            }
            case Bang -> {
                return !isTruthy(right);
            }
            default -> {
                // Never here
                return null;
            }
        }
    }

    private Object evaluate(final Expr expr) {
        return expr.accept(this);
    }

    private boolean isTruthy(final Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof Boolean) {
            return (boolean)obj;
        } else {
            return true;
        }
    }

    private boolean equ(final Object left, final Object right) {
        if (left == null && right == null) {
            return true;
        } else if (left == null) {
            return false;
        }

        return left.equals(right);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) {
            return;
        }

        throw new RuntimeError(operator, "Operand must be a number");
    }

    private void checkNumberOperands(Token operator, Object operand0, Object operand1) {
        if (operand0 instanceof Double && operand1 instanceof Double) {
            return;
        }

        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private String stringify(Object obj) {
        if (obj == null) {
            return "nothing";
        }

        if (obj instanceof Double) {
            String text = obj.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length()-2);
            }
            return text;
        }

        return obj.toString();
    }

    @Override
    public Object visitAssignExpr(Assign expr) {
        final Object value = evaluate(expr.value);
        final Integer distance = locals.get(expr);
        if (distance != null) {
            environment.assignAt(distance, expr.name, value);
        } else {
            globals.assign(expr.name, value);
        }
        return value;
    }

    @Override
    public Object visitCallExpr(Call expr) {
        final Object callee = evaluate(expr.callee);
        final List<Object> args = new ArrayList<>();

        for (final Expr arg : expr.arguments) {
            args.add(evaluate(arg));
        }

        if (!(callee instanceof final AstreCallable function)) {
            throw new RuntimeError(expr.paren, "Can only call function name and classes.");
        }

        if (function.arity() != -1 && args.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected " + function.arity() + " arguments but got " + args.size() + ".");
        }

        return function.call(this, args);
    }

    @Override
    public Object visitGetExpr(Get expr) {
        if (evaluate(expr.obj) instanceof final AstreInstance obj) {
            return obj.get(expr.name);
        }

        throw new RuntimeError(expr.name, "Only instances have properties");
    }

    @Override
    public Object visitLogicalExpr(Logical expr) {
        final Object left = evaluate(expr.left);
        final Object right = evaluate(expr.right);

        if (expr.operator.type == TokenType.Or) {
            if (isTruthy(left)) {
                return left;
            } else if (isTruthy(right)) {
                return right;
            }
        } else if (expr.operator.type == TokenType.And) {
            return isTruthy(left) && isTruthy(right);
        }

        return false;
    }

    @Override
    public Object visitSetExpr(Set expr) {
        if (evaluate(expr.obj) instanceof final AstreInstance object) {
            return object.set(expr.name, evaluate(expr.value));
        }

        throw new RuntimeError(expr.name, "Only instances have fields.");
    }

    @Override
    public Object visitSuperExpr(Super expr) {
        final int distance = locals.get(expr);
        final AstreStruct superStruct = (AstreStruct)environment.getAt(distance, "super");
        final AstreInstance obj = (AstreInstance)environment.getAt(distance-1, "self");
        final AstreFunction method = superStruct.findMethod(expr.method.lexeme);
        if (method == null) {
            throw new RuntimeError(expr.method, "Undefined property `" + expr.method.lexeme + "`.");
        }

        return method.bind(obj);
    }

    @Override
    public Object visitSelfExpr(Self expr) {
        return lookupVariable(expr.keyword, expr);
    }

    @Override
    public Object visitVariable(Variable expr) {
        return lookupVariable(expr.name, expr);
    }

    private Object lookupVariable(Token name, Expr expr) {
        final Integer distance = locals.get(expr);
        if (distance != null) {
            return environment.getAt(distance, name.lexeme);
        } else {
            return globals.get(name);
        }
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitBlockStmt(Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitStructStmt(Struct stmt) {
        final Object superStruct;
        if (stmt.superStruct != null) {
            superStruct = evaluate(stmt.superStruct);
            if (!(superStruct instanceof AstreStruct)) {
                throw new RuntimeError(stmt.superStruct.name, "Super-Struct must be a struct");
            }
        } else {
            superStruct = null;
        }

        environment.define(stmt.name, Modifier.Nullable, stmt.name.lexeme, null);

        if (stmt.superStruct != null) {
            environment = new Environment(environment);
            environment.define(stmt.name, Modifier.Constant, "super", superStruct);
        }

        final Map<String, AstreFunction> methods = new HashMap<>();
        for (final FunctionStmt method : stmt.methods) {
            methods.put(method.name.lexeme, new AstreFunction(method, environment, method.name.lexeme.equals("anew")));
        }

        if (superStruct != null) {
            environment = environment.enclosing;
        }

        environment.assign(stmt.name, new AstreStruct(stmt.name.lexeme, (AstreStruct)superStruct, methods));

        return null;
    }

    @Override
    public Void visitFunctionStmt(FunctionStmt stmt) {
        environment.define(stmt.name, Modifier.Nullable, stmt.name.lexeme, new AstreFunction(stmt, environment));
        return null;
    }

    @Override
    public Void visitIfStmt(If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Print stmt) {
        System.out.println(stringify(evaluate(stmt.expression)));
        return null;
    }

    @Override
    public Void visitReturnStmt(ReturnStmt stmt) {
        throw new Return((stmt.value != null) ? evaluate(stmt.value) : null);
    }

    @Override
    public Void visitLetStmt(Let stmt) {
        environment.define(stmt.keyword, stmt.mod, stmt.name.lexeme, (stmt.init != null) ? evaluate(stmt.init) : null);
        return null;
    }

    @Override
    public Void visitWhileStmt(While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Void visitForStmt(For stmt) {
        executeFor(stmt, new Environment(environment));
        return null;
    }
}