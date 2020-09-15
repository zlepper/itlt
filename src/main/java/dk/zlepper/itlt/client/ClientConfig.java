package dk.zlepper.itlt.client;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.ForgeConfigSpec;

import java.io.File;
import java.nio.file.Path;

public final class ClientConfig {
    private static final ForgeConfigSpec.Builder clientConfigBuilder = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec clientConfig;

    public static ForgeConfigSpec.BooleanValue
            enable64bitRequirement,
            enable64bitWarning,
            enableCustom64bitJavaGuide,
            enableMinMemoryRequirement,
            enableMinMemoryWarning,
            enableMaxMemoryRequirement,
            enableMaxMemoryWarning,
            enableCustomMemoryAllocGuide,
            enableMinJavaVerRequirement,
            enableCustomWindowTitle,
            enableCustomIcon,
            enableAnticheat,
            enableAutoRemovalOfCheats;

    public static ForgeConfigSpec.ConfigValue<String>
            customWindowTitleText,
            custom64bitJavaGuideURL,
            customMemoryAllocGuideURL;

    public static ForgeConfigSpec.ConfigValue<Double>
            reqMinMemoryAmountInGB,
            reqMaxMemoryAmountInGB,
            warnMinMemoryAmountInGB,
            warnMaxMemoryAmountInGB;

    public static ForgeConfigSpec.ConfigValue<Integer>
            requiredMinJavaVerion;

    static {

        // Requirements section
        clientConfigBuilder.push("Requirements");//.comment("Requirements will prevent the modpack from launching if not met.");

            clientConfigBuilder.push("Java64bit");
                enable64bitRequirement = clientConfigBuilder
                        .comment("Whether or not to require 64bit Java to be able to launch the modpack.\r\n" +
                                "If this is enabled and someone tries to launch the modpack with 32bit Java, they'll get a message telling them how to upgrade and the modpack will close until they relaunch it with 64bit Java.\r\n" +
                                "Note: Enabling this overrides enable64bitWarning.")
                        .define("enable64bitRequirement", true);

                clientConfigBuilder.push("Guide");
                    enableCustom64bitJavaGuide = clientConfigBuilder
                            .comment("Enable this if you want to be able to change the link your users are sent to when they ask for instructions on how to get 64bit Java.\r\n" +
                                    "This is mainly useful for when you're using an unsupported version of this mod and the default guide is outdated.\r\n")
                            .define("enableCustom64bitJavaGuide", false);
                    custom64bitJavaGuideURL = clientConfigBuilder
                            .comment("The URL of the guide you want users to visit when they want 64bit Java.\r\n" +
                                    "Note: enableCustom64bitJavaGuide must be enabled for this to take effect.\r\n" +
                                    "Note: The URL must start with \"https://\" for security reasons.")
                            .define("custom64bitJavaGuideURL", "https://ozli.ga");
                clientConfigBuilder.pop();
            clientConfigBuilder.pop();

            clientConfigBuilder.push("Memory");
                clientConfigBuilder.push("Min");
                    // Todo: improve comments for memory-related requirements
                    enableMinMemoryRequirement = clientConfigBuilder
                            .comment("Enable this to require that at least X amount of RAM is available to the modpack for allocating.\r\n" +
                                    "This is useful if you have users complaining about \"OutOfMemory\" crashes.")
                            .define("enableMinMemoryRequirement", true);

                    reqMinMemoryAmountInGB = clientConfigBuilder
                            .comment("") // todo: comment
                            .define("reqMinMemoryAmountInGB", 0.5);
                clientConfigBuilder.pop();

                clientConfigBuilder.push("Max");
                    enableMaxMemoryRequirement = clientConfigBuilder
                            .comment("Enable this to require that no more than X amount of RAM is available to the modpack for allocating.\r\n" +
                                    "This is useful if for preventing users from allocating excessive amounts of RAM to the point of causing nasty GC-related lag spikes as a result.")
                            .define("enableMaxMemoryRequirement", true);

                    reqMaxMemoryAmountInGB = clientConfigBuilder
                            .comment("") // todo: comment
                            .define("reqMaxMemoryAmountInGB", 16.0);
                clientConfigBuilder.pop();

                clientConfigBuilder.push("Guide");
                    enableCustomMemoryAllocGuide = clientConfigBuilder
                            .comment("") // todo: comment
                            .define("enableCustomMemoryGuide", false);

                    customMemoryAllocGuideURL = clientConfigBuilder
                            .comment("") // todo: comment
                            .define("customMemoryAllocGuideURL", "https://ozli.ga");
                clientConfigBuilder.pop();
            clientConfigBuilder.pop();

            clientConfigBuilder.push("JavaVersion");
                enableMinJavaVerRequirement = clientConfigBuilder
                        .comment("") // todo: comment
                        .define("enableMinJavaVerRequirement", true);

                requiredMinJavaVerion = clientConfigBuilder
                        .comment("")
                        .define("requiredMinJavaVerion", 8);
            clientConfigBuilder.pop();

        clientConfigBuilder.pop();


        // Warnings section
        clientConfigBuilder.push("Warnings");//.comment("Warnings will let the user know when something is wrong but still allows the modpack to continue launching.");

            clientConfigBuilder.push("Java64bit");
                enable64bitWarning = clientConfigBuilder
                        .comment("Whether or not to warn when someone tries to launch the modpack with 32bit Java.\r\n" +
                                "If this is enabled and someone does that, they'll get a message telling them how to upgrade with the option to ask later and continue launching the modpack.")
                        .define("enable64bitWarning", true);
            clientConfigBuilder.pop();

            clientConfigBuilder.push("Memory");
                clientConfigBuilder.push("Min");
                    enableMinMemoryWarning = clientConfigBuilder
                            .comment("")
                            .define("enableMinMemoryWarning", true);

                    warnMinMemoryAmountInGB = clientConfigBuilder
                            .comment("") // todo: comment
                            .define("warnMinMemoryAmountInGB", 1.0);
                clientConfigBuilder.pop();

                clientConfigBuilder.push("Max");
                    enableMaxMemoryWarning = clientConfigBuilder
                            .comment("")
                            .define("enableMaxMemoryWarning", true);

                    warnMaxMemoryAmountInGB = clientConfigBuilder
                            .comment("") // todo: comment
                            .define("warnMaxMemoryAmountInGB", 14.0);
                clientConfigBuilder.pop();
            clientConfigBuilder.pop();

            // todo: config option of warning when more than (maxSysRAM - 1GB) is allocated to the game
            // todo: enableMinJavaVerWarning and warnMinJavaVersion config options
            // todo: ignoreMinJavaVerWarningOnTwitchLauncher (as it forces you to use at most Java 8)

        clientConfigBuilder.pop();


        // Display section
        clientConfigBuilder.push("Display");//.comment("Here you can change the aesthetics of your modpack.");

            clientConfigBuilder.push("WindowTitle");
                enableCustomWindowTitle = clientConfigBuilder
                        .comment("Enable this if you want to change the name of the Minecraft window.")
                        .define("enableCustomWindowTitle", false);

                customWindowTitleText = clientConfigBuilder
                        .comment("The name you want your Minecraft window to be.\r\n" +
                                "Note: enableCustomWindowTitle must be enabled for this to take effect.")
                        .define("customWindowTitleText", "Minecraft" + Minecraft.getInstance().getVersion());
            clientConfigBuilder.pop();

            clientConfigBuilder.push("Icon");
                enableCustomIcon = clientConfigBuilder
                        .comment("Enable this if you want to change the window icon of the Minecraft window.\r\n" +
                                "Note: The icon needs to be placed in config" + File.separator + "itlt" + File.separator + "icon.png and be no larger than 128px squared.\r\n" +
                                "Warning: Icon sizes beyond 128px squared can cause blurriness or even crashes on certain operating systems!")
                        .define("enableCustomIcon", false);
            clientConfigBuilder.pop();

            // todo: Technic launcher support (for both custom window title and custom icon) and relevant configs for turning that off if desired

        clientConfigBuilder.pop();


        // Server list section
        clientConfigBuilder.push("ServerList");

            // todo

        clientConfigBuilder.pop();


        // Anti-cheat section
        clientConfigBuilder.push("Anti-cheat");//.comment("No silver bullet, but definitely helps combat against some cheaters. Intended to compliment a full server-side anti-cheat mod/plugin.\r\n");

            enableAnticheat = clientConfigBuilder
                    .comment("Whether or not to detect and report known cheats to servers with itlt installed and anti-cheat enabled.\r\n" +
                            "Note: Disabling this won't suddenly allow you to cheat on said servers - it'll simply prevent you from joining them at all.\r\n" +
                            "Note: Depending on the server, you may be able to join it with cheats installed even if anti-cheat is enabled on the server. The action the server takes towards cheaters is down to the server's staff.")
                    .define("enableAnticheat", true);

            enableAutoRemovalOfCheats = clientConfigBuilder
                    .comment("Enable this if you want itlt to automatically delete known cheat mods so that they don't run on next launch.\r\n" +
                            "This feature is intended to prevent accidental cheating on servers. You'll probably want to disable this setting if you want to cheat on singleplayer.\r\n" +
                            "Note: enableAnticheat must be enabled for this to take effect.")
                    .define("enableAutoRemovalOfCheats", true);

        clientConfigBuilder.pop();


        // Build the config
        clientConfig = clientConfigBuilder.build();


        /*
        CLIENT_BUILDER.comment("Bit detection").push(CATEGORY_BIT_DETECTION);

        BIT_DETECTION_SHOULD_YELL_AT_32_BIT_USERS = CLIENT_BUILDER.comment("Set to true to make itlt yell at people attempting to use 32x java for the modpack.")
                .define("ShouldYellAt32BitUsers", true);
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
        */
    }


    public static void loadConfig(ForgeConfigSpec spec, final Path path) {

        final CommentedFileConfig configData = CommentedFileConfig.builder(path)
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();

        configData.load();
        spec.setConfig(configData);
    }
}
