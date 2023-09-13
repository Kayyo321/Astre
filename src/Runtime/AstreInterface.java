package Runtime;

import java.util.Map;

public class AstreInterface {
    public final String name;
    public final Map<String, Integer> methods;

    public AstreInterface(final String name, final Map<String, Integer> methods) {
        this.name = name;
        this.methods = methods;
    }

    @Override
    public String toString() {
        return name + " Interface";
    }
}
