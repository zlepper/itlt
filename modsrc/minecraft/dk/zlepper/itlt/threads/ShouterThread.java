package dk.zlepper.itlt.threads;

import dk.zlepper.itlt.mod_Itlt;
import dk.zlepper.itlt.proxies.ClientProxy;

import javax.swing.*;
import java.util.logging.Level;

public class ShouterThread extends Thread {

    private String message;

    public ShouterThread(String message) {
        this.message = message;
    }

    @Override
    public void run() {
        try {
            JOptionPane.showMessageDialog(null, message, "Java version issue", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            mod_Itlt.logger.log(Level.WARNING, message);
        }
    }
}
