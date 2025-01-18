package ga.ozli.minecraftmods.itlt.shared.mixins;

import ga.ozli.minecraftmods.itlt.shared.client.ClientUtils;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class WindowTitleMixin {
    @Shadow
    static Minecraft instance;

    @Inject(method = "updateTitle", at = @At("HEAD"), cancellable = true)
    private void itlt$updateTitle(CallbackInfo ci) {
        ClientUtils.setCustomWindowTitle();
        ci.cancel();
    }
}
