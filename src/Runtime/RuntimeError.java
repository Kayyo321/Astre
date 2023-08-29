package Runtime;

import LexicalAnalysis.Token;

public class RuntimeError extends RuntimeException {
    public final Token token;

    public RuntimeError(final Token token, final String message) {
        super(message);
        this.token = token;
    }
}