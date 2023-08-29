package Runtime;

import java.util.List;

import Parsing.*;

public class AstreFunction implements AstreCallable {
    private final Stmt.Function declaration;
    private final Environment closure;
    private final boolean isAnew;

    public AstreFunction(Stmt.Function declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
        this.isAnew = false;
    }

    public AstreFunction(final Stmt.Function declaration, final Environment closure, final boolean isAnew) {
        this.declaration = declaration;
        this.closure = closure;
        this.isAnew = isAnew;
    }

    public AstreFunction bind(final AstreInstance instance) {
        final Environment environment = new Environment(closure);
        environment.define(null, Stmt.Modifier.Constant, "self", instance);
        return new AstreFunction(declaration, environment, isAnew);
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        final Environment environment = new Environment(closure);
        for (int i = 0; i < declaration.params.size(); ++i) {
            environment.define(null, Stmt.Modifier.None, declaration.params.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            if (isAnew) {
                return closure.getAt(0, "self");
            }

            return returnValue.value;
        }

        if (isAnew) {
            return closure.getAt(0, "self");
        }

        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }
}