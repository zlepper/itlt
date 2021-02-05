package dk.zlepper.itlt.mixins;

import net.minecraftforge.fml.network.FMLHandshakeHandler;
import net.minecraftforge.fml.network.FMLHandshakeMessages;
import net.minecraftforge.fml.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

import static dk.zlepper.itlt.server.helpers.ServerUtils.itltHandleClientModListOnServer;

@Mixin(FMLHandshakeHandler.class)
public class FMLHandshakeHandlerMixin {

    // note: remap needs to be false for injecting methods added by Forge
    @Inject(method = "handleClientModListOnServer", at = @At("TAIL"), remap = false)
    private void handleClientModListOnServer(FMLHandshakeMessages.C2SModListReply clientModList, Supplier<NetworkEvent.Context> c, CallbackInfo ci) {
        itltHandleClientModListOnServer(clientModList, c);
    }
}