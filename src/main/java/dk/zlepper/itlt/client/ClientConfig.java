package dk.zlepper.itlt.client;

// todo: config option of warning when more than (maxSysRAM - 1GB) is allocated to the game
// todo: being able to imc to the itlt mod and get a crash report back if it was your fault

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
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
            enableCustomJavaUpgradeGuide,
            enableMinMemoryRequirement,
            enableMinMemoryWarning,
            enableMaxMemoryRequirement,
            enableMaxMemoryWarning,
            enableCustomMemoryAllocGuide,
            enableMinJavaVerRequirement,
            enableMinJavaVerWarning,
            selectivelyIgnoreMinJavaVerWarning,
            enableCustomWindowTitle,
            enableAppendingToCustomTitle,
            enableUsingTechnicDisplayName,
            enableCustomIcon,
            enableUsingTechnicIcon,
            enableCustomServerListEntries,
            enableAnticheat,
            enableAutoRemovalOfCheats;

    public static ForgeConfigSpec.ConfigValue<String>
            customWindowTitleText,
            custom64bitJavaGuideURL,
            customJavaUpgradeGuideURL,
            customMemoryAllocGuideURL;

    public static ForgeConfigSpec.ConfigValue<Double>
            reqMinMemoryAmountInGB,
            reqMaxMemoryAmountInGB,
            warnMinMemoryAmountInGB,
            warnMaxMemoryAmountInGB;

    public static ForgeConfigSpec.ConfigValue<Integer>
            requiredMinJavaVersion,
            warnMinJavaVersion;

    static {
        // Forge's config system doesn't guarantee to preserve the order of options, hence the large use of grouping to avoid confusion to the user.
        // Yes, I'm aware I don't need the curly brackets around each config group, I do it so that devs looking at this have the ability to collapse config groups to make things easier to read and understand.

        // Java section
        clientConfigBuilder.push("Java"); {

            // Java.Arch
            clientConfigBuilder.push("Arch"); {

                // Java.Arch.Guide
                clientConfigBuilder.push("Guide"); {
                    enableCustom64bitJavaGuide = clientConfigBuilder
                            .comment("\r\nEnable this if you want to be able to change the link your users are sent to when they ask for instructions on how to get 64bit Java.\r\n" +
                                    "This is mainly useful for when you're using an unsupported version of this mod and the default guide is outdated.")
                            .define("enableCustom64bitJavaGuide", false);
                    custom64bitJavaGuideURL = clientConfigBuilder
                            .comment("\r\nThe URL of the guide you want users to visit when they want 64bit Java.\r\n" +
                                    "Note: enableCustom64bitJavaGuide must be enabled for this to take effect.\r\n" +
                                    "Note: The URL must start with \"https://\" for security reasons.")
                            .define("custom64bitJavaGuideURL", "https://ozli.ga");
                } clientConfigBuilder.pop();

                // Java.Arch.Requirement
                clientConfigBuilder.push("Requirement"); {
                    enable64bitRequirement = clientConfigBuilder
                            .comment("\r\nWhether or not to require 64bit Java to be able to launch the modpack.\r\n" +
                                    "If this is enabled and someone tries to launch the modpack with 32bit Java, they'll get a message telling them how to upgrade and the modpack will close until they relaunch it with 64bit Java.\r\n" +
                                    "Note: Enabling this overrides enable64bitWarning.")
                            .define("enable64bitRequirement", true);
                } clientConfigBuilder.pop();

                // Java.Arch.Warning
                clientConfigBuilder.push("Warning"); {
                    enable64bitWarning = clientConfigBuilder
                            .comment("\r\nWhether or not to warn when someone tries to launch the modpack with 32bit Java.\r\n" +
                                    "If this is enabled and someone does that, they'll get a message telling them how to upgrade with the option to ask later and continue launching the modpack.")
                            .define("enable64bitWarning", true);
                } clientConfigBuilder.pop();

            } clientConfigBuilder.pop(); // end of Java.Arch

            // Java.Version
            clientConfigBuilder.push("Version"); {

                // Java.Version.Guide
                clientConfigBuilder.push("Guide"); {
                    enableCustomJavaUpgradeGuide = clientConfigBuilder
                            .comment("\r\nEnable this if you want to be able to change the link your users are sent to when they ask for instructions on how to upgrade Java.\r\n" +
                                    "This is mainly useful for when you're using an unsupported version of this mod and the default guide is outdated.")
                            .define("enableCustomJavaUpgradeGuide", false);
                    customJavaUpgradeGuideURL = clientConfigBuilder
                            .comment("\r\nThe URL of the guide you want users to visit when they want 64bit Java.\r\n" +
                                    "Note: enableCustomJavaUpgradeGuide must be enabled for this to take effect.\r\n" +
                                    "Note: The URL must start with \"https://\" for security reasons.")
                            .define("customJavaUpgradeGuideURL", "https://ozli.ga");
                } clientConfigBuilder.pop();

                // Java.Version.Requirement
                clientConfigBuilder.push("Requirement"); {
                    enableMinJavaVerRequirement = clientConfigBuilder
                            .comment("\r\nWhether or not to require a certain version of Java to be able to launch the modpack.\r\n" +
                                    "If someone tries to launch the modpack with a version of Java older than that specified in requiredMinJavaVersion, they'll get a message telling them how to upgrade and that the modpack will close until they relaunch it with more modern Java.\r\n" +
                                    "Note: This is *separate* from enableMinJavaVerWarning - you can have a separate version requirement and warning.")
                            .define("enableMinJavaVerRequirement", true);
                    requiredMinJavaVersion = clientConfigBuilder
                            .comment("\r\nThe minimum version of Java needed to be able to launch the modpack.\r\n" +
                                    "Note: itlt handles Java version naming scheme differences for you, meaning you can put \"7\" here and itlt will correctly check against \"Java 1.7\" internally, while values such as \"15\" will check against \"Java 15\" internally.")
                            .define("requiredMinJavaVerion", 8);
                } clientConfigBuilder.pop();

                // Java.Version.Warning
                clientConfigBuilder.push("Warning"); {
                    enableMinJavaVerWarning = clientConfigBuilder
                            .comment("\r\nWhether or not to warn when someone tries to launch the modpack with a version of Java older than that specified in warnMinJavaVersion.\r\n" +
                                    "If this is enabled and someone does that, they'll get a message telling them how to upgrade with the option to ask later and continue launching the modpack.\r\n" +
                                    "Note: This is *separate* from enableMinJavaVerRequirement - you can have a separate version requirement and warning.")
                            .define("enableMinJavaVerWarning", true);
                    warnMinJavaVersion = clientConfigBuilder
                            .comment("\r\nThe minimum recommended version of Java needed to skip the warning message when launching the modpack.")
                            .define("warnMinJavaVersion", 8);
                    selectivelyIgnoreMinJavaVerWarning = clientConfigBuilder
                            .comment("\r\nSome launchers (such as Twitch/CurseForge launcher) do not allow the Java version to be changed beyond Java 8.\r\n" +
                                    "Enable this option to ignore the MinJavaVerWarning on launchers where the users are unable to change the version of Java used to launch the game.")
                            .define("ignoreMinJavaVerWarningWhenVerForced", true);
                } clientConfigBuilder.pop();

            } clientConfigBuilder.pop(); // end of Java.Version

            // Todo: improve comments for memory-related options
            // Java.Memory
            clientConfigBuilder.push("Memory"); {

                // Java.Memory.Guide
                clientConfigBuilder.push("Guide"); {
                    enableCustomMemoryAllocGuide = clientConfigBuilder
                            .comment("") // todo: comment
                            .define("enableCustomMemoryGuide", false);
                    customMemoryAllocGuideURL = clientConfigBuilder
                            .comment("") // todo: comment
                            .define("customMemoryAllocGuideURL", "https://ozli.ga");
                } clientConfigBuilder.pop();

                // Java.Memory.Min
                clientConfigBuilder.push("Min"); {

                    // Java.Memory.Min.Requirement
                    clientConfigBuilder.push("Requirement"); {
                        enableMinMemoryRequirement = clientConfigBuilder
                                .comment("\r\nEnable this to require that at least X amount of RAM is available to the modpack for allocating.\r\n" +
                                        "This is useful if you have users complaining about \"OutOfMemory\" crashes.\r\n" +
                                        "Note: This is *separate* from enableMinMemoryWarning - you can have a separate min RAM allocation requirement and warning.")
                                .define("enableMinMemoryRequirement", true);
                        reqMinMemoryAmountInGB = clientConfigBuilder
                                .comment("\r\nThe minimum amount of allocated RAM in GB needed to be able to launch the modpack.")
                                .define("reqMinMemoryAmountInGB", 0.5);
                    } clientConfigBuilder.pop();

                    // Java.Memory.Min.Warning
                    clientConfigBuilder.push("Warning"); {
                        enableMinMemoryWarning = clientConfigBuilder
                                .comment("\r\nEnable this to show a warning when less than X amount of RAM is available to the modpack for allocating.\r\n" +
                                        "Think of this like a recommended amount while the requirement is a minimum amount.\r\n" +
                                        "Warning: Setting this too high could make it impossible for some of your users to allocate the amount you're recommending and may actually hurt performance (see the max memory section for details).\r\n" +
                                        "Note: This is *separate* from enableMinMemoryRequirement - you can have a separate min RAM allocation requirement and warning.")
                                .define("enableMinMemoryWarning", true);
                        warnMinMemoryAmountInGB = clientConfigBuilder
                                .comment("\r\nThe minimum recommended amount of allocated RAM in GB needed to skip the warning message when launching the modpack.")
                                .define("warnMinMemoryAmountInGB", 1.0);
                    } clientConfigBuilder.pop();

                } clientConfigBuilder.pop();

                // Java.Memory.Max
                clientConfigBuilder.push("Max"); {

                    // Java.Memory.Max.Requirement
                    clientConfigBuilder.push("Requirement"); {
                        enableMaxMemoryRequirement = clientConfigBuilder
                                .comment("\r\nEnable this to require that no more than X amount of RAM is available to the modpack for allocating.\r\n" +
                                        "This is useful for preventing users from allocating excessive amounts of RAM to the point of causing nasty GC-related lag spikes as a result.")
                                .define("enableMaxMemoryRequirement", true);
                        reqMaxMemoryAmountInGB = clientConfigBuilder
                                .comment("") // todo: comment
                                .define("reqMaxMemoryAmountInGB", 16.0);
                    } clientConfigBuilder.pop();

                    // Java.Memory.Max.Warning
                    clientConfigBuilder.push("Warning"); {
                        enableMaxMemoryWarning = clientConfigBuilder
                                .comment("")
                                .define("enableMaxMemoryWarning", true);
                        warnMaxMemoryAmountInGB = clientConfigBuilder
                                .comment("") // todo: comment
                                .define("warnMaxMemoryAmountInGB", 14.0);
                    } clientConfigBuilder.pop();

                } clientConfigBuilder.pop();

            } clientConfigBuilder.pop(); // end of Java.Memory

        } clientConfigBuilder.pop(); // end of Java section

        // Display section
        clientConfigBuilder.push("Display"); {//.comment("Here you can change the aesthetics of your modpack.");

            // Display.WindowTitle
            clientConfigBuilder.push("WindowTitle"); {
                enableCustomWindowTitle = clientConfigBuilder
                        .comment("\r\nEnable this if you want to change the name of the Minecraft window.")
                        .define("enableCustomWindowTitle", true);
                customWindowTitleText = clientConfigBuilder
                        .comment("\r\nThe name you want your Minecraft window to be.\r\n" +
                                "Note: enableCustomWindowTitle must be enabled for this to take effect.")
                        .define("customWindowTitleText", "");
                enableAppendingToCustomTitle = clientConfigBuilder
                        .comment("\r\nEnable this if you want the game's version to be appended to the end of your customWindowTitleText.\r\n" +
                                "For example: \"ModpackName (Minecraft* 1.16.3)\")\r\n" +
                                "Note: This is enabled by default because Mojang went out of their way to prevent modders from changing the window title easily - this setting is a happy middle ground where both modpack authors' and Mojang's preferences are respected.")
                        .define("enableAppendingToCustomTitle", true);
                enableUsingTechnicDisplayName = clientConfigBuilder
                        .comment("\r\nWhether or not to automatically use your modpack's display name instead of the customWindowTitleText when launching from the Technic Launcher.\r\n" +
                                "Note: This will override the contents of customWindowTitleText when launching from a Technic Platform modpack.\r\n" +
                                "Note: enableCustomWindowTitle must be enabled for this to take effect.")
                        .define("enableUsingTechnicDisplayName", true);
            } clientConfigBuilder.pop();

            // Display.Icon
            clientConfigBuilder.push("Icon"); {
                enableCustomIcon = clientConfigBuilder
                        .comment("\r\nEnable this if you want to change the window icon of the Minecraft window.\r\n" +
                                "Note: The icon needs to be placed in config" + File.separator + "itlt" + File.separator + "icon.png and be no larger than 128px squared.\r\n" +
                                "Warning: Icon sizes beyond 128px squared can cause blurriness or even crashes on certain operating systems!")
                        .define("enableCustomIcon", false);
                enableUsingTechnicIcon = clientConfigBuilder
                        .comment("\r\nEnable this if you want itlt to automatically use your modpack's icon instead of the icon.png when launching from the Technic Launcher.\r\n" +
                                "Note: This will override the icon.png when launching from a Technic Platform modpack.\r\n" +
                                "Note: enableCustomIcon must be enabled for this to take effect.")
                        .define("enableUsingTechnicIcon", true);
            } clientConfigBuilder.pop();

        } clientConfigBuilder.pop(); // end of Display section

        // Server list section
        clientConfigBuilder.push("ServerList"); {
            enableCustomServerListEntries = clientConfigBuilder
                    .comment("") // todo
                    .define("enableCustomServerListEntries", false);
        } clientConfigBuilder.pop();

        // Anticheat section
        clientConfigBuilder.push("Anticheat"); { //.comment("No silver bullet, but definitely helps combat against some cheaters. Intended to compliment a full server-side anti-cheat mod/plugin.\r\n");
            enableAnticheat = clientConfigBuilder
                    .comment("\r\nWhether or not to detect and report known cheats to servers with itlt installed and anti-cheat enabled.\r\n" +
                            "Note: Disabling this won't suddenly allow you to cheat on said servers - it'll simply prevent you from joining them at all.\r\n" +
                            "Note: Depending on the server, you may be able to join it with cheats installed even if anti-cheat is enabled on the server. The action the server takes towards cheaters is down to the server's staff.")
                    .define("enableAnticheat", true);
            enableAutoRemovalOfCheats = clientConfigBuilder
                    .comment("\r\nEnable this if you want itlt to automatically delete known cheat mods so that they don't run on next launch.\r\n" +
                            "This feature is intended to prevent accidental cheating on servers. You'll probably want to disable this setting if you want to cheat on singleplayer.\r\n" +
                            "Note: enableAnticheat must be enabled for this to take effect.")
                    .define("enableAutoRemovalOfCheats", true);
        } clientConfigBuilder.pop();


        // Build the config
        clientConfig = clientConfigBuilder.build();
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
