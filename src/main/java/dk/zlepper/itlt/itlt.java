package dk.zlepper.itlt;

import dk.zlepper.itlt.client.ClientConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(itlt.MOD_ID)
public final class itlt {
    public static final String MOD_ID = "itlt";
    public static final Logger LOGGER = LogManager.getLogger();

    public itlt() {
        MinecraftForge.EVENT_BUS.register(this);

        final ModLoadingContext modLoadingContext = ModLoadingContext.get();

        modLoadingContext.registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        modLoadingContext.registerConfig(ModConfig.Type.CLIENT, ClientConfig.clientConfig);
        ClientConfig.loadConfig(ClientConfig.clientConfig, FMLPaths.CONFIGDIR.get().resolve("itlt-client.toml"));
    }
}
