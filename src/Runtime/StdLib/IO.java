package Runtime.StdLib;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
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
        astre.environment.define(null, Stmt.Modifier.Constant, "write_err", new AstreCallable() {
            @Override
            public int arity() {
                return -1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                for (int i = 0; i < args.size(); ++i) {
                    System.err.print(args.get(i));
                    if (i != args.size()-1) {
                        System.err.print(" ");
                    }
                }

                return null;
            }
        });
        astre.environment.define(null, Stmt.Modifier.Constant, "writeln_err", new AstreCallable() {
            @Override
            public int arity() {
                return -1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                for (int i = 0; i < args.size(); ++i) {
                    System.err.print(args.get(i));
                    if (i != args.size()-1) {
                        System.err.print(" ");
                    }
                }

                System.err.println();

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
                return o;
            }
        });
        astre.environment.define(null, Stmt.Modifier.Constant, "ftell", new AstreCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader((String)args.get(0)));
                    int charCount = 0;

                    while (bufferedReader.read() != -1) {
                        ++charCount;
                    }

                    bufferedReader.close();

                    return (double)charCount;
                } catch (final IOException ioe) {
                    ioe.printStackTrace();
                    System.exit(1);
                }
                return null;//never here
            }
        });
        astre.environment.define(null, Stmt.Modifier.Constant, "freadb", new AstreCallable() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                try {
                    RandomAccessFile file  = new RandomAccessFile((String)args.get(0), "r");
                    file.seek((int)args.get(1));

                    int charCode = file.read();

                    if (charCode != -1) {
                        char character = (char)charCode;
                        return Character.toString(character);
                    } else {
                        System.out.println("Index: " + args.get(1) + " out of range in file: " + args.get(0) + ".");
                        System.exit(1);
                    }
                } catch (final IOException ioe) {
                    ioe.printStackTrace();
                    System.exit(1);
                }
                return null;//never here
            }
        });
    }
}
