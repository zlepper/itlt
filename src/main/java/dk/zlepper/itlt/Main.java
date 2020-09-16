package dk.zlepper.itlt;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public final class Main {

    public static void main(final String[] args) {
        // for debugging
        /*for (int i = 0; i < args.length; i++) {
            System.out.println("arg" + i + ": " + args[i]);
        }*/

        final String
                messageType = args[0],
                messageTitle = args[1],
                messageBody = args[2],
                errorMessage = args[6],
                guideURL = args[7];

        final String[] messageOptions = { args[3], args[4], args[5] };
        final String[] reducedMessageOptions = { args[3], args[4] };

        final int selectedOption;

        // attempt to use the OS theme for the popups rather than the default java theme
        // todo: Manually detect Windows 10 and use modern icons for it
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | InstantiationException | IllegalAccessException exception) {
            // unable to use system theme, ignore the error and continue using the default java theme
        }

        if (messageType.equals("warn")) {
            selectedOption = JOptionPane.showOptionDialog(getParentComponent(), messageBody, messageTitle,
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, messageOptions, messageOptions[0]);
        } else if (messageType.equals("require")) {
            selectedOption = JOptionPane.showOptionDialog(getParentComponent(), messageBody, messageTitle,
                    JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, reducedMessageOptions, reducedMessageOptions[0]);
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
        Actual warning button labels will vary on what the warning is. For example, Button 0 might actually be "Install 64bit Java now" if the messageContent is WantsJava64bit. */
        if (selectedOption == 0) {
            showGuide(guideURL, errorMessage);
        } else if (selectedOption == 2) {
            // Todo: "Don't remind again" functionality
        }

        // prevent the jvm running the popup window from continuing to run after it's closed
        System.exit(0);
    }

    // creates and returns a popup jframe
    private static JFrame getParentComponent() {
        final JFrame parent = new JFrame();
        parent.setAutoRequestFocus(true);
        parent.setAlwaysOnTop(true);
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
