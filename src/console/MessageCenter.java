package console;

/**
 * Created by Antonio on 16-03-2016.
 */
public class MessageCenter {
    /**
     * The aim of this method is to create a single point of access to the standard output.
     * @param msg message to be printed
     */
    public static void error(String msg) {
        System.err.println(msg);
    }

    /**
     * The aim of this method is to create a single point of access to the standard error output.
     * @param msg message to be printed
     */
    public static void output(String msg) {
        System.out.println(msg);
    }
}
