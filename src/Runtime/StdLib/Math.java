package Runtime.StdLib;

import java.util.List;
import java.util.function.Consumer;

import Parsing.Stmt;
import Runtime.Environment;
import Runtime.AstreCallable;
import Runtime.Interpreter;

public class Math {
    public static Consumer<Environment> builder = Math::build;

    private static void build(Environment environment) {
        environment.define(null, Stmt.Modifier.Constant, "sin", new AstreCallable() {
            @Override public int arity() { return 1; }
            @Override public Object call(Interpreter ignore, List<Object> args) {
                return java.lang.Math.sin((double)args.get(0));
            }
        });
        environment.define(null, Stmt.Modifier.Constant, "cos", new AstreCallable() {
            @Override public int arity() { return 1; }
            @Override public Object call(Interpreter ignore, List<Object> args) {
                return java.lang.Math.cos((double)args.get(0));
            }
        });
        environment.define(null, Stmt.Modifier.Constant, "tan", new AstreCallable() {
            @Override public int arity() { return 1; }
            @Override public Object call(Interpreter ignore, List<Object> args) {
                return java.lang.Math.tan((double)args.get(0));
            }
        });
    }
}
