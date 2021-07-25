package dk.zlepper.itlt.helpers;

public class Platform {
  public static boolean isWindows() {
    return getOsName().toLowerCase().startsWith("windows");
  }
  public static boolean isMac() {
    return getOsName().startsWith("Mac") || getOsName().startsWith("Darwin");
  }
  private static String getOsName() {
    return System.getProperty("os.name");
  }
}