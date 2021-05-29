package dk.zlepper.itlt;

import dk.zlepper.itlt.client.helpers.ClientUtils;
import dk.zlepper.itlt.client.helpers.Platform;
import dk.zlepper.itlt.client.helpers.WarningPreferences;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;

public final class Main {

    /** Fetch an icon from a Windows icon resource DLL
     * For example: getWindowsSystemIcon("imageres", 98, 32); would return the 32px² error icon from imageres.
     * Use a tool such as IconExtract from NirSoft to find out what iconID you want.
     * Note: Icons may change between major Windows releases. **/
    @Nullable
    public static Icon getWindowsSystemIcon(final String resourceDll, final int iconID, final int size) {
        try {
            final Class<?> clazz = Class.forName("sun.awt.shell.Win32ShellFolder2");

            // note: getIconResource will return 0 if the request icon cannot be found
            final Method m = clazz.getDeclaredMethod("getIconResource", String.class, int.class, int.class,
                    int.class, boolean.class);
            m.setAccessible(true);
            final long hIcon = (long) m.invoke(null, resourceDll + ".dll", iconID, size, size, true);

            final Method makeIconMethod = clazz.getDeclaredMethod("makeIcon", long.class, boolean.class);
            makeIconMethod.setAccessible(true);
            final Image image = (Image) makeIconMethod.invoke(null, hIcon, true);

            final Method disposeIconMethod = clazz.getDeclaredMethod("disposeIcon", long.class);
            disposeIconMethod.setAccessible(true);
            disposeIconMethod.invoke(null, hIcon);

            return new ImageIcon(image);
        } catch (final IllegalAccessException e) {
            // Don't show the illegal access stacktrace on Java 16+
            if (ClientUtils.getJavaVersion() > 15)
                System.err.println("Warn: Please run with JVM's \"permit illegal access\" flag for the best experience on Windows.");
            else e.printStackTrace();
            return null;
        } catch (final InvocationTargetException | NoSuchMethodException | ClassNotFoundException | ClassCastException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(final String[] args) {
        // for debugging
        /*for (int i = 0; i < args.length; i++) {
            System.out.println("arg" + i + ": " + args[i]);
        }*/

        // attempt to use the OS theme for the popups rather than the default java theme
        Icon infoIcon = null;
        Icon warningIcon = null;
        Icon errorIcon = null;
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Manually detect Windows and use modern icons for it
            if (Platform.isWindows()) {
                infoIcon = getWindowsSystemIcon("imageres", 81, 32);
                warningIcon = getWindowsSystemIcon("imageres", 84, 32);
                errorIcon = getWindowsSystemIcon("imageres", 98, 32);
            }
        } catch (final ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException
                | InstantiationException ignored) {
            // unable to use system theme, ignore the error and continue using the default Java theme
        }

        // prevent an error from showing if something/someone tries running the jar directly
        try {
            System.out.println("messageContent: " + args[8]);
        } catch (final ArrayIndexOutOfBoundsException e) {
            final String helpMsg = "To install this mod, place me in the mods folder of a Minecraft Forge 1.13.2 setup.";
            JOptionPane.showMessageDialog(getParentComponent(), helpMsg, "It's the little things mod",
                    JOptionPane.INFORMATION_MESSAGE, infoIcon);
            System.out.println(helpMsg);
            System.exit(0);
        }

        // grab the args given to us and put them in more human-readable variables
        final String
                messageType = args[0],
                messageTitle = args[1],
                messageBody = args[2],
                errorMessage = args[6],
                guideURL = args[7],
                messageContent = args[8];

        final String[]
                messageOptions = { args[3], args[4], args[5] },
                reducedMessageOptions = { args[3], args[4] };

        // check the user's preference about being asked if available
        final WarningPreferences warningPreferences = new WarningPreferences();
        warningPreferences.load();

        final int selectedOption;

        if (messageType.equals("wants")) {
            if (warningPreferences.getBoolInt(messageContent, true)) {
                selectedOption = JOptionPane.showOptionDialog(getParentComponent(), messageBody, messageTitle,
                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, warningIcon, messageOptions, messageOptions[0]);
            } else {
                System.out.println("Skipping warning as the user has requested that we don't ask them about it again.");
                selectedOption = 1;
            }
        } else if (messageType.equals("needs")) {
            selectedOption = JOptionPane.showOptionDialog(getParentComponent(), messageBody, messageTitle,
                    JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, errorIcon, reducedMessageOptions, reducedMessageOptions[0]);
        } else {
            selectedOption = 1; // Emulate clicking the "Ignore"/"Remind me later" button if the messageType is invalid
        }

        /* selectedOption number is counted left to right from the buttons, like this:

        /-- messageTitle ----------- _ □ X -\
        ¦ (messageIcon) messageBody         ¦
        ¦ [Button 0] [Button 1] [Button 2]  ¦
        \-----------------------------------/

        E.g.: If the user clicks "Button 2", the int of selectedOption would be 2.

        For warnings, Button 0 is likely "Read how-to guide", Button 1 is likely "Remind me later", Button 2 "Don't remind again".
        Actual warning button labels will vary on what the warning is.
        For example, Button 0 might actually be "Install 64bit Java now" if messageContent == WantsJava64bit. */
        if (selectedOption == 0) {
            showGuide(guideURL, errorMessage);
        } else if (selectedOption == 2) {
            warningPreferences.setBoolInt(messageContent, false);
            warningPreferences.save();
        }

        // prevent the jvm running the popup window from continuing to run after it's closed
        System.exit(0);
    }

    // creates and returns a popup jframe
    private static JFrame getParentComponent() {
        final JFrame parent = new JFrame();
        parent.setAutoRequestFocus(true);
        parent.setAlwaysOnTop(true);
        //parent.getContentPane().setBackground(Color.DARK_GRAY);
        //parent.getContentPane().setForeground(Color.WHITE);
        return parent;
    }

    private static void showGuide(final String guideURL, final String errorMessage) {
        try {
            final URI guideURI = new URI(guideURL);
            Desktop.getDesktop().browse(guideURI);
        } catch (final IOException | URISyntaxException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(getParentComponent(), errorMessage, null, JOptionPane.ERROR_MESSAGE);
        }
    }
}
