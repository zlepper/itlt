package dk.zlepper.itlt;

import com.google.gson.Gson;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import dk.zlepper.itlt.about.mod;
import dk.zlepper.itlt.helpers.IconLoader;
import dk.zlepper.itlt.proxies.ClientProxy;
import dk.zlepper.itlt.proxies.CommonProxy;
import dk.zlepper.itlt.threads.ShouterThread;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Mod(modid = mod.ID, version = mod.VERSION, name = mod.NAME)
public class Itlt {
    @Mod.Instance("itlt")
    public static Itlt instance;

    @SidedProxy(clientSide = "dk.zlepper.itlt.proxies.ClientProxy", serverSide = "dk.zlepper.itlt.proxies.ServerProxy")
    public static CommonProxy proxy;

    public static Logger logger;
    private boolean makeScreenBigger;
    private String windowDisplayTitle;

    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        logger = event.getModLog();

        if (proxy instanceof ClientProxy) {
            Configuration config = new Configuration(event.getSuggestedConfigurationFile());
            config.load();
            Property javaBitDetectionProp = config.get("BitDetection", "ShouldYellAt32BitUsers", false);
            javaBitDetectionProp.comment = "Set to true to make itlt yell at people attempting to use 32x java for the modpack.";
            String yelling = javaBitDetectionProp.getBoolean() ? "We are yelling at people" : "We are NOT yelling at people";
            logger.info(yelling);

            Property javaBitIssueMessageProp = config.get("BitDetection", "ErrorMessage", "You are using a 32 bit version of java. This is not recommended with this modpack.");
            javaBitIssueMessageProp.comment = "If ShouldYellAt32BitUsers is set to true, this is the message that will be displayed to the user.";

            if (javaBitDetectionProp.getBoolean(false)) {
                if (!Minecraft.getMinecraft().isJava64bit()) {
                    ShouterThread st = new ShouterThread(javaBitIssueMessageProp.getString());
                    st.start();
                }
            }

            Property shouldMaximizeDisplayProp = config.get("Display", "ShouldMaximizeDisplay", false);
            shouldMaximizeDisplayProp.comment = "Set to true to make minecraft attempt to maximize itself on startup (This is kinda unstable right now, so don't trust it too much)";
            makeScreenBigger = shouldMaximizeDisplayProp.getBoolean();

            Property windowDisplayTitleProp = config.get("Display", "windowDisplayTitle", "Minecraft " + Minecraft.getMinecraft().getVersion());
            windowDisplayTitleProp.comment = "Change this value to change the name of the MineCraft window";
            windowDisplayTitle = windowDisplayTitleProp.getString();

            Property customIconProp = config.get("Display", "loadCustomIcon", true);
            customIconProp.comment = "Set to true to load a custom icon from config" + File.separator + "itlt" + File.separator + "icon.png";
            if (customIconProp.getBoolean()) {
                File di = Paths.get(event.getModConfigurationDirectory().getAbsolutePath(), "itlt").toFile();
                logger.info(di);
                if (di.exists()) {
                    File icon = Paths.get(di.getAbsolutePath(), "icon.png").toFile();
                    logger.info(icon.exists() ? "Custom modpack icon found" : "Custom modpack icon NOT found.");
                    if (icon.exists() && !icon.isDirectory()) {
                        Display.setIcon(IconLoader.load(icon));
                    }
                } else {
                    logger.error("Directory for custom modpack icon not found!");
                    if (di.mkdir()) {
                        logger.info("Made the directory for you. ");
                    }
                }
            }

            Property useTechnicIconProp = config.get("Display", "useTechnicIcon", true);
            useTechnicIconProp.comment = "Set to true to attempt to use the icon assigned to the modpack by the technic launcher. \nThis will take priority over loadCustomIcon";
            if (useTechnicIconProp.getBoolean()) {
                Path assets = getAssetDir();

                File icon = Paths.get(assets.toAbsolutePath().toString(), "icon.png").toFile();
                logger.info(icon.exists() ? "Technic icon found" : "Technic icon NOT found. ");
                if (icon.exists() && !icon.isDirectory()) {
                    Display.setIcon(IconLoader.load(icon));
                }
            }

            Property useTechnicDisplayNameProp = config.get("Display", "useTechnicDisplayName", true);
            useTechnicDisplayNameProp.comment = "Set to true to attempt to get the display name of the pack of the info json file \nThis will take priority over windowDisplayTitle";
            if (useTechnicDisplayNameProp.getBoolean()) {
                Path assets = getAssetDir();

                File cacheFile = Paths.get(assets.toAbsolutePath().toString(), "cache.json").toFile();
                logger.info(cacheFile.exists() ? "Cache file found" : "Cache file not found.");
                if (cacheFile.exists() && !cacheFile.isDirectory()) {
                    String json = null;
                    try {
                        json = StringUtils.join(Files.readAllLines(cacheFile.toPath(), StandardCharsets.UTF_8), "");
                        logger.info(json);
                    } catch (IOException e) {
                        logger.error(e.toString());
                    }
                    if (json != null) {
                        Map cacheContents = new Gson().fromJson(json, Map.class);
                        logger.info(cacheContents.size());
                        if (cacheContents.containsKey("displayName")) {
                            logger.info(cacheContents.get("displayName").toString());
                            windowDisplayTitle = cacheContents.get("displayName").toString();
                        }
                    }
                }
            }

            Property addCustomServerProp = config.get("Server", "AddDedicatedServer", false);
            addCustomServerProp.comment = "Set to true to have a dedicated server added to the server list ingame. The server will not overwrite others servers.";

            Property customServerNameProp = config.get("Server", "ServerName", "Localhost");
            customServerNameProp.comment = "The name of the dedicated server to add.";

            Property customServerIpProp = config.get("Server", "ServerIP", "127.0.0.1:25555");
            customServerIpProp.comment = "The ip of the dedicated server to add.";

            if (addCustomServerProp.getBoolean()) {
                ServerList serverList = new ServerList(Minecraft.getMinecraft());
                int c = serverList.countServers();
                boolean foundServer = false;
                for (int i = 0; i < c; i++) {
                    ServerData data = serverList.getServerData(i);

                    if (data.serverIP.equals(customServerIpProp.getString())) {
                        foundServer = true;
                        break;
                    }
                }
                if (!foundServer) {
                    // I have no clue what the last boolean is for.
                    // Possibly decides if it's a lan server, or an actual multiplayer server.
                    // Settings it to false should make it a multiplayer server
                    ServerData data = new ServerData(customServerNameProp.getString(), customServerIpProp.getString(), false);
                    serverList.addServerData(data);
                    serverList.saveServerList();
                }
            }

            config.save();
        }
    }

    private Path getAssetDir() {
        // Get the current Working directory
        Path currentRelativePath = Paths.get("").toAbsolutePath();
        String slugname = currentRelativePath.getFileName().toString();

        Path directParent = currentRelativePath.getParent();
        if (directParent == null) {
            return currentRelativePath;
        }

        // Should be the .technic directory
        Path technic = directParent.getParent();
        if (technic == null) {
            return currentRelativePath;
        }

        // Should be the asset directory for that modpack
        return Paths.get(technic.toAbsolutePath().toString(), "assets", "packs", slugname);
    }

    @Mod.EventHandler
    public void contruction(FMLLoadCompleteEvent event) {
        if (proxy instanceof ClientProxy) {
            ClientProxy cp = (ClientProxy) proxy;
            if (makeScreenBigger && !ClientProxy.changed) cp.changeScreen();
            cp.setWindowDisplayTitle(windowDisplayTitle);
        }
    }

}