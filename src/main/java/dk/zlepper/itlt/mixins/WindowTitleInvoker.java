package dk.zlepper.itlt.mixins;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface WindowTitleInvoker {
    @Invoker("getWindowTitle")
    static String getWindowTitle() {
        throw new AssertionError(); // this should only run if the Mixin fails
    }
}
