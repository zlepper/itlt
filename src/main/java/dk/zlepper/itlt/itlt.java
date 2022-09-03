package dk.zlepper.itlt;

import dk.zlepper.itlt.client.ClientConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.network.NetworkConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(itlt.MOD_ID)
public final class itlt {
    public static final String
            MOD_ID = "itlt",
            VERSION = "2.1.5";
    public static final Logger LOGGER = LogManager.getLogger();
    public static ModLoadingContext modLoadingContext;

    public itlt() {
        modLoadingContext = ModLoadingContext.get();

        modLoadingContext.registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));

        ClientConfig.init();
        modLoadingContext.registerConfig(ModConfig.Type.CLIENT, ClientConfig.clientConfig);

        MinecraftForge.EVENT_BUS.register(this);
    }
}
