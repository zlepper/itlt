package dk.zlepper.itlt.mixins;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class WindowTitleMixin {
    /**
     * @author Paint_Ninja
     * @reason Restores the ability to change the Minecraft window title
     */
    @Inject(method = "setDefaultMinecraftTitle", at = @At(value = "HEAD"), cancellable = true)
    private void setDefaultMinecraftTitle(CallbackInfo ci) { ci.cancel(); }
}