package comiam.sapper.util;

public class IOUtils
{
    public static <T> void print(T message)
    {
        System.console().printf("%s", message.toString());
        System.console().flush();
    }

    public static <T> void println(T message)
    {
        System.console().printf("%s\n", message.toString());
        System.console().flush();
    }

    public static void println()
    {
        println("");
    }
}
