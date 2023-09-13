package Runtime;

import LexicalAnalysis.Token;

import java.util.HashMap;
import java.util.Map;

public class AstreInstance {
    public final AstreStruct struct;
    private final Map<String, Object> fields = new HashMap<>();

    public AstreInstance(final AstreStruct struct) {
        this.struct = struct;
    }

    public Object get(final Token name) {
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }

        final AstreFunction method = struct.findMethod(name.lexeme);
        if (method != null) {
            return method.bind(this);
        }

        throw new RuntimeError(name, "Undefined property `" + name.lexeme + "`.");
    }

    public Object set(final Token name, final Object value) {
        fields.put(name.lexeme, value);
        return value;
    }

    @Override
    public String toString() {
        return struct.name + " instance";
    }
}
