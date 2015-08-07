package dk.zlepper.itlt;

import cpw.mods.fml.client.registry.ClientRegistry;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

/**
 * Created by Rasmus on 7/17/2015.
 */
public class KeyBindings {

    private static KeyBinding Enter;
    private static KeyBinding Return;

    public static void init() {
        Enter = new KeyBinding("Enter", Keyboard.KEY_RETURN, "key.categories.itlt");
        Return = new KeyBinding("Other Enter", 156, "key.categories.itlt");
        ClientRegistry.registerKeyBinding(Enter);
        ClientRegistry.registerKeyBinding(Return);
    }

    public static boolean isEnterPressed() {
        return Enter.isPressed() || Return.isPressed();
    }
}
