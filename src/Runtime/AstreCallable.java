package Runtime;

import java.util.List;

public interface AstreCallable {
    default int arity() {
        return 0;
    }

    default Object call(Interpreter interpreter, List<Object> arguments) {
        return null;
    }
}