package dk.zlepper.itlt.proxies;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

/**
 * Created by Rasmus on 7/16/2015.
 */
public class ClientProxy extends CommonProxy {
    public ClientProxy() {
        // Feature to come
        //KeyBindings.init();
    }

    public static boolean changed = false;

    public void changeScreen() {
        if(!changed) {
            /*GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            int width = gd.getDisplayMode().getWidth();
            int height = gd.getDisplayMode().getHeight();*/
            //Display.getDesktopDisplayMode();
            //DisplayMode dm = new DisplayMode(700, 800, 0, 0, true);
            try {
                Display.setLocation(-7, 0);
                Display.setDisplayMode(Display.getDesktopDisplayMode());
            } catch (LWJGLException e) {
                //e.printStackTrace();
            } catch (IllegalStateException e) {
                //e.printStackTrace();
            }
            changed = true;
        }
    }

    public void setWindowDisplayTitle(String title) {
        Display.setTitle(title);

    }
}
