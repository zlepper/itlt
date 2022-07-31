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
     */
    @Inject(method = "updateTitle", at = @At(value = "HEAD"), cancellable = true)
    private void updateTitle(CallbackInfo ci) {
        instance.getWindow().setTitle(ClientUtils.getCustomWindowTitle(instance));
        ci.cancel();
    }

    @Shadow
    static Minecraft instance;
}