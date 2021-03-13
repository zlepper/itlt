package dk.zlepper.itlt.client.helpers;

import java.io.*;
import java.util.Properties;

public class WarningPreferences {

    private static final String warningPrefsFileStr = "config/itlt/warning.properties";
    private static final File warningPrefsFile = new File(warningPrefsFileStr);
    public final Properties properties = new Properties();

    public WarningPreferences() {
        // create the warning preferences file if it doesn't already exists
        if (!warningPrefsFile.exists()) {
            try {
                System.out.println(warningPrefsFile);
                if (!warningPrefsFile.createNewFile())
                    System.err.println("Error: Unable to create warning.properties for storing warning preferences.");
            } catch (final IOException e) {
                System.err.println("Error: Unable to create warning.properties for storing warning preferences.");
                e.printStackTrace();
            }
        }
    }

    public void load() {
        try {
            final InputStream input = new FileInputStream(warningPrefsFileStr);
            properties.load(input);
        } catch (final IOException e) {
            final File warningPropertiesFile = new File(warningPrefsFileStr);
            if (warningPropertiesFile.exists() && !warningPropertiesFile.isDirectory()) {
                // the file exists but we're having trouble reading it, so delete it
                System.err.println("Error: Warning preferences file found but can't be read, deleting...");
                if (!warningPropertiesFile.delete()) warningPropertiesFile.deleteOnExit();
            }
        }
    }

    public void save() {
        try {
            final OutputStream output = new FileOutputStream(warningPrefsFile);
            properties.store(output, null);
        } catch (final IOException e) {
            System.err.println("Error: Unable to write warning preferences to file.");
            e.printStackTrace();
        }
    }

    /** Note: This will return false if the property is not found.
     * Make sure you load() your WarningPreferences first. **/
    public boolean getBoolInt(final String keyName) {
        return properties.getProperty(keyName, "0").equals("1");
    }

    /** Make sure you load() your WarningPreferences first. **/
    public boolean getBoolInt(final String keyName, final boolean defaultValue) {
        return properties.getProperty(keyName, convertBoolToString(defaultValue)).equals("1");
    }

    public void setBoolInt(final String keyName, final boolean keyBoolValue) {
        properties.setProperty(keyName, convertBoolToString(keyBoolValue));
    }

    private static String convertBoolToString(final boolean bool) {
        if (bool) return "1";
        else return "0";
    }
}
