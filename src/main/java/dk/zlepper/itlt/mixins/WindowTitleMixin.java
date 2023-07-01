package dk.zlepper.itlt.mixins;

import dk.zlepper.itlt.MinecraftExtension;
import dk.zlepper.itlt.client.helpers.ClientUtils;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;

@Mixin(Minecraft.class)
public class WindowTitleMixin implements MinecraftExtension {
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

    @Override
    public void updateIcon(final File inputIconFile) throws IOException {
        ClientUtils.setWindowIcon(inputIconFile, instance);
    }
}