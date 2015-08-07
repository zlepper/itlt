package dk.zlepper.itlt.proxies;

import dk.zlepper.itlt.KeyBindings;
import net.minecraft.client.Minecraft;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.*;
import org.lwjgl.opengl.DisplayMode;

import java.awt.*;
import java.lang.reflect.Field;

/**
 * Created by Rasmus on 7/16/2015.
 */
public class ClientProxy extends CommonProxy {
    public ClientProxy() {
        KeyBindings.init();
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
            } catch (LWJGLException | IllegalStateException e) {
                //e.printStackTrace();
            }
            changed = true;
        }
    }

    public void setWindowDisplayTitle(String title) {
        Display.setTitle(title);

    }
}
