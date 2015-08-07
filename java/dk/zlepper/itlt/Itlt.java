package dk.zlepper.itlt;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import dk.zlepper.itlt.about.mod;
import dk.zlepper.itlt.helpers.IconLoader;
import dk.zlepper.itlt.proxies.ClientProxy;
import dk.zlepper.itlt.proxies.CommonProxy;
import dk.zlepper.itlt.threads.ShouterThread;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

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

            Property windowDisplayTitleProp = config.get("Display", "windowDisplayTitle", "Minecraft 1.7.10");
            windowDisplayTitleProp.comment = "Change this value to change the name of the MineCraft window";
            windowDisplayTitle = windowDisplayTitleProp.getString();

            Property customIconProp = config.get("Display", "loadCustomIcon", true);
            customIconProp.comment = "Set to true to load a custom icon from config" + File.pathSeparator + "itlt" + File.pathSeparator + "icon.png";
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
                    if(di.mkdir()) {
                        logger.info("Made the directory for you. ");
                    }
                }
            }

            Property useTechnicIconProp = config.get("Display", "useTechnicIcon", true);
            useTechnicIconProp.comment = "Set to true to attempt to use the icon assigned to the modpack by the technic launcher.";
            if(useTechnicIconProp.getBoolean()) {
                Path currentRelativePath = Paths.get("").toAbsolutePath();
                logger.info(currentRelativePath.toString());
                String slugname = currentRelativePath.getFileName().toString();
                logger.info(slugname);

                // Should be the .technic directory
                Path technic = currentRelativePath.getParent().getParent();
                logger.info(technic.toAbsolutePath().toString());

                // Should be the asset directory for that modpack
                Path assets = Paths.get(technic.toAbsolutePath().toString() , "assets", "packs", slugname);
                logger.info(assets.toAbsolutePath().toString());

                File icon = Paths.get(assets.toAbsolutePath().toString(), "icon.png").toFile();
                logger.info(icon.exists() ? "Technic icon found" : "Technic icon NOT found. ");
                if(icon.exists() && !icon.isDirectory()) {
                    Display.setIcon(IconLoader.load(icon));
                }
            }

            config.save();
        }
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




















