package dk.zlepper.itlt.client.helpers;

public class Platform {
    public static boolean isWindows() {
        return getOsName().startsWith("Windows");
    }
    public static boolean isMac() {
        return getOsName().startsWith("Mac") || getOsName().startsWith("Darwin");
    }
    private static String getOsName() {
        return System.getProperty("os.name");
    }
}
