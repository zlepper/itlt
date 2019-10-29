package dk.zlepper.itlt;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;

import java.io.File;
import java.nio.file.Path;

public class Config {

    public static final String CATEGORY_BIT_DETECTION = "BitDetection";
    public static final String CATEGORY_DISPLAY = "Display";
    public static final String CATEGORY_SERVER = "Server";

    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

    public static ForgeConfigSpec CLIENT_CONFIG;

    public static ForgeConfigSpec.BooleanValue BIT_DETECTION_SHOULD_YELL_AT_32_BIT_USERS;
    public static ForgeConfigSpec.ConfigValue<String> BIT_DETECTION_MESSAGE;

    public static ForgeConfigSpec.ConfigValue<String> DISPLAY_WINDOW_DISPLAY_TITLE;
    public static ForgeConfigSpec.BooleanValue DISPLAY_LOAD_CUSTOM_ICON;
    public static ForgeConfigSpec.BooleanValue DISPLAY_USE_TECHNIC_ICON;
    public static ForgeConfigSpec.BooleanValue DISPLAY_USE_TECHNIC_DISPLAY_NAME;


    public static ForgeConfigSpec.BooleanValue SERVER_ADD_DEDICATED_SERVER;
    public static ForgeConfigSpec.ConfigValue<String> SERVER_SERVER_NAME;
    public static ForgeConfigSpec.ConfigValue<String> SERVER_SERVER_IP;


    static {
        CLIENT_BUILDER.comment("Bit detection").push(CATEGORY_BIT_DETECTION);

        BIT_DETECTION_SHOULD_YELL_AT_32_BIT_USERS = CLIENT_BUILDER.comment("Set to true to make itlt yell at people attempting to use 32x java for the modpack.")
                .define("ShouldYellAt32BitUsers", false);
        BIT_DETECTION_MESSAGE = CLIENT_BUILDER.comment("If ShouldYellAt32BitUsers is set to true, this is the message that will be displayed to the user.")
                .define("Message", "You are using a 32 bit version of java. This is not recommended with this modpack.");

        CLIENT_BUILDER.pop();

        CLIENT_BUILDER.comment("Display").push(CATEGORY_DISPLAY);

        DISPLAY_WINDOW_DISPLAY_TITLE = CLIENT_BUILDER.comment("Change this value to change the name of the MineCraft window")
                .define("windowDisplayTitle", "Minecraft " + Minecraft.getInstance().getVersion());

        DISPLAY_LOAD_CUSTOM_ICON = CLIENT_BUILDER.comment("Set to true to load a custom icon from config" + File.separator + "itlt" + File.separator + "icon.png")
                .define("loadCustomIcon", true);

        DISPLAY_USE_TECHNIC_ICON = CLIENT_BUILDER.comment("Set to true to attempt to use the icon assigned to the modpack by the technic launcher. \nThis will take priority over loadCustomIcon")
                .define("useTechnicIcon", true);

        DISPLAY_USE_TECHNIC_DISPLAY_NAME = CLIENT_BUILDER.comment("Set to true to attempt to get the display name of the pack of the info json file \\nThis will take priority over windowDisplayTitle")
                .define("useTechnicDisplayName", true);

        CLIENT_BUILDER.pop();

        CLIENT_BUILDER.comment("Management of a dedicated server").push(CATEGORY_SERVER);

        SERVER_ADD_DEDICATED_SERVER = CLIENT_BUILDER.comment("Set to true to have a dedicated server added to the server list ingame. The server will not overwrite others servers.")
                .define("AddDedicatedServer", false);

        SERVER_SERVER_NAME = CLIENT_BUILDER.comment("The name of the dedicated server to add.")
                .define("ServerName", "localhost");

        SERVER_SERVER_IP = CLIENT_BUILDER.comment("The ip of the dedicated server to add.")
                .define("ServerIP", "127.0.0.1:25555");

        CLIENT_BUILDER.pop();

        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }


    public static void loadConfig(ForgeConfigSpec spec, Path path) {

        final CommentedFileConfig configData = CommentedFileConfig.builder(path)
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();

        configData.load();
        spec.setConfig(configData);
    }

    @SubscribeEvent
    public static void onLoad(final ModConfig.Loading configEvent) {

    }

    @SubscribeEvent
    public static void onReload(final ModConfig.ConfigReloading configEvent) {
    }
}
