package dk.zlepper.itlt.proxies;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;

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
        	Minecraft minecraft = Minecraft.getMinecraft();
            if (!minecraft.isFullScreen()) {
            	minecraft.toggleFullscreen();
            }
            changed = true;
        }
    }

    public void setWindowDisplayTitle(String title) {
        Display.setTitle(title);

    }
}
