package dk.zlepper.itlt.threads;

import dk.zlepper.itlt.Itlt;
import dk.zlepper.itlt.proxies.ClientProxy;

import javax.swing.*;

public class ShouterThread extends Thread {

    private String message;

    public ShouterThread(String message) {
        this.message = message;
    }

    @Override
    public void run() {
        if(Itlt.proxy instanceof ClientProxy) {
            JOptionPane.showMessageDialog(null, message, "Java version issue", JOptionPane.WARNING_MESSAGE);
        } else {
//            Itlt.logger.warn(message);
        }
    }
}
