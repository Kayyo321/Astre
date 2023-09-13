package Runtime.StdLib;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import Parsing.Stmt;
import Runtime.Interpreter;
import Runtime.AstreCallable;

public class Rand {
    public static final Consumer<Interpreter> builder = Rand::build;
    private static Random random = new Random();

    private static void build(final Interpreter interpreter) {
        interpreter.environment.define(null, Stmt.Modifier.Constant, "random", new AstreCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                return random.nextDouble();
            }
        });
        interpreter.environment.define(null, Stmt.Modifier.Constant, "randint", new AstreCallable() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                final double min = (double)args.get(0);
                final double max = (double)args.get(1);

                final double result = random.nextDouble(min, max);

                return (int)result;
            }
        });
        interpreter.environment.define(null, Stmt.Modifier.Constant, "uniform", new AstreCallable() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                final double min = (double)args.get(0);
                final double max = (double)args.get(1);

                final double result = random.nextDouble(min, max);

                return result;
            }
        });
    }
}
