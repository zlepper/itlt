package dk.zlepper.itlt.mixins;

import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public class WindowTitleMixin {
    /**
     * @author Paint_Ninja
     * @reason Restores the ability to change the Minecraft window title
     */
    @Inject(method = "setTitle", at = @At(value = "HEAD"), cancellable = true)
    private void setTitle(CallbackInfo ci) { ci.cancel(); }
}