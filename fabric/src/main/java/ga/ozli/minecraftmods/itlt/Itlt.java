package ga.ozli.minecraftmods.itlt;

import fuzs.forgeconfigapiport.fabric.api.forge.v4.ForgeConfigRegistry;
import ga.ozli.minecraftmods.itlt.client.ClientModEvents;
import ga.ozli.minecraftmods.itlt.shared.CommonClass;
import ga.ozli.minecraftmods.itlt.shared.CommonConfig;
import ga.ozli.minecraftmods.itlt.shared.Constants;
import ga.ozli.minecraftmods.itlt.shared.client.ClientConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.neoforged.fml.config.ModConfig;

public final class Itlt implements ModInitializer {
    @Override
    public void onInitialize() {
        ForgeConfigRegistry.INSTANCE.register(Constants.MOD_ID, ModConfig.Type.COMMON, CommonConfig.SPEC);

        // todo: crash callable

        CommonClass.checkJavaVersion();
        CommonClass.checkJavaMemory();

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ForgeConfigRegistry.INSTANCE.register(Constants.MOD_ID, ModConfig.Type.CLIENT, ClientConfig.SPEC);

            ClientLifecycleEvents.CLIENT_STARTED.register(ClientModEvents::onClientSetup);
            ScreenEvents.BEFORE_INIT.register(ClientModEvents::onLoadComplete);
        } else {
            ServerLifecycleEvents.SERVER_STARTED.register(server -> {
                if (CommonConfig.Java.Advanced.GC_ON_STARTUP) {
                    System.gc();
                }
            });
        }
    }
}
