package ga.ozli.minecraftmods.itlt;

import ga.ozli.minecraftmods.itlt.client.ClientModEvents;
import ga.ozli.minecraftmods.itlt.shared.CommonClass;
import ga.ozli.minecraftmods.itlt.shared.CommonConfig;
import ga.ozli.minecraftmods.itlt.shared.Constants;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.CrashReportCallables;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(Constants.MOD_ID)
public record Itlt(FMLJavaModLoadingContext context) {
    public Itlt {
        context.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);

        var modBus = context.getModEventBus();
        modBus.addListener(EventPriority.NORMAL, false, FMLCommonSetupEvent.class, this::onCommonSetup);

        if (FMLEnvironment.dist.isClient()) {
            ClientModEvents.onConstruct(context);
            modBus.addListener(EventPriority.NORMAL, false, FMLClientSetupEvent.class, ClientModEvents::onClientSetup);
            modBus.addListener(EventPriority.NORMAL, false, FMLLoadCompleteEvent.class, ClientModEvents::onLoadComplete);
        }
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        CommonConfig.load();

        CrashReportCallables.registerCrashCallable("Modpack details", CommonConfig.ModpackInfo.INSTANCE::toFriendlyString);

        CommonClass.checkJavaVersion();
        CommonClass.checkJavaMemory();

        if (CommonConfig.Java.Advanced.GC_ON_STARTUP) {
            var modBus = context.getModEventBus();
            if (FMLEnvironment.dist.isClient()) {
                modBus.addListener(EventPriority.MONITOR, false, FMLLoadCompleteEvent.class, e -> System.gc());
            } else {
                modBus.addListener(EventPriority.MONITOR, false, ServerStartedEvent.class, e -> System.gc());
            }
        }
    }
}
