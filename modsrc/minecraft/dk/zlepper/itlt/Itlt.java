package dk.zlepper.itlt;

//import com.google.gson.Gson;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import dk.zlepper.itlt.about.mod;
import dk.zlepper.itlt.helpers.IconLoader;
import dk.zlepper.itlt.proxies.ClientProxy;
import dk.zlepper.itlt.proxies.CommonProxy;
import dk.zlepper.itlt.threads.ShouterThread;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;
//import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.Display;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Mod(modid = mod.ID, version = mod.VERSION, name = mod.NAME, acceptedMinecraftVersions = "[1.4.4,1.4.5]")
public class Itlt {
    @Mod.Instance("itlt")
    public static Itlt instance;

    @SidedProxy(clientSide = "dk.zlepper.itlt.proxies.ClientProxy", serverSide = "dk.zlepper.itlt.proxies.ServerProxy")
    public static CommonProxy proxy;

    public static Logger logger;
    private boolean makeScreenBigger;
    private String windowDisplayTitle;

    /*private static MethodHandle launchedVersionGet = null;

    static {
        Field field;
        try {
            field = Minecraft.class.getDeclaredField("field_110447_Z"); // Minecraft.launchedVersion
            field.setAccessible(true);
            launchedVersionGet = MethodHandles.publicLookup().unreflectGetter(field);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }*/

    @Mod.PreInit
    public void preinit(FMLPreInitializationEvent event) {
        logger = event.getModLog();

        if (proxy instanceof ClientProxy) {
            Configuration config = new Configuration(event.getSuggestedConfigurationFile());
            config.load();
            Property javaBitDetectionProp = config.get("BitDetection", "ShouldYellAt32BitUsers", false);
            javaBitDetectionProp.comment = "Set to true to make itlt yell at people attempting to use 32x java for the modpack.";
            String yelling = javaBitDetectionProp.getBoolean(false) ? "We are yelling at people" : "We are NOT yelling at people";
            logger.info(yelling);

            Property javaBitIssueMessageProp = config.get("BitDetection", "ErrorMessage", "You are using a 32 bit version of java. This is not recommended with this modpack.");
            javaBitIssueMessageProp.comment = "If ShouldYellAt32BitUsers is set to true, this is the message that will be displayed to the user.";

            if (javaBitDetectionProp.getBoolean(false)) {
                if (!isJava64bit()) {
                    ShouterThread st = new ShouterThread(javaBitIssueMessageProp.value);
                    st.start();
                }
            }

            Property shouldMaximizeDisplayProp = config.get("Display", "ShouldMaximizeDisplay", false);
            shouldMaximizeDisplayProp.comment = "Set to true to make minecraft attempt to maximize itself on startup (This is kinda unstable right now, so don't trust it too much)";
            makeScreenBigger = shouldMaximizeDisplayProp.getBoolean(false);

            /*String launchedVersion;
            try {
                launchedVersion = (String) launchedVersionGet.invokeExact((Minecraft) Minecraft.getMinecraft());
            } catch (Throwable throwable) {
                launchedVersion = "";
                throwable.printStackTrace();
            }
            Property windowDisplayTitleProp = config.get("Display", "windowDisplayTitle", "Minecraft 1.5.2");
            windowDisplayTitleProp.comment = "Change this value to change the name of the MineCraft window";
            windowDisplayTitle = windowDisplayTitleProp.getString();

            Property customIconProp = config.get("Display", "loadCustomIcon", true);
            customIconProp.comment = "Set to true to load a custom icon from config" + File.separator + "itlt" + File.separator + "icon.png";
            if (customIconProp.getBoolean(true)) {
                File di = Paths.get(event.getModConfigurationDirectory().getAbsolutePath(), "itlt").toFile();
                logger.info(di.toString());
                if (di.exists()) {
                    File icon = Paths.get(di.getAbsolutePath(), "icon.png").toFile();
                    logger.info(icon.exists() ? "Custom modpack icon found" : "Custom modpack icon NOT found.");
                    if (icon.exists() && !icon.isDirectory()) {
                        Display.setIcon(IconLoader.load(icon));
                    }
                } else {
                    logger.log(Level.SEVERE, "Directory for custom modpack icon not found!");
                    if(di.mkdir()) {
                        logger.info("Made the directory for you. ");
                    }
                }
            }

            Property useTechnicIconProp = config.get("Display", "useTechnicIcon", true);
            useTechnicIconProp.comment = "Set to true to attempt to use the icon assigned to the modpack by the technic launcher. \nThis will take priority over loadCustomIcon";
            if(useTechnicIconProp.getBoolean(true)) {
                Path assets = getAssetDir();

                File icon = Paths.get(assets.toAbsolutePath().toString(), "icon.png").toFile();
                logger.info(icon.exists() ? "Technic icon found" : "Technic icon NOT found. ");
                if(icon.exists() && !icon.isDirectory()) {
                    Display.setIcon(IconLoader.load(icon));
                }
            }

            Property useTechnicDisplayNameProp = config.get("Display", "useTechnicDisplayName", true);
            useTechnicDisplayNameProp.comment = "Set to true to attempt to get the display name of the pack of the info json file \nThis will take priority over windowDisplayTitle";
            if(useTechnicDisplayNameProp.getBoolean(true)) {
                Path assets = getAssetDir();

                File cacheFile = Paths.get(assets.toAbsolutePath().toString(), "cache.json").toFile();
                logger.info(cacheFile.exists() ? "Cache file found" : "Cache file not found.");
                if(cacheFile.exists() && !cacheFile.isDirectory()) {
                    String json = null;
                    try {
                        json = StringUtils.join(Files.readAllLines(cacheFile.toPath(), StandardCharsets.UTF_8), "");
                        logger.info(json);
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, e.toString());
                    }
                    if(json != null) {
                        Map cacheContents = new Gson().fromJson(json, Map.class);
                        logger.info(String.valueOf(cacheContents.size()));
                        if(cacheContents.containsKey("displayName")) {
                            logger.info(cacheContents.get("displayName").toString());
                            windowDisplayTitle = cacheContents.get("displayName").toString();
                        }
                    }
                }
            }*/

            Property addCustomServerProp = config.get("Server", "AddDedicatedServer", false);
            addCustomServerProp.comment = "Set to true to have a dedicated server added to the server list ingame. The server will not overwrite others servers.";

            Property customServerNameProp = config.get("Server", "ServerName", "Localhost");
            customServerNameProp.comment = "The name of the dedicated server to add.";

            Property customServerIpProp = config.get("Server", "ServerIP", "127.0.0.1:25555");
            customServerIpProp.comment = "The ip of the dedicated server to add.";

            if(addCustomServerProp.getBoolean(false)) {
                ServerList serverList = new ServerList(Minecraft.getMinecraft());
                int c = serverList.countServers();
                boolean foundServer = false;
                for (int i = 0; i < c; i++) {
                    ServerData data = serverList.getServerData(i);

                    if (data.serverIP.equals(customServerIpProp.value)) {
                        foundServer = true;
                        break;
                    }
                }
                if (!foundServer) {
                    ServerData data = new ServerData(customServerNameProp.value, customServerIpProp.value);
                    serverList.addServerData(data);
                    serverList.saveServerList();
                }
            }

            config.save();
        } else {
            logger.info("Itlt initialized on server, as itlt is purely clientside we aren't doing much. ");
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

    //@Mod.EventHandler
    //public void loadComplete(FMLLoadCompleteEvent event) {
    @Mod.PostInit
    public void postInit(FMLPostInitializationEvent event) {
        if (proxy instanceof ClientProxy) {
            ClientProxy cp = (ClientProxy) proxy;
            if (makeScreenBigger && !ClientProxy.changed) cp.changeScreen();
            cp.setWindowDisplayTitle(windowDisplayTitle);
        }
    }

    private static boolean isJava64bit() {
        String[] astring = new String[]{"sun.arch.data.model", "com.ibm.vm.bitmode", "os.arch"};

        for(String s : astring) {
            String s1 = System.getProperty(s);
            if (s1 != null && s1.contains("64")) {
                return true;
            }
        }

        return false;
    }
}