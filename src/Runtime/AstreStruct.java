package Runtime;

import java.util.List;
import java.util.Map;

public class AstreStruct implements AstreCallable {
    public final String name;
    public final AstreStruct superStruct;
    private final Map<String, AstreFunction> methods;


    public AstreStruct(final String name, final AstreStruct superStruct, final Map<String, AstreFunction> methods) {
        this.name = name;
        this.superStruct = superStruct;
        this.methods = methods;
    }

    public AstreFunction findMethod(final String name) {
        final AstreFunction func = methods.get(name);
        if (func != null) {
            return func;
        } else if (superStruct != null) {
            return superStruct.findMethod(name);
        } else {
            return null;
        }
    }

    @Override
    public int arity() {
        final AstreFunction anew = findMethod("anew");
        return (anew != null) ? anew.arity() : 0;
    }

    @Override
    public Object call(final Interpreter interpreter, final List<Object> arguments) {
        final AstreInstance instance = new AstreInstance(this);
        final AstreFunction anew = findMethod("anew");
        if (anew != null) {
            anew.bind(instance).call(interpreter, arguments);
        }
        return instance;
    }

    @Override
    public String toString() {
        return name;
    }
}
