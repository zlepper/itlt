package dk.zlepper.itlt.client;

// todo: being able to imc to the itlt mod and get a crash report back if it was your fault

/* todo: change format of enableAppendingToCustomTitle from "packName (Minecraft* version)"
         to "Minecraft* version - packName" and account for Vanilla changing the title depending on the menu
         (e.g. "Minecraft* 1.15.2 (Multiplayer) - My Fancy Modpack v2.0").
         Doing this is in theory the best middle-ground approach. An option to change between the old and new
         appending formats is probably a good idea as well.
 */

// todo: investigate relaunching the game with modern Java when available on the system on launchers that force Java 8
// todo progress: proof-of-concept done with Trailblaze, determined it's too much work for now

// todo: launch game in fullscreen by default config option (while still respecting the player's choice if they change it)

// todo: account for modpack authors setting the max lower than the min and enabling both, making it impossible to satisfy
//       a want or need. When this happens, show a tailored error message for this specific scenario or change the
//       behaviour so that the max warn/need gets disabled when the min is higher (with an updated config comments to note this)

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import dk.zlepper.itlt.common.ChecksumType;
import dk.zlepper.itlt.itlt;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ClientConfig {
    private static final ForgeConfigSpec.Builder clientConfigBuilder = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec clientConfig;

    public static ForgeConfigSpec.BooleanValue
            enable64bitRequirement,
            enable64bitWarning,
            enableCustom64bitJavaGuide,
            enableCustomJavaUpgradeGuide,
            enableCustomJavaDowngradeGuide,
            enableMinMemoryRequirement,
            enableMinMemoryWarning,
            enableMaxMemoryRequirement,
            enableMaxMemoryWarning,
            enableNearMaxMemoryWarning,
            enableCustomMemoryAllocGuide,
            enableMinJavaVerRequirement,
            enableMinJavaVerWarning,
            selectivelyIgnoreMinJavaVerWarning,
            enableMaxJavaVerRequirement,
            enableMaxJavaVerWarning,
            selectivelyIgnoreMaxJavaVerWarning,
            enableCustomWindowTitle,
            enableAppendingToCustomTitle,
            enableUsingAutodetectedDisplayName,
            enableCustomIcon,
            enableUsingAutodetectedIcon,
            enableCustomServerListEntries,
            enableAnticheat,
            enableAutoRemovalOfCheats,
            enableExplicitGC,
            doExplicitGCOnPause,
            doExplicitGCOnSleep,
            doExplicitGCOnMenu;

    public static ForgeConfigSpec.ConfigValue<String>
            customWindowTitleText,
            custom64bitJavaGuideURL,
            customJavaUpgradeGuideURL,
            customJavaDowngradeGuideURL,
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
            requiredMaxJavaVersion,
            warnMaxJavaVersion,
            parallelModChecksThreshold;

    public static ForgeConfigSpec.ConfigValue<ChecksumType> preferredChecksumType;

    /** Simplify floats to ints when they represent the same value (e.g. show "1" instead of "1.0") **/
    public static String getSimplifiedDoubleStr(final double doubleNum) {
        if (doubleNum == (int) doubleNum)
            return String.valueOf((int) doubleNum);
        else return String.valueOf(doubleNum);
    }

    /** Returns the path as a File if it already exists or has been successfully created. Returns null otherwise **/
    @Nullable
    public static File makeItltFolderIfNeeded() {
        final File itltDir = Paths.get(FMLPaths.CONFIGDIR.get().toAbsolutePath().toString(), "itlt").toFile();
        if (!itltDir.exists() && (enableCustomIcon.get() || areAnyWarningsEnabled()) && !itltDir.mkdir()) {
            itlt.LOGGER.warn("Unable to make an \"itlt\" folder inside the config folder. Please make it manually.");
            return null;
        }
        return itltDir;
    }

    public static boolean areAnyWarningsEnabled() {
        return enableMinMemoryWarning.get() || enableMaxMemoryWarning.get() ||
                enableMinJavaVerWarning.get() || enableMaxJavaVerWarning.get() || enable64bitWarning.get();
    }

    static {
        /* Forge's config system doesn't guarantee to preserve the order of options, hence the large use of grouping to
         * avoid confusion to the user.
         *
         * Yes, I'm aware I don't need the curly brackets around each config group, I do it so that devs looking at
         * this have the ability to collapse config groups to make things easier to read and understand.
         */

        // Java section
        clientConfigBuilder.push("Java"); {

            // Java.Advanced
            clientConfigBuilder.push("Advanced"); {

                // Java.Advanced.ExplicitGC
                clientConfigBuilder.push("ExplicitGC"); {

                    enableExplicitGC = clientConfigBuilder
                            .comment(" ",
                                    " Enable this to allow itlt to explicitly request a garbage collection whenever the user",
                                    " pauses the game or opens an screen with an opaque background while keeping auto GC.",
                                    " (e.g. the Resource Packs screen)",
                                    " ",
                                    " Doing this can help reduce memory usage in certain situations and also slightly",
                                    " reduces the chances of a large GC happening in the middle of early gameplay.",
                                    " ",
                                    " Note: For best performance with this option, include -XX:+AlwaysPreTouch in your",
                                    " JVM args and have Xms and Xmx be the same value. Omit the AlwaysPreTouch arg for",
                                    " lower physical memory usage.",
                                    " ",
                                    " Warning: This option has no effect if the -XX:+DisableExplicitGC JVM arg is present.",
                                    " ",
                                    " Turn this on to help prevent and/or reduce GC-related lag spikes, turn it off to only",
                                    " rely on the pressure-based automatic GC (Vanilla behaviour).")
                            .define("enableExplicitGC", false);

                    doExplicitGCOnPause = clientConfigBuilder
                            .comment(" " ,
                                    " Whether or not to run explicit GC when the player pauses the game.",
                                    " ",
                                    " Mainly useful to turn off if you usually only have the game paused for a tiny amount ",
                                    " of time (e.g. less than ~2s).",
                                    " ",
                                    " Note: enableExplicitGC must be true for this to have any effect.")
                            .define("explicitGCOnPause", true);

                    doExplicitGCOnSleep = clientConfigBuilder
                            .comment(" ",
                                    " Whether or not to run explicit GC when the player is sleeping in a bed.",
                                    " ",
                                    " Note: enableExplicitGC must be true for this to have any effect.")
                            .define("explicitGCOnSleep", true);

                    doExplicitGCOnMenu = clientConfigBuilder
                            .comment(" ",
                                    " Whether or not to run explicit GC when navigating one of the following opaque",
                                    " background screens: Singleplayer world selection, Multiplayer server selection,",
                                    " Resource Pack selection, Language selection, Chat options, controls options,",
                                    " accessibility options, realms main screen and stats menu.",
                                    " ",
                                    " Mainly useful to disable for speedruns that start the timer when the main menu is shown.",
                                    " ",
                                    " Note: enableExplicitGC must be true for this to have any effect.")
                            .define("explicitGCOnMenu", true);

                } clientConfigBuilder.pop();

            } clientConfigBuilder.pop();

            // Java.Arch
            clientConfigBuilder.push("Arch"); {

                // Java.Arch.Guide
                clientConfigBuilder.push("Guide"); {
                    enableCustom64bitJavaGuide = clientConfigBuilder
                            .comment(" " ,
                                    " Enable this if you want to be able to change the link your users are sent to when",
                                    " they ask for instructions on how to get 64bit Java.",
                                    " ",
                                    " This is mainly useful for when you're using an unsupported version of this mod and",
                                    " the default guide's outdated or 404s.")
                            .define("enableCustom64bitJavaGuide", false);
                    custom64bitJavaGuideURL = clientConfigBuilder
                            .comment(" ",
                                    " The URL of the guide you want users to visit when they want 64bit Java.",
                                    " Note: enableCustom64bitJavaGuide must be enabled for this to take effect.",
                                    " Note: The URL must start with \"https://\" for security reasons.")
                            .define("custom64bitJavaGuideURL", "https://ozli.ga");
                } clientConfigBuilder.pop();

                // Java.Arch.Requirement
                clientConfigBuilder.push("Requirement"); {
                    enable64bitRequirement = clientConfigBuilder
                            .comment(" ",
                                    " Whether or not to require 64bit Java to be able to launch the modpack.",
                                    " ",
                                    " If this is enabled and someone tries to launch the modpack with 32bit Java, they'll",
                                    " get a message telling them how to upgrade and the modpack will close until they",
                                    " relaunch it with 64bit Java.",
                                    " ",
                                    " Note: Enabling this overrides enable64bitWarning.")
                            .define("enable64bitRequirement", true);
                } clientConfigBuilder.pop();

                // Java.Arch.Warning
                clientConfigBuilder.push("Warning"); {
                    enable64bitWarning = clientConfigBuilder
                            .comment(" ",
                                    " Whether or not to warn when someone tries to launch the modpack with 32bit Java.",
                                    " ",
                                    " If this is enabled and someone tries to launch the modpack with 32bit Java, they'll",
                                    " get a message asking them to upgrade with instructions and the option to ask later",
                                    " and continue launching the modpack.")
                            .define("enable64bitWarning", true);
                } clientConfigBuilder.pop();

            } clientConfigBuilder.pop(); // end of Java.Arch

            // Java.Version
            clientConfigBuilder.push("Version"); {

                // Java.Version.Min
                clientConfigBuilder.push("Min"); {

                    // Java.Version.Min.Guide
                    clientConfigBuilder.push("Guide"); {
                        enableCustomJavaUpgradeGuide = clientConfigBuilder
                                .comment(" ",
                                        " Enable this if you want to be able to change the link your users are sent to when",
                                        " they ask for instructions on how to upgrade Java.",
                                        " ",
                                        " This is mainly useful for when you're using an unsupported version of this mod",
                                        " and the default guide is outdated.")
                                .define("enableCustomJavaUpgradeGuide", false);
                        customJavaUpgradeGuideURL = clientConfigBuilder
                                .comment(" ",
                                        " The URL of the guide you want users to visit when they want to upgrade Java.",
                                        " Note: enableCustomJavaUpgradeGuide must be enabled for this to take effect.",
                                        " Note: The URL must start with \"https://\" for security reasons.")
                                .define("customJavaUpgradeGuideURL", "https://ozli.ga");
                    } clientConfigBuilder.pop();

                    // Java.Version.Min.Requirement
                    clientConfigBuilder.push("Requirement"); {
                        enableMinJavaVerRequirement = clientConfigBuilder
                                .comment(" ",
                                        " Whether or not to require a certain version of Java to be able to launch the modpack.",
                                        " ",
                                        " If someone tries to launch the modpack with a version of Java older than what's",
                                        " specified in requiredMinJavaVersion, they'll get a message telling them how to",
                                        " upgrade and that the modpack will close until they relaunch it with more modern Java.",
                                        " ",
                                        " Note: This is *separate* from enableMinJavaVerWarning - you can have a separate",
                                        " version requirement and warning.")
                                .define("enableMinJavaVerRequirement", true);
                        requiredMinJavaVersion = clientConfigBuilder
                                .comment(" ",
                                        " The minimum version of Java needed to be able to launch the modpack.",
                                        " ",
                                        " Note: itlt handles Java version naming scheme differences for you, meaning you can",
                                        " put \"7\" here and itlt will correctly check against \"Java 1.7\" internally,",
                                        " while values such as \"15\" will check against \"Java 15\" internally.")
                                .defineInRange("requiredMinJavaVerion", 8, 6, 127);
                    } clientConfigBuilder.pop();

                    // Java.Version.Min.Warning
                    clientConfigBuilder.push("Warning"); {
                        enableMinJavaVerWarning = clientConfigBuilder
                                .comment(" ",
                                        " Whether or not to warn when someone tries to launch the modpack with a version",
                                        " of Java older than that specified in warnMinJavaVersion.",
                                        " ",
                                        " If this is enabled and someone does that, they'll get a message telling them how",
                                        " to upgrade with the option to ask later and continue launching the modpack.",
                                        " ",
                                        " Note: This is *separate* from enableMinJavaVerRequirement - you can have a",
                                        " separate version requirement and warning.")
                                .define("enableMinJavaVerWarning", true);
                        warnMinJavaVersion = clientConfigBuilder
                                .comment(" ",
                                        " The minimum recommended version of Java needed to skip the warning message when",
                                        " launching the modpack.")
                                .defineInRange("warnMinJavaVersion", 8, 6, 127);
                        selectivelyIgnoreMinJavaVerWarning = clientConfigBuilder
                                .comment(" ",
                                        " Some launchers (such as Twitch/CurseForge launcher) do not allow the Java version",
                                        " to be changed beyond Java 8.",
                                        " ",
                                        " Enable this option to ignore the MinJavaVerWarning on launchers where the users",
                                        " are unable to change the version of Java used to launch the game.")
                                .define("ignoreMinJavaVerWarningWhenVerForced", true);
                    } clientConfigBuilder.pop();

                } clientConfigBuilder.pop();

                // Java.Version.Max
                clientConfigBuilder.push("Max"); {

                    // Java.Version.Max.Guide
                    clientConfigBuilder.push("Guide"); {
                        enableCustomJavaDowngradeGuide = clientConfigBuilder
                                .comment(" ",
                                        " Enable this if you want to be able to change the link your users are sent to when",
                                        " they ask for instructions on how to downgrade Java.",
                                        " ",
                                        " Note: I recommend stating in your guide why you want your users to use an older",
                                        " version of Java than what Forge supports (Java 15 works in Forge 1.16.5 at the",
                                        " time of writing). You should ideally be using the latest supported version of",
                                        " Java if it works with your mods.",
                                        " ",
                                        " This is mainly useful for when you're using an unsupported version of this mod",
                                        " and the default guide is outdated.")
                                .define("enableCustomJavaDowngradeGuide", false);
                        customJavaDowngradeGuideURL = clientConfigBuilder
                                .comment(" ",
                                        " The URL of the guide you want users to visit when they want 64bit Java.",
                                        " Note: enableCustomJavaDowngradeGuide must be enabled for this to take effect.",
                                        " Note: The URL must start with \"https://\" for security reasons.")
                                .define("customJavaDowngradeGuideURL", "https://ozli.ga");
                    } clientConfigBuilder.pop();

                    // Java.Version.Max.Requirement
                    clientConfigBuilder.push("Requirement"); {
                        enableMaxJavaVerRequirement = clientConfigBuilder
                                .comment(" ",
                                        " Whether or not to require a certain version of Java to be able to launch the modpack.",
                                        " ",
                                        " If someone tries to launch the modpack with a version of Java newer than what's",
                                        " specified in requiredMaxJavaVersion, they'll get a message telling them how to",
                                        " downgrade and that the modpack will close until they relaunch it with older Java.",
                                        " ",
                                        " Note: If your version of Forge doesn't support the max version of Java you're",
                                        " trying to prevent, this mod won't be able to kick into action and show the message",
                                        " to users.",
                                        " ",
                                        " Note: This is *separate* from enableMaxJavaVerWarning - you can have a separate ",
                                        " version requirement and warning.")
                                .define("enableMaxJavaVerRequirement", false);
                        requiredMaxJavaVersion = clientConfigBuilder
                                .comment(" ",
                                        " The maximum version of Java needed to be able to launch the modpack.",
                                        " ",
                                        " Note: itlt handles Java version naming scheme differences for you, meaning you can",
                                        " put \"7\" here and itlt will correctly check against \"Java 1.7\" internally,",
                                        " while values such as \"15\" will check against \"Java 15\" internally.")
                                .defineInRange("requiredMaxJavaVerion", 15, 6, 127);
                    } clientConfigBuilder.pop();

                    // Java.Version.Max.Warning
                    clientConfigBuilder.push("Warning"); {
                        enableMaxJavaVerWarning = clientConfigBuilder
                                .comment(" ",
                                        " Whether or not to warn when someone tries to launch the modpack with a version",
                                        " of Java newer than that specified in warnMaxJavaVersion.",
                                        " ",
                                        " If this is enabled and someone does that, they'll get a message telling them how",
                                        " to downgrade with the option to ask later and continue launching the modpack.",
                                        " ",
                                        " Note: This is *separate* from enableMaxJavaVerRequirement - you can have a",
                                        " separate version requirement and warning.")
                                .define("enableMaxJavaVerWarning", false);
                        warnMaxJavaVersion = clientConfigBuilder
                                .comment(" ",
                                        " The minimum recommended version of Java needed to skip the warning message when",
                                        " launching the modpack.")
                                .defineInRange("warnMaxJavaVersion", 15, 6, 127);
                        selectivelyIgnoreMaxJavaVerWarning = clientConfigBuilder
                                .comment(" ",
                                        " Some launchers (such as Twitch/CurseForge launcher) do not allow the Java version",
                                        " to be changed from Java 8.",
                                        " ",
                                        " Enable this option to ignore the MaxJavaVerWarning on launchers where the users",
                                        " are unable to change the version of Java used to launch the game.")
                                .define("ignoreMaxJavaVerWarningWhenVerForced", true);
                    } clientConfigBuilder.pop();

                } clientConfigBuilder.pop(); // end of Java.Version.Max

            } clientConfigBuilder.pop(); // end of Java.Version

            // Java.Memory
            clientConfigBuilder.push("Memory"); {

                // Java.Memory.Guide
                clientConfigBuilder.push("Guide"); {
                    enableCustomMemoryAllocGuide = clientConfigBuilder
                            .comment(" ",
                                    " Enable this if you want to be able to change the link your users are sent to when",
                                    " they ask for instructions on how to change their memory allocation settings.",
                                    " ",
                                    " This is mainly useful for when you're using an unsupported version of this mod and",
                                    " the default guide is outdated.")
                            .define("enableCustomMemoryGuide", false);
                    customMemoryAllocGuideURL = clientConfigBuilder
                            .comment(" ",
                                    " The URL of the guide you want users to visit when they want to change their memory",
                                    " allocation settings.",
                                    " ",
                                    "Note: enableCustomJavaUpgradeGuide must be enabled for this to take effect",
                                    " ",
                                    "Note: The URL must start with \"https://\" for security reasons.")
                            .define("customMemoryAllocGuideURL", "https://ozli.ga");
                } clientConfigBuilder.pop();

                // Java.Memory.Min
                clientConfigBuilder.push("Min"); {

                    // Java.Memory.Min.Requirement
                    clientConfigBuilder.push("Requirement"); {
                        enableMinMemoryRequirement = clientConfigBuilder
                                .comment(" ",
                                        " Enable this to require that at least X amount of RAM is available to the modpack",
                                        " for allocating.",
                                        " ",
                                        " This is useful if you have users complaining about \"OutOfMemory\" crashes.",
                                        " ",
                                        " Note: This is *separate* from enableMinMemoryWarning - you can have a separate",
                                        " min RAM allocation requirement and warning.")
                                .define("enableMinMemoryRequirement", true);
                        reqMinMemoryAmountInGB = clientConfigBuilder
                                .comment(" ", " The minimum amount of allocated RAM in GB needed to be able to launch the modpack.")
                                .defineInRange("reqMinMemoryAmountInGB", 0.5, 0.1, 1024.0);
                    } clientConfigBuilder.pop();

                    // Java.Memory.Min.Warning
                    clientConfigBuilder.push("Warning"); {
                        enableMinMemoryWarning = clientConfigBuilder
                                .comment(" ",
                                        " Enable this to show a warning when less than X amount of RAM is available to",
                                        " the modpack for allocating.",
                                        " ",
                                        " Think of this like a recommended amount while the requirement is a minimum amount.",
                                        " ",
                                        " Warning: Setting this too high could make it impossible for some of your users",
                                        " to allocate the amount you're recommending and may actually hurt performance",
                                        " (see the max memory section for details).",
                                        " ",
                                        " Note: This is *separate* from enableMinMemoryRequirement - you can have a",
                                        " separate min RAM allocation requirement and warning.")
                                .define("enableMinMemoryWarning", true);
                        warnMinMemoryAmountInGB = clientConfigBuilder
                                .comment(" ",
                                        " The minimum recommended amount of allocated RAM in GB needed to skip the warning",
                                        " message when launching the modpack.")
                                .defineInRange("warnMinMemoryAmountInGB", 1.0, 0.1, 1024.0);
                    } clientConfigBuilder.pop();

                } clientConfigBuilder.pop();

                // Java.Memory.Max
                clientConfigBuilder.push("Max"); {

                    // Java.Memory.Max.Requirement
                    clientConfigBuilder.push("Requirement"); {
                        enableMaxMemoryRequirement = clientConfigBuilder
                                .comment(" ",
                                        " Enable this to require that no more than X amount of RAM is available to the",
                                        " modpack for allocating.",
                                        " ",
                                        " This is useful for preventing users from allocating excessive amounts of RAM",
                                        " to the point of causing nasty GC-related lag spikes as a result.")
                                .define("enableMaxMemoryRequirement", true);
                        reqMaxMemoryAmountInGB = clientConfigBuilder
                                .comment(" ", "The maximum amount of allocated RAM in GB to be able to launch the modpack.")
                                .define("reqMaxMemoryAmountInGB", 16.0);
                    } clientConfigBuilder.pop();

                    // Java.Memory.Max.Warning
                    clientConfigBuilder.push("Warning"); {
                        enableMaxMemoryWarning = clientConfigBuilder
                                .comment(" ",
                                        " Enable this to show a warning when more than X amount of RAM is available to",
                                        " the modpack for allocating.",
                                        " ",
                                        " This is useful for warning users that are allocating excessive amounts of RAM",
                                        " to the point of causing nasty GC-related lag spikes as a result.")
                                .define("enableMaxMemoryWarning", true);
                        warnMaxMemoryAmountInGB = clientConfigBuilder
                                .comment(" ",
                                        " The maximum recommended amount of allocated RAM in GB needed to skip the warning",
                                        " message when launching the modpack.")
                                .define("warnMaxMemoryAmountInGB", 14.0);
                    } clientConfigBuilder.pop();

                } clientConfigBuilder.pop();

                // Java.Memory.NearMax
                clientConfigBuilder.push("NearMax"); {

                    // Java.Memory.NearMax.Warning
                    clientConfigBuilder.push("Warning"); {
                        enableNearMaxMemoryWarning = clientConfigBuilder
                                .comment(" ",
                                        " Enable this to show a warning when not enough RAM is left over for the OS and",
                                        " drivers to use.",
                                        " ",
                                        " This is useful for warning users that are allocating so much RAM that there ",
                                        " isn't enough left over for other important processes to use without hitting",
                                        " the much slower swap space, hurting performance as a result.")
                                .define("enableNearMaxMemoryWarning", true);
                        warnNearMaxMemoryWarningInGB = clientConfigBuilder
                                .comment(" ",
                                        " The minimum recommended amount of memory left over after allocation in GB needed",
                                        " to skip the warning message when launching the modpack.")
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
                        .comment(" ",
                                " Enable this if you want to change the name of the Minecraft window.")
                        .define("enableCustomWindowTitle", true);
                customWindowTitleText = clientConfigBuilder
                        .comment(" ",
                                " The name you want your Minecraft window to be.",
                                " ",
                                " Note: enableCustomWindowTitle must be enabled for this to take effect.")
                        .define("customWindowTitleText", "");
                enableAppendingToCustomTitle = clientConfigBuilder
                        .comment(" ",
                                " Enable this if you want the game's version to be appended to the end of your",
                                " customWindowTitleText.",
                                " ",
                                " For example: \"ModpackName (Minecraft* 1.16.5)\"",
                                " Note: This is enabled by default because Mojang went out of their way to prevent",
                                " modders from changing the window title easily - this setting is a reasonable",
                                " middle-ground where both modpack authors' and Mojang's preferences are respected.",
                                " ",
                                "Note: I recommend leaving this enabled out of respect if you do enableCustomWindowTitle.")
                        .define("enableAppendingToCustomTitle", true);
                enableUsingAutodetectedDisplayName = clientConfigBuilder
                        .comment(" ",
                                " Whether or not to automatically use your modpack's display name instead of the ",
                                " customWindowTitleText when launching from a supported launcher.",
                                " ",
                                " Note: This will override the contents of customWindowTitleText when launching from a",
                                " supported launcher.",

                                "Note: enableCustomWindowTitle must be enabled for this to take effect.")
                        .define("enableUsingAutodetectedDisplayName", true);
            } clientConfigBuilder.pop();

            // Display.Icon
            clientConfigBuilder.push("Icon"); {
                enableCustomIcon = clientConfigBuilder
                        .comment(" ",
                                " Enable this if you want to change the window icon of the Minecraft window.",
                                " Note: The icon needs to be placed in config" + File.separator + "itlt" + File.separator + "icon.png.",
                                " ",
                                " Note: For best results, use a square PNG with one of these sizes: 128x128, 96x96, ",
                                " 64x64, 48x48, 32x32, 24x24, 16x16.",
                                " ",
                                " Warning: Icon sizes beyond 128px squared or non-square icons may result in a poor ",
                                " quality image on some operating systems as well as wasting storage space and bandwidth.")
                        .define("enableCustomIcon", false);
                enableUsingAutodetectedIcon = clientConfigBuilder
                        .comment(" ",
                                " Enable this if you want itlt to automatically use your modpack's icon instead of the",
                                " icon.png when launching from a supported launcher.",
                                " ",
                                " Note: This will override the config" + File.separator + "itlt" + File.separator + "icon.png when",
                                " launching from a supported launcher modpack.",
                                " ",
                                " Note: enableCustomIcon must be enabled for this to take effect.")
                        .define("enableUsingAutodetectedIcon", true); // Currently supported launchers: Technic, MultiMC.
            } clientConfigBuilder.pop();

        } clientConfigBuilder.pop(); // end of Display section

        // Server list section
        clientConfigBuilder.push("ServerList"); {
            enableCustomServerListEntries = clientConfigBuilder
                    .comment(" No comment yet") // todo
                    .define("enableCustomServerListEntries", false);
        } clientConfigBuilder.pop();

        // Anti-cheat section
        clientConfigBuilder.push("Anti-cheat"); { //.comment("No silver bullet, but definitely helps combat against some cheaters. Intended to compliment a full server-side anti-cheat mod/plugin.\r\n");
            enableAnticheat = clientConfigBuilder
                    .comment(" ",
                            " Whether or not to detect and report known cheats to servers with itlt installed" +
                            " and anti-cheat enabled.",
                            " ",
                            " Note: Disabling this won't suddenly allow you to cheat on said servers - it'll simply",
                            " prevent you from joining some of them at all.",
                            " ",
                            " Note: Depending on the server, you may be able to join it with cheats installed even if",
                            " anti-cheat is enabled on the server. The action the server takes towards cheaters is",
                            " down to the server's staff.")
                    .define("enableAnticheat", true);
            enableAutoRemovalOfCheats = clientConfigBuilder
                    .comment(" ",
                            " Enable this if you want itlt to automatically delete known cheat mods so that they don't",
                            " run on next launch.",
                            " ",
                            " This feature is intended to prevent accidental cheating on servers.",
                            " You'll probably want to disable this setting if you want to cheat on singleplayer.",
                            " ",
                            " Note: enableAnticheat must be enabled for this to take effect.")
                    .define("enableAutoRemovalOfCheats", false);

            // Anti-cheat.Advanced
            clientConfigBuilder.push("Advanced"); {
                parallelModChecksThreshold = clientConfigBuilder
                        .comment(" ",
                                " To check if a known cheat mod is present, itlt needs to iterate through each mod.",
                                " These checks are pretty fast, but can still benefit from multithreading if there's",
                                " *a lot* of mods.",
                                " ",
                                " Here you can change the threshold for how many mods are needed before multithreading's",
                                " attempted. The default is >100 mods.",
                                " ",
                                " Warning: There's an overhead associated with multithreading so having the threshold too",
                                " low can actually hurt performance.")
                        .defineInRange("parallelModChecksThreshold", 100, 1, 1024);
                preferredChecksumType = clientConfigBuilder
                        .comment(" No comment yet")
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
