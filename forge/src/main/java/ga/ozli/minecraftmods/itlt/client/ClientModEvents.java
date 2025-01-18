package ga.ozli.minecraftmods.itlt.client;

import ga.ozli.minecraftmods.itlt.platform.Services;
import ga.ozli.minecraftmods.itlt.shared.client.ClientConfig;
import ga.ozli.minecraftmods.itlt.shared.client.ClientUtils;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public final class ClientModEvents {
    public static void onConstruct(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
    }

    public static void onClientSetup(FMLClientSetupEvent event) {
        ClientConfig.load();
        ClientConfig.ready = true;

        // using enqueueWork to make sure this runs on the render thread
        event.enqueueWork(() -> {
            if (ClientConfig.Graphics.WRONG_GPU && ClientUtils.runningOnWrongGPU()) {
                Services.PLATFORM.addWarning("Running on wrong GPU");
            }
        });
    }

    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        event.enqueueWork(ClientUtils::setCustomIcon);
        event.enqueueWork(ClientUtils::setCustomWindowTitle);
    }

    private ClientModEvents() {}
}
