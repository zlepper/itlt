package dk.zlepper.itlt.mixins;

import dk.zlepper.itlt.client.helpers.ClientUtils;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class WindowTitleMixin {
    /**
     * @author Paint_Ninja
     * @reason Restores the ability to change the Minecraft window title
     * func_230150_b_ == setDefaultMinecraftTitle == updateTitle
     * func_230148_b_ == setWindowTitle == setTitle
     */
    @Inject(method = "func_230150_b_", at = @At(value = "HEAD"), cancellable = true)
    private void func_230150_b_(CallbackInfo ci) {
        instance.getMainWindow().func_230148_b_(ClientUtils.getCustomWindowTitle(instance));
        ci.cancel();
    }

    @Shadow
    private static Minecraft instance;
}