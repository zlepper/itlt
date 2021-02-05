package dk.zlepper.itlt.client;

// todo: being able to imc to the itlt mod and get a crash report back if it was your fault

/* todo: change format of enableAppendingToCustomTitle from "packName (Minecraft* version)"
         to "Minecraft* version - packName" and account for Vanilla changing the title depending on the menu
         (e.g. "Minecraft* 1.15.2 (Multiplayer) - My Fancy Modpack v2.0").
         Doing this is in theory the best middle-ground approach. An option to change between the old and new
         appending formats is probably a good idea as well.
 */

// todo: investigate relaunching the game with modern Java when available on the system on launchers that force Java 8

// todo: launch game in fullscreen by default config option (while still respecting the player's choice if they change it)

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import dk.zlepper.itlt.common.ChecksumType;
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
            enableNearMaxMemoryWarning,
            enableCustomMemoryAllocGuide,
            enableMinJavaVerRequirement,
            enableMinJavaVerWarning,
            selectivelyIgnoreMinJavaVerWarning,
            enableCustomWindowTitle,
            enableAppendingToCustomTitle,
            enableUsingAutodetectedDisplayName,
            enableCustomIcon,
            enableUsingAutodetectedIcon,
            enableCustomServerListEntries,
            enableAnticheat,
            enableAutoRemovalOfCheats,
            enableExplicitGC,
            doExplicitGCOnPause;

    public static ForgeConfigSpec.ConfigValue<String>
            customWindowTitleText,
            custom64bitJavaGuideURL,
            customJavaUpgradeGuideURL,
            customMemoryAllocGuideURL;

    public static ForgeConfigSpec.ConfigValue<Double>
            reqMinMemoryAmountInGB,
            reqMaxMemoryAmountInGB,
            warnMinMemoryAmountInGB,
            warnMaxMemoryAmountInGB,
            warnNearMaxMemoryWarningInGB;

    public static ForgeConfigSpec.ConfigValue<Integer>
            requiredMinJavaVersion,
            warnMinJavaVersion,
            parallelModChecksThreshold;

    public static ForgeConfigSpec.ConfigValue<ChecksumType> preferredChecksumType;

    static {
        /* Forge's config system doesn't guarantee to preserve the order of options, hence the large use of grouping to
         * avoid confusion to the user.
         *
         * Yes, I'm aware I don't need the curly brackets around each config group, I do it so that devs looking at
         * this have the ability to collapse config groups to make things easier to read and understand.
         */

        // Java section
        clientConfigBuilder.push("Java"); {

            // Java.ExplicitGC
            clientConfigBuilder.push("ExplicitGC"); {

                enableExplicitGC = clientConfigBuilder
                        .comment("\r\nEnable this to allow itlt to explicitly request a garbage collection whenever " +
                                    "the user pauses the game or opens an screen with an opaque background " +
                                    "(e.g. the Resource Packs screen).",

                                "Doing this can help reduce memory usage in certain situations and also slightly " +
                                    "reduces the chances of a large GC happening in the middle of gameplay.",

                                "Note: This does not replace automatic GC, it simply requests a GC to run on certain " +
                                    "actions in addition to auto GC to help reduce the chances of a large GC sweep.",

                                "TL;DR: Turn this on to help prevent and/or reduce GC-related lag spikes, turn it " +
                                    "off to only rely on the pressure-based automatic GC (Vanilla behaviour).")
                        .define("enableExplicitGC", true);

                doExplicitGCOnPause = clientConfigBuilder
                        .comment("\r\nWhether or not to run explicit GC when the user pauses the game.",

                                "Mainly useful to turn off if you usually only have the game paused for a tiny " +
                                    "amount of time (e.g. less than ~2s).",

                                "Note: enableExplicitGC must be true for this to have any effect. " +
                                    "If enableExplicitGC is false, explicit GC from this mod won't happen regardless.")
                        .define("explicitGCOnPause", true);

            } clientConfigBuilder.pop();

            // Java.Arch
            clientConfigBuilder.push("Arch"); {

                // Java.Arch.Guide
                clientConfigBuilder.push("Guide"); {
                    enableCustom64bitJavaGuide = clientConfigBuilder
                            .comment("\r\nEnable this if you want to be able to change the link your users are sent " +
                                        "to when they ask for instructions on how to get 64bit Java.",

                                    "This is mainly useful for when you're using an unsupported version of this mod " +
                                        "and the default guide is outdated.")
                            .define("enableCustom64bitJavaGuide", false);
                    custom64bitJavaGuideURL = clientConfigBuilder
                            .comment("\r\nThe URL of the guide you want users to visit when they want 64bit Java.",
                                    "Note: enableCustom64bitJavaGuide must be enabled for this to take effect.",
                                    "Note: The URL must start with \"https://\" for security reasons.")
                            .define("custom64bitJavaGuideURL", "https://ozli.ga");
                } clientConfigBuilder.pop();

                // Java.Arch.Requirement
                clientConfigBuilder.push("Requirement"); {
                    enable64bitRequirement = clientConfigBuilder
                            .comment("\r\nWhether or not to require 64bit Java to be able to launch the modpack.",

                                    "If this is enabled and someone tries to launch the modpack with 32bit Java, " +
                                        "they'll get a message telling them how to upgrade and the modpack will " +
                                        "close until they relaunch it with 64bit Java.",

                                    "Note: Enabling this overrides enable64bitWarning.")
                            .define("enable64bitRequirement", true);
                } clientConfigBuilder.pop();

                // Java.Arch.Warning
                clientConfigBuilder.push("Warning"); {
                    enable64bitWarning = clientConfigBuilder
                            .comment("\r\nWhether or not to warn when someone tries to launch the modpack with 32bit Java.",

                                    "If this is enabled and someone does that, they'll get a message telling them " +
                                        "how to upgrade with the option to ask later and continue launching the modpack.")
                            .define("enable64bitWarning", true);
                } clientConfigBuilder.pop();

            } clientConfigBuilder.pop(); // end of Java.Arch

            // Java.Version
            clientConfigBuilder.push("Version"); {

                // Java.Version.Guide
                clientConfigBuilder.push("Guide"); {
                    enableCustomJavaUpgradeGuide = clientConfigBuilder
                            .comment("\r\nEnable this if you want to be able to change the link your users are sent " +
                                        "to when they ask for instructions on how to upgrade Java.",

                                    "This is mainly useful for when you're using an unsupported version of this mod " +
                                        "and the default guide is outdated.")
                            .define("enableCustomJavaUpgradeGuide", false);
                    customJavaUpgradeGuideURL = clientConfigBuilder
                            .comment("\r\nThe URL of the guide you want users to visit when they want 64bit Java.",
                                    "Note: enableCustomJavaUpgradeGuide must be enabled for this to take effect.",
                                    "Note: The URL must start with \"https://\" for security reasons.")
                            .define("customJavaUpgradeGuideURL", "https://ozli.ga");
                } clientConfigBuilder.pop();

                // Java.Version.Requirement
                clientConfigBuilder.push("Requirement"); {
                    enableMinJavaVerRequirement = clientConfigBuilder
                            .comment("\r\nWhether or not to require a certain version of Java to be able to launch the modpack.",

                                    "If someone tries to launch the modpack with a version of Java older than that " +
                                        "specified in requiredMinJavaVersion, they'll get a message telling them how " +
                                        "to upgrade and that the modpack will close until they relaunch it with more modern Java.",

                                    "Note: This is *separate* from enableMinJavaVerWarning - you can have a separate " +
                                        "version requirement and warning.")
                            .define("enableMinJavaVerRequirement", true);
                    requiredMinJavaVersion = clientConfigBuilder
                            .comment("\r\nThe minimum version of Java needed to be able to launch the modpack.",

                                    "Note: itlt handles Java version naming scheme differences for you, meaning you " +
                                        "can put \"7\" here and itlt will correctly check against \"Java 1.7\" internally, " +
                                        "while values such as \"15\" will check against \"Java 15\" internally.")
                            .defineInRange("requiredMinJavaVerion", 8, 6, 128);
                } clientConfigBuilder.pop();

                // Java.Version.Warning
                clientConfigBuilder.push("Warning"); {
                    enableMinJavaVerWarning = clientConfigBuilder
                            .comment("\r\nWhether or not to warn when someone tries to launch the modpack with " +
                                        "a version of Java older than that specified in warnMinJavaVersion.",

                                    "If this is enabled and someone does that, they'll get a message telling them " +
                                        "how to upgrade with the option to ask later and continue launching the modpack.",

                                    "Note: This is *separate* from enableMinJavaVerRequirement - you can have a " +
                                        "separate version requirement and warning.")
                            .define("enableMinJavaVerWarning", true);
                    warnMinJavaVersion = clientConfigBuilder
                            .comment("\r\nThe minimum recommended version of Java needed to skip the warning message " +
                                        "when launching the modpack.")
                            .defineInRange("warnMinJavaVersion", 8, 6, 128);
                    selectivelyIgnoreMinJavaVerWarning = clientConfigBuilder
                            .comment("\r\nSome launchers (such as Twitch/CurseForge launcher) do not allow the Java " +
                                        "version to be changed beyond Java 8.",

                                    "Enable this option to ignore the MinJavaVerWarning on launchers where the users " +
                                        "are unable to change the version of Java used to launch the game.")
                            .define("ignoreMinJavaVerWarningWhenVerForced", true);
                } clientConfigBuilder.pop();

            } clientConfigBuilder.pop(); // end of Java.Version

            // Java.Memory
            clientConfigBuilder.push("Memory"); {

                // Java.Memory.Guide
                clientConfigBuilder.push("Guide"); {
                    enableCustomMemoryAllocGuide = clientConfigBuilder
                            .comment("\r\nEnable this if you want to be able to change the link your users are sent to " +
                                        "when they ask for instructions on how to change their memory allocation settings.",

                                    "This is mainly useful for when you're using an unsupported version of this mod " +
                                        "and the default guide is outdated.")
                            .define("enableCustomMemoryGuide", false);
                    customMemoryAllocGuideURL = clientConfigBuilder
                            .comment("\r\nThe URL of the guide you want users to visit when they want to change their " +
                                        "memory allocation settings.",

                                    "Note: enableCustomJavaUpgradeGuide must be enabled for this to take effect",

                                    "Note: The URL must start with \"https://\" for security reasons.")
                            .define("customMemoryAllocGuideURL", "https://ozli.ga");
                } clientConfigBuilder.pop();

                // Java.Memory.Min
                clientConfigBuilder.push("Min"); {

                    // Java.Memory.Min.Requirement
                    clientConfigBuilder.push("Requirement"); {
                        enableMinMemoryRequirement = clientConfigBuilder
                                .comment("\r\nEnable this to require that at least X amount of RAM is available to " +
                                            "the modpack for allocating.",

                                        "This is useful if you have users complaining about \"OutOfMemory\" crashes.",

                                        "Note: This is *separate* from enableMinMemoryWarning - you can have a separate " +
                                            "min RAM allocation requirement and warning.")
                                .define("enableMinMemoryRequirement", true);
                        reqMinMemoryAmountInGB = clientConfigBuilder
                                .comment("\r\nThe minimum amount of allocated RAM in GB needed to be able to launch the modpack.")
                                .defineInRange("reqMinMemoryAmountInGB", 0.5, 0.1, 1024.0);
                    } clientConfigBuilder.pop();

                    // Java.Memory.Min.Warning
                    clientConfigBuilder.push("Warning"); {
                        enableMinMemoryWarning = clientConfigBuilder
                                .comment("\r\nEnable this to show a warning when less than X amount of RAM is " +
                                            "available to the modpack for allocating.",

                                        "Think of this like a recommended amount while the requirement is a minimum amount.",

                                        "Warning: Setting this too high could make it impossible for some of your users " +
                                            "to allocate the amount you're recommending and may actually hurt performance " +
                                            "(see the max memory section for details).",

                                        "Note: This is *separate* from enableMinMemoryRequirement - you can have a " +
                                            "separate min RAM allocation requirement and warning.")
                                .define("enableMinMemoryWarning", true);
                        warnMinMemoryAmountInGB = clientConfigBuilder
                                .comment("\r\nThe minimum recommended amount of allocated RAM in GB needed to skip " +
                                            "the warning message when launching the modpack.")
                                .defineInRange("warnMinMemoryAmountInGB", 1.0, 0.1, 1024.0);
                    } clientConfigBuilder.pop();

                } clientConfigBuilder.pop();

                // Java.Memory.Max
                clientConfigBuilder.push("Max"); {

                    // Java.Memory.Max.Requirement
                    clientConfigBuilder.push("Requirement"); {
                        enableMaxMemoryRequirement = clientConfigBuilder
                                .comment("\r\nEnable this to require that no more than X amount of RAM is available " +
                                            "to the modpack for allocating.",

                                        "This is useful for preventing users from allocating excessive amounts of " +
                                            "RAM to the point of causing nasty GC-related lag spikes as a result.")
                                .define("enableMaxMemoryRequirement", true);
                        reqMaxMemoryAmountInGB = clientConfigBuilder
                                .comment("\r\nThe maximum amount of allocated RAM in GB to be able to launch the modpack.")
                                .define("reqMaxMemoryAmountInGB", 16.0);
                    } clientConfigBuilder.pop();

                    // Java.Memory.Max.Warning
                    clientConfigBuilder.push("Warning"); {
                        enableMaxMemoryWarning = clientConfigBuilder
                                .comment("\r\nEnable this to show a warning when more than X amount of RAM is " +
                                            "available to the modpack for allocating.",

                                        "This is useful for warning users that are allocating excessive amounts of " +
                                            "RAM to the point of causing nasty GC-related lag spikes as a result.")
                                .define("enableMaxMemoryWarning", true);
                        warnMaxMemoryAmountInGB = clientConfigBuilder
                                .comment("\r\nThe maximum recommended amount of allocated RAM in GB needed to skip " +
                                            "the warning message when launching the modpack.")
                                .define("warnMaxMemoryAmountInGB", 14.0);
                    } clientConfigBuilder.pop();

                } clientConfigBuilder.pop();

                // Java.Memory.NearMax
                clientConfigBuilder.push("NearMax"); {

                    // Java.Memory.NearMax.Warning
                    clientConfigBuilder.push("Warning"); {
                        enableNearMaxMemoryWarning = clientConfigBuilder
                                .comment("\r\nEnable this to show a warning when not enough RAM is left over for " +
                                            "the OS and drivers to use.",

                                        "This is useful for warning users that are allocating so much RAM that there " +
                                            "isn't enough left over for other important processes to use without " +
                                            "hitting the swap, hurting performance as a result.")
                                .define("enableNearMaxMemoryWarning", true);
                        warnNearMaxMemoryWarningInGB = clientConfigBuilder
                                .comment("\r\nThe minimum recommended amount of memory left over after allocation in " +
                                            "GB needed to skip the warning message when launching the modpack.")
                                .defineInRange("warnNearMaxMemoryWarningInGB", 1.0, 0.1, 2.0);
                    }
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
                        .comment("\r\nThe name you want your Minecraft window to be.",

                                "Note: enableCustomWindowTitle must be enabled for this to take effect.")
                        .define("customWindowTitleText", "");
                enableAppendingToCustomTitle = clientConfigBuilder
                        .comment("\r\nEnable this if you want the game's version to be appended to the end of " +
                                    "your customWindowTitleText.",

                                "For example: \"ModpackName (Minecraft* 1.16.3)\")",
                                "Note: This is enabled by default because Mojang went out of their way to prevent " +
                                    "modders from changing the window title easily - this setting is a somewhat-happy " +
                                    "middle ground where both modpack authors' and Mojang's preferences are respected.",

                                "Note: I recommend leaving this enabled out of respect if you do enableCustomWindowTitle.")
                        .define("enableAppendingToCustomTitle", true);
                enableUsingAutodetectedDisplayName = clientConfigBuilder
                        .comment("\r\nWhether or not to automatically use your modpack's display name instead of the " +
                                    "customWindowTitleText when launching from a supported launcher.",

                                "Note: This will override the contents of customWindowTitleText when launching from " +
                                    "a supported launcher.",

                                "Note: enableCustomWindowTitle must be enabled for this to take effect.")
                        .define("enableUsingAutodetectedDisplayName", true);
            } clientConfigBuilder.pop();

            // Display.Icon
            clientConfigBuilder.push("Icon"); {
                enableCustomIcon = clientConfigBuilder
                        .comment("\r\nEnable this if you want to change the window icon of the Minecraft window.",
                                "Note: The icon needs to be placed in config" + File.separator + "itlt" + File.separator + "icon.png.",

                                "Note: For best results, use a square PNG with one of these sizes: 128x128, 96x96, " +
                                    "64x64, 48x48, 32x32, 24x24, 16x16.",

                                "Warning: Icon sizes beyond 128px squared or non-square icons may result in a poor " +
                                    "quality image on some operating systems as well as wasting storage space and bandwidth.")
                        .define("enableCustomIcon", false);
                enableUsingAutodetectedIcon = clientConfigBuilder
                        .comment("\r\nEnable this if you want itlt to automatically use your modpack's icon instead " +
                                    "of the icon.png when launching from a supported launcher.",

                                "Note: This will override the config" + File.separator + "itlt" + File.separator +
                                    "icon.png when launching from a supported launcher modpack.",

                                "Note: enableCustomIcon must be enabled for this to take effect.")
                        .define("enableUsingAutodetectedIcon", true); // Currently supported launchers: Technic, MultiMC.
            } clientConfigBuilder.pop();

        } clientConfigBuilder.pop(); // end of Display section

        // Server list section
        clientConfigBuilder.push("ServerList"); {
            enableCustomServerListEntries = clientConfigBuilder
                    .comment("") // todo
                    .define("enableCustomServerListEntries", false);
        } clientConfigBuilder.pop();

        // Anti-cheat section
        clientConfigBuilder.push("Anti-cheat"); { //.comment("No silver bullet, but definitely helps combat against some cheaters. Intended to compliment a full server-side anti-cheat mod/plugin.\r\n");
            enableAnticheat = clientConfigBuilder
                    .comment("\r\nWhether or not to detect and report known cheats to servers with itlt installed " +
                                "and anti-cheat enabled.",

                            "Note: Disabling this won't suddenly allow you to cheat on said servers - it'll simply " +
                                "prevent you from joining them at all.",

                            "Note: Depending on the server, you may be able to join it with cheats installed even if " +
                                "anti-cheat is enabled on the server. The action the server takes towards cheaters " +
                                "is down to the server's staff.")
                    .define("enableAnticheat", true);
            enableAutoRemovalOfCheats = clientConfigBuilder
                    .comment("\r\nEnable this if you want itlt to automatically delete known cheat mods so that they " +
                                "don't run on next launch.",

                            "This feature is intended to prevent accidental cheating on servers. " +
                                "You'll probably want to disable this setting if you want to cheat on singleplayer.",

                            "Note: enableAnticheat must be enabled for this to take effect.")
                    .define("enableAutoRemovalOfCheats", true);

            // Anti-cheat.Advanced
            clientConfigBuilder.push("Advanced"); {
                parallelModChecksThreshold = clientConfigBuilder
                        .comment("\r\nTo check if a known cheat mod is present, itlt needs to iterate through each " +
                                    "currently loaded mod.",

                                "These checks are pretty fast, but can still benefit from multithreading if there " +
                                    "are *a lot* of mods.",

                                "You can change the threshold for how many mods are needed before multithreading is " +
                                    "attempted here. The default is >100 mods.",

                                "Warning: There's an overhead associated with multithreading so having the threshold " +
                                    "too low can actually hurt performance.")
                        .defineInRange("parallelModChecksThreshold", 100, 1, 1024);
                preferredChecksumType = clientConfigBuilder
                        .comment("")
                        .defineEnum("preferredChecksumType", ChecksumType.Default);
            }
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
