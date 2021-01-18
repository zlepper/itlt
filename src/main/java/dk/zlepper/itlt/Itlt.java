package dk.zlepper.itlt;

import com.google.gson.Gson;
import dk.zlepper.itlt.about.mod;
import dk.zlepper.itlt.helpers.IconLoader;
import dk.zlepper.itlt.proxies.ClientProxy;
import dk.zlepper.itlt.proxies.CommonProxy;
import dk.zlepper.itlt.proxies.ServerProxy;
import dk.zlepper.itlt.threads.ShouterThread;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Mod("itlt")
public final class Itlt {

    private static final Logger LOGGER = LogManager.getLogger();
    public static CommonProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public Itlt() {
        System.setProperty("java.awt.headless", "false");
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientInit);

        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
        Config.loadConfig(Config.CLIENT_CONFIG, FMLPaths.CONFIGDIR.get().resolve("itlt-client.toml"));

    }

    private void clientInit(final FMLClientSetupEvent event) {
        if (proxy instanceof ServerProxy) {
            LOGGER.info("itlt loaded on server, as itlt is a clientside mod, we aren't doing anything.");
            return;
        }

        final boolean shouldYell = Config.BIT_DETECTION_SHOULD_YELL_AT_32_BIT_USERS.get();
        final String yelling = shouldYell ? "We are yelling at people" : "We are NOT yelling at people";
        LOGGER.info(yelling);

        final Minecraft mcInstance = Minecraft.getInstance();

        if (shouldYell) {
            if (!mcInstance.isJava64bit()) {
                final ShouterThread st = new ShouterThread(Config.BIT_DETECTION_MESSAGE.get());
                st.start();
            }
        }

        String windowDisplayTitle = Config.DISPLAY_WINDOW_DISPLAY_TITLE.get();

        GLFW.glfwSetWindowTitle(mcInstance.mainWindow.getHandle(), windowDisplayTitle);

        if (Config.DISPLAY_LOAD_CUSTOM_ICON.get()) {
            File di = Paths.get(FMLPaths.CONFIGDIR.get().toAbsolutePath().toString(), "itlt").toFile();
            LOGGER.info(di);
            if (di.exists()) {
                final File icon = Paths.get(di.getAbsolutePath(), "icon.png").toFile();
                LOGGER.info(icon.exists() ? "Custom modpack icon found" : "Custom modpack icon NOT found.");
                if (icon.exists() && !icon.isDirectory()) {
                    SetWindowIcon(icon);
                }
            } else {
                LOGGER.error("Directory for custom modpack icon not found!");
                if (di.mkdir()) {
                    LOGGER.info("Made the directory for you. ");
                }
            }
        }

        if (Config.DISPLAY_USE_TECHNIC_ICON.get()) {
            final Path assets = getAssetDir();

            final File icon = Paths.get(assets.toAbsolutePath().toString(), "icon.png").toFile();
            LOGGER.info(icon.exists() ? "Technic icon found" : "Technic icon NOT found. ");
            if (icon.exists() && !icon.isDirectory()) {
                SetWindowIcon(icon);
            }
        }

        if (Config.DISPLAY_USE_TECHNIC_DISPLAY_NAME.get()) {
            Path assets = getAssetDir();

            final File cacheFile = Paths.get(assets.toAbsolutePath().toString(), "cache.json").toFile();
            LOGGER.info(cacheFile.exists() ? "Cache file found" : "Cache file not found.");
            if (cacheFile.exists() && !cacheFile.isDirectory()) {
                String json = null;
                try {
                    json = StringUtils.join(Files.readAllLines(cacheFile.toPath(), StandardCharsets.UTF_8), "");
                    LOGGER.info(json);
                } catch (IOException e) {
                    LOGGER.error(e.toString());
                }
                if (json != null) {
                    final Map cacheContents = new Gson().fromJson(json, Map.class);
                    LOGGER.info(cacheContents.size());
                    if (cacheContents.containsKey("displayName")) {
                        LOGGER.info(cacheContents.get("displayName").toString());
                        windowDisplayTitle = cacheContents.get("displayName").toString();
                    }
                }
            }
        }

        if (Config.SERVER_ADD_DEDICATED_SERVER.get()) {
            ServerList serverList = new ServerList(mcInstance);
            final int c = serverList.countServers();
            boolean foundServer = false;
            for (int i = 0; i < c; i++) {
                ServerData data = serverList.getServerData(i);

                if (data.serverIP.equals(Config.SERVER_SERVER_IP.get())) {
                    foundServer = true;
                    break;
                }
            }
            if (!foundServer) {
                // The last boolean determines if it is a lan server (true), or an actual multiplayer server (false)
                ServerData data = new ServerData(Config.SERVER_SERVER_NAME.get(), Config.SERVER_SERVER_IP.get(), false);
                serverList.addServerData(data);
                serverList.saveServerList();
            }
        }
    }

    private void SetWindowIcon(File icon) {
        try(InputStream is1 = new FileInputStream(icon.getAbsoluteFile())) {
            try(InputStream is2 = new FileInputStream(icon.getAbsoluteFile())) {
                Minecraft.getInstance().mainWindow.setWindowIcon(is1, is2);
                LOGGER.info("Set window icon without issues");
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("Failed to open icon that we just confirmed was there???", e);
        } catch (IOException e) {
            LOGGER.error("Something went wrong when reading the icon file", e);
        }
    }

    private Path getAssetDir() {
        // Get the current Working directory
        final Path currentRelativePath = Paths.get("").toAbsolutePath();
        final String slugname = currentRelativePath.getFileName().toString();

        final Path directParent = currentRelativePath.getParent();
        if (directParent == null) {
            return currentRelativePath;
        }
        // Should be the .technic directory
        final Path technic = directParent.getParent();
        if (technic == null) {
            return currentRelativePath;
        }

        // Should be the asset directory for that modpack
        return Paths.get(technic.toAbsolutePath().toString(), "assets", "packs", slugname);
    }

    /*
    @Mod.EventHandler
    @SideOnly(Side.CLIENT)
    public void contruction(FMLLoadCompleteEvent event) {
        if (proxy instanceof ClientProxy) {
            ClientProxy cp = (ClientProxy) proxy;
            if (makeScreenBigger && !ClientProxy.changed) cp.changeScreen();
            cp.setWindowDisplayTitle(windowDisplayTitle);
        }
    }

     */

}
