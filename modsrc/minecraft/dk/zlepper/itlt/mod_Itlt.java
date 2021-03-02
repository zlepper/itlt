package dk.zlepper.itlt;

//import com.google.gson.Gson;
import net.minecraft.src.BaseMod;
import dk.zlepper.itlt.about.mod;
import dk.zlepper.itlt.helpers.IconLoader;
import dk.zlepper.itlt.proxies.ClientProxy;
import dk.zlepper.itlt.threads.ShouterThread;
import net.minecraft.client.Minecraft;
/*import net.minecraft.src.ServerData;
import net.minecraft.src.ServerList;*/
import net.minecraft.src.forge.Configuration;
import net.minecraft.src.forge.NetworkMod;
import net.minecraft.src.forge.Property;
//import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.Display;

import cpw.mods.fml.common.Mod;

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

//@Mod(version = mod.VERSION, name = mod.NAME)
public class mod_Itlt extends NetworkMod {
	
	public static mod_Itlt instance;
	
	@Override
	public boolean clientSideRequired() {
		return true;
	}
	
	@Override
	public boolean serverSideRequired() {
		return false;
	}
	
	@Override
	public String getVersion() {
		return mod.VERSION;
	}
	
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

    /*@Mod.PreInit
    public void load() {
        
    }*/

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
    public void modsLoaded() {
    	super.modsLoaded();
    	
    	logger = getModLog();

        Configuration config = new Configuration(getSuggestedConfigurationFile());
        config.load();
        Property javaBitDetectionProp = config.getOrCreateBooleanProperty("BitDetection", "ShouldYellAt32BitUsers", false);
        javaBitDetectionProp.comment = "Set to true to make itlt yell at people attempting to use 32x java for the modpack.";
        String yelling = javaBitDetectionProp.getBoolean(false) ? "We are yelling at people" : "We are NOT yelling at people";
        logger.info(yelling);

        Property javaBitIssueMessageProp = config.getOrCreateProperty("BitDetection", "ErrorMessage", "You are using a 32 bit version of java. This is not recommended with this modpack.");
        javaBitIssueMessageProp.comment = "If ShouldYellAt32BitUsers is set to true, this is the message that will be displayed to the user.";

        if (javaBitDetectionProp.getBoolean(false)) {
            if (!isJava64bit()) {
                ShouterThread st = new ShouterThread(javaBitIssueMessageProp.value);
                st.start();
            }
        }

        Property shouldMaximizeDisplayProp = config.getOrCreateBooleanProperty("Display", "ShouldMaximizeDisplay", false);
        shouldMaximizeDisplayProp.comment = "Set to true to make minecraft attempt to maximize itself on startup (This is kinda unstable right now, so don't trust it too much)";
        makeScreenBigger = shouldMaximizeDisplayProp.getBoolean(false);

        // todo
        /*Property addCustomServerProp = config.getOrCreateBooleanProperty("Server", "AddDedicatedServer", false);
        addCustomServerProp.comment = "Set to true to have a dedicated server added to the server list ingame. The server will not overwrite others servers.";

        Property customServerNameProp = config.getOrCreateProperty("Server", "ServerName", "Localhost");
        customServerNameProp.comment = "The name of the dedicated server to add.";

        Property customServerIpProp = config.getOrCreateProperty("Server", "ServerIP", "127.0.0.1:25555");
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
        }*/

        config.save();
    	
        if (makeScreenBigger) ClientProxy.changeScreen();
        ClientProxy.setWindowDisplayTitle(windowDisplayTitle);
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

    private static Logger getModLog() {
        return Logger.getLogger(mod.ID);
    }
    
    private static File getSuggestedConfigurationFile() {
    	return new File("config/", "itlt.cfg");
    }

    @Override
	public void load() {
		instance = this;
	}
}