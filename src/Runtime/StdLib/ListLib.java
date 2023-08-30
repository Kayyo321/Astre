package Runtime.StdLib;

import java.util.function.Consumer;

import Runtime.Environment;

public class ListLib {
    public static Consumer<Environment> builder = ListLib::build;

    private static void build(Environment environment) {

    }
}
