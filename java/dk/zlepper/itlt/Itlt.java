package dk.zlepper.itlt;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import dk.zlepper.itlt.about.mod;
import dk.zlepper.itlt.proxies.ClientProxy;
import dk.zlepper.itlt.proxies.CommonProxy;
import dk.zlepper.itlt.threads.ShouterThread;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.logging.log4j.Logger;

@Mod(modid = mod.ID, version = mod.VERSION, name = mod.NAME)
public class Itlt
{
    @Mod.Instance("itlt")
    public static Itlt instance;

    @SidedProxy(clientSide = "dk.zlepper.itlt.proxies.ClientProxy")
    public static CommonProxy proxy;

    public static Logger logger;

    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        logger = event.getModLog();

        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();
        Property javaBitDetectionProp = config.get("BitDetection", "ShouldYellAt32BitUsers", false);
        javaBitDetectionProp.comment = "Set to true to make itlt yell at people attempting to use 32x java for the modpack.";
        String yelling = javaBitDetectionProp.getBoolean() ? "We are yelling at people" : "We are NOT yelling at people";
        logger.info(yelling);

        Property javaBitIssueMessageProp = config.get("BitDetection", "ErrorMessage", "You are using a 32 bit version of java. This is not recommended with this modpack.");
        javaBitIssueMessageProp.comment = "If ShouldYellAt32BitUsers is set to true, this is the message that will be displayed to the user.";

        if(javaBitDetectionProp.getBoolean(false)) {
            String bitVersion = System.getProperty("os.arch");
            if(bitVersion.equalsIgnoreCase("x86")) {
                ShouterThread st = new ShouterThread(javaBitIssueMessageProp.getString());
                st.start();
            }
        }

        config.save();
    }
}