package dk.zlepper.itlt;

import com.google.gson.Gson;
import dk.zlepper.itlt.helpers.IconHandler;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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

@Mod(modid = mod.ID, version = mod.VERSION, name = mod.NAME, acceptableRemoteVersions = "*")
public final class Itlt {
    @Mod.Instance("itlt")
    public static Itlt instance;

    @SidedProxy(clientSide = "dk.zlepper.itlt.proxies.ClientProxy", serverSide = "dk.zlepper.itlt.proxies.ServerProxy")
    public static CommonProxy proxy;

    public static Logger logger;
    private boolean makeScreenBigger;
    private String windowDisplayTitle;

    @Mod.EventHandler
    @SideOnly(Side.CLIENT)
    public void preinit(FMLPreInitializationEvent event) throws IOException
    {
        logger = event.getModLog();

        if (proxy instanceof ClientProxy) {
            final Minecraft mcInstance = Minecraft.getMinecraft();

            Configuration config = new Configuration(event.getSuggestedConfigurationFile());
            config.load();
            Property javaBitDetectionProp = config.get("BitDetection", "ShouldYellAt32BitUsers", true);
            javaBitDetectionProp.setComment("Set to true to make itlt yell at people attempting to use 32x java for the modpack.");
            final String yelling = javaBitDetectionProp.getBoolean() ? "We are yelling at people" : "We are NOT yelling at people";
            logger.info(yelling);

            Property javaBitIssueMessageProp = config.get("BitDetection", "ErrorMessage", "You are using a 32 bit version of java. This is not recommended with this modpack.");
            javaBitIssueMessageProp.setComment("If ShouldYellAt32BitUsers is set to true, this is the message that will be displayed to the user.");

            if (javaBitDetectionProp.getBoolean(false)) {
                if (!mcInstance.isJava64bit()) {
                    final ShouterThread st = new ShouterThread(javaBitIssueMessageProp.getString());
                    st.start();
                }
            }

            Property shouldMaximizeDisplayProp = config.get("Display", "ShouldMaximizeDisplay", false);
            shouldMaximizeDisplayProp.setComment("Set to true to make minecraft attempt to maximize itself on startup (This is kinda unstable right now, so don't trust it too much)");
            makeScreenBigger = shouldMaximizeDisplayProp.getBoolean();

            Property windowDisplayTitleProp = config.get("Display", "windowDisplayTitle", "Minecraft " + mcInstance.getVersion());
            windowDisplayTitleProp.setComment("Change this value to change the name of the MineCraft window");
            windowDisplayTitle = windowDisplayTitleProp.getString();

            Property customIconProp = config.get("Display", "loadCustomIcon", true);
            customIconProp.setComment("Set to true to load a custom icon from config" + File.separator + "itlt" + File.separator + "icon.png");
            if (customIconProp.getBoolean()) {
                File customIcon = null;
                final File di = Paths.get(event.getModConfigurationDirectory().getAbsolutePath(), "itlt").toFile();
                logger.info(di);
                if (di.exists()) {
                    final File icoIcon = Paths.get(di.getAbsolutePath(), "icon.ico").toFile();
                    final File icnsIcon = Paths.get(di.getAbsolutePath(), "icon.icns").toFile();
                    final File pngIcon = Paths.get(di.getAbsolutePath(), "icon.png").toFile();
                    logger.info(icoIcon.exists() ? "Custom modpack .ico found" : "Custom modpack .ico NOT found.");
                    logger.info(icnsIcon.exists() ? "Custom modpack .icns found" : "Custom modpack .icns NOT found.");
                    logger.info(pngIcon.exists() ? "Custom modpack .png found" : "Custom modpack .png NOT found.");

                    if (icoIcon.exists() && !icoIcon.isDirectory()) customIcon = icoIcon;
                    else if (icnsIcon.exists() && !icnsIcon.isDirectory()) customIcon = icnsIcon;
                    else if (pngIcon.exists() && !pngIcon.isDirectory()) customIcon = pngIcon;
                    if(customIcon != null) {
                        IconHandler.setWindowIcon(customIcon);
                    } else {
                        logger.warn("loadCustomIcon is true but icon.ico/icns/png is missing or invalid.");
                    }
                } else {
                    logger.error("Directory for custom modpack icon not found!");
                    if (di.mkdir()) {
                        logger.info("Made the directory for you. ");
                    }
                }
            }

            Property useTechnicIconProp = config.get("Display", "useTechnicIcon", true);
            useTechnicIconProp.setComment("Set to true to attempt to use the icon assigned to the modpack by the technic launcher. \nThis will take priority over loadCustomIcon");
            if (useTechnicIconProp.getBoolean()) {
                final Path assets = getAssetDir();

                final File icon = Paths.get(assets.toAbsolutePath().toString(), "icon.png").toFile();
                logger.info(icon.exists() ? "Technic icon found" : "Technic icon NOT found. ");
                if (icon.exists() && !icon.isDirectory()) {
                    Display.setIcon(IconLoader.load(icon));
                }
            }

            Property useTechnicDisplayNameProp = config.get("Display", "useTechnicDisplayName", true);
            useTechnicDisplayNameProp.setComment("Set to true to attempt to get the display name of the pack of the info json file \nThis will take priority over windowDisplayTitle");
            if (useTechnicDisplayNameProp.getBoolean()) {
                final Path assets = getAssetDir();

                final File cacheFile = Paths.get(assets.toAbsolutePath().toString(), "cache.json").toFile();
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
                        final Map cacheContents = new Gson().fromJson(json, Map.class);
                        logger.info(cacheContents.size());
                        if (cacheContents.containsKey("displayName")) {
                            logger.info(cacheContents.get("displayName").toString());
                            windowDisplayTitle = cacheContents.get("displayName").toString();
                        }
                    }
                }
            }

            Property addCustomServerProp = config.get("Server", "AddDedicatedServer", false);
            addCustomServerProp.setComment("Set to true to have a dedicated server added to the server list ingame. The server will not overwrite others servers.");

            Property customServerNameProp = config.get("Server", "ServerName", "Localhost");
            customServerNameProp.setComment("The name of the dedicated server to add.");

            Property customServerIpProp = config.get("Server", "ServerIP", "127.0.0.1:25555");
            customServerIpProp.setComment("The ip of the dedicated server to add.");

            if (addCustomServerProp.getBoolean()) {
                ServerList serverList = new ServerList(mcInstance);
                final int c = serverList.countServers();
                boolean foundServer = false;
                for (int i = 0; i < c; i++) {
                    ServerData data = serverList.getServerData(i);

                    if (data.serverIP.equals(customServerIpProp.getString())) {
                        foundServer = true;
                        break;
                    }
                }
                if (!foundServer) {
                    // The last boolean determines if it is a lan server (true), or an actual multiplayer server (false)
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

    @Mod.EventHandler
    @SideOnly(Side.CLIENT)
    public void contruction(FMLLoadCompleteEvent event) {
        if (proxy instanceof ClientProxy) {
            ClientProxy cp = (ClientProxy) proxy;
            if (makeScreenBigger && !ClientProxy.changed) cp.changeScreen();
            cp.setWindowDisplayTitle(windowDisplayTitle);
        }
    }

}
