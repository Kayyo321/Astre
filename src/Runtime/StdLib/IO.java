package Runtime.StdLib;

import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

import Parsing.Stmt;
import Runtime.Interpreter;
import Runtime.AstreCallable;

public class IO {
    public final static Consumer<Interpreter> builder = IO::build;

    private static void build(final Interpreter astre) {
        astre.environment.define(null, Stmt.Modifier.Constant, "write", new AstreCallable() {
            @Override
            public int arity() {
                return -1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                for (int i = 0; i < args.size(); ++i) {
                    System.out.print(args.get(i));
                    if (i != args.size()-1) {
                        System.out.print(" ");
                    }
                }

                return null;
            }
        });
        astre.environment.define(null, Stmt.Modifier.Constant, "writeln", new AstreCallable() {
            @Override
            public int arity() {
                return -1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                for (int i = 0; i < args.size(); ++i) {
                    System.out.print(args.get(i));
                    if (i != args.size()-1) {
                        System.out.print(" ");
                    }
                }

                System.out.println();

                return null;
            }
        });
        astre.environment.define(null, Stmt.Modifier.Constant, "readln", new AstreCallable() {
            @Override
            public int arity() {
                return -1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                for (int i = 0; i < args.size(); ++i) {
                    System.out.print(args.get(i));
                    if (i != args.size()-1) {
                        System.out.print(" ");
                    }
                }

                final String o = new Scanner(System.in).nextLine();
                System.out.println();
                return o;
            }
        });
        astre.environment.define(null, Stmt.Modifier.Constant, "read_num", new AstreCallable() {
            @Override
            public int arity() {
                return -1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                for (int i = 0; i < args.size(); ++i) {
                    System.out.print(args.get(i));
                    if (i != args.size()-1) {
                        System.out.print(" ");
                    }
                }

                final double o = new Scanner(System.in).nextDouble();
                System.out.println();
                return o;
            }
        });
    }
}
