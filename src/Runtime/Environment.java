package Runtime;

import java.util.HashMap;
import java.util.Map;

import LexicalAnalysis.*;
import Parsing.Stmt.Modifier;

public class Environment {
    private final Map<String, Object> values = new HashMap<>();
    private final Map<String, Modifier> modifiers = new HashMap<>();
    public final Environment enclosing;

    public Environment() {
        this.enclosing = null;
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    public Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        } else if (enclosing != null) {
            return enclosing.get(name);
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    public void define(Token keyword, Modifier modifier, String name, Object value) {
        values.put(name, value);
        modifiers.put(name, modifier);

        if ((modifier != Modifier.Nullable && modifier != Modifier.Both) && value == null) {
            throw new RuntimeError(keyword, "Cannot assign `nothing` to variable that doesn't accept the `nothing` value (put a `?` after `let` to allow it).");
        }
    }

    public Object getAt(int distance, String name) {
        return ancestor(distance).values.get(name);
    }

    public Environment ancestor(int distance) {
        Environment environment = this;
        for (int i = 0; i < distance; ++i) {
            assert environment != null;
            environment = environment.enclosing;
        }
        return environment;
    }

    public void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            if (modifiers.get(name.lexeme) == Modifier.Constant || modifiers.get(name.lexeme) == Modifier.Both) {
                throw new RuntimeError(name, "Cannot assign variable which was declared constant");
            }
            if (modifiers.get(name.lexeme) != Modifier.Nullable && value == null) {
                throw new RuntimeError(name, "Cannot assign variable which doesn't accept `nothing` values (put `?` after `let` to allow it).");
            }
            values.put(name.lexeme, value);
            return;
        } else if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    public void assignAt(int distance, Token name, Object value) {
        ancestor(distance).values.put(name.lexeme, value);
    }
}