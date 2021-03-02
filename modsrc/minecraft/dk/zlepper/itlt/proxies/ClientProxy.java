package dk.zlepper.itlt.proxies;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import net.minecraft.client.Minecraft;
import net.minecraft.src.GameSettings;

/**
 * Created by Rasmus on 7/16/2015.
 */
public class ClientProxy {
	
	private static MethodHandle theMinecraftGet = null;

    static {
        Field field;
        try {
        	// MCP: Minecraft.theMinecraft
        	// SRG: field_21900_a - obtained by searching for the MCP name in itlt/conf/fields.csv
        	// NOTCH: a - obtained by searching for the SRG name in itlt/conf/client.srg
        	// I don't remember what the dev env uses, so if this doesn't work change it to one that 
        	// works and then change it back to the notch one before recompiling and reobfuscating
            field = Minecraft.class.getDeclaredField("a");
            field.setAccessible(true);
            theMinecraftGet = MethodHandles.publicLookup().unreflectGetter(field);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
	
    public ClientProxy() {
        // Feature to come
        //KeyBindings.init();
    }

    public static boolean changed = false;

    public static void changeScreen() {
    	Minecraft minecraft;
        try {
            minecraft = (Minecraft) theMinecraftGet.invoke();
        } catch (Throwable throwable) {
            minecraft = null;
            throwable.printStackTrace();
        }
        
        if(!changed) {
            //if (!minecraft.fullscreen) {
            minecraft.toggleFullscreen();
            // }
            changed = true;
        }
    }

    public static void setWindowDisplayTitle(String title) {
        Display.setTitle(title);
    }
}
