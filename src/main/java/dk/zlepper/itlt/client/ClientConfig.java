package dk.zlepper.itlt.client;

// todo: being able to imc to the itlt mod and get a crash report back if it was your fault

// todo: investigate relaunching the game with modern Java when available on the system on launchers that force Java 8
// todo progress: proof-of-concept done with Trailblaze, determined it's too much work for now

// todo: launch game in fullscreen by default config option (while still respecting the player's choice if they change it)

// todo: account for modpack authors setting the max lower than the min and enabling both, making it impossible to satisfy
//       a want or need. When this happens, show a tailored error message for this specific scenario or change the
//       behaviour so that the max warn/need gets disabled when the min is higher (with updated config comments to note this)

import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import dk.zlepper.itlt.client.helpers.ConfigUtils;
import dk.zlepper.itlt.client.helpers.Migration;
import dk.zlepper.itlt.itlt;
import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class ClientConfig {

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
            ignoreMinJavaVerWarningWhenVerForced,
            enableMaxJavaVerRequirement,
            enableMaxJavaVerWarning,
            ignoreMaxJavaVerWarningWhenVerForced,
            enableCustomWindowTitle,
            enableUsingAutodetectedDisplayName,
            enableEnhancedVanillaIcon,
            enableCustomIcon,
            enableUsingAutodetectedIcon,
            enableCustomServerListEntries,
            enableExplicitGC,
            enableWelcomeScreen,
            enableUsingCustomWelcomeHeaderModpackDisplayName;

    public static ForgeConfigSpec.ConfigValue<String>
            customWindowTitleText,
            custom64bitJavaGuideURL,
            customJavaUpgradeGuideURL,
            customJavaDowngradeGuideURL,
            customMemoryAllocGuideURL,
            autoDetectedDisplayNameFallback,
            customWelcomeHeaderModpackDisplayName,
            configVersion;

    public static ForgeConfigSpec.DoubleValue
            reqMinMemoryAmountInGB,
            reqMaxMemoryAmountInGB,
            warnMinMemoryAmountInGB,
            warnMaxMemoryAmountInGB,
            warnNearMaxMemoryWarningInGB;

    public static ForgeConfigSpec.IntValue
            requiredMinJavaVersion,
            warnMinJavaVersion,
            requiredMaxJavaVersion,
            warnMaxJavaVersion;

    public static ForgeConfigSpec.ConfigValue<List<? extends String>> doExplicitGCWhen;

    public enum explicitGCTriggers {
        Pause, Sleep, Menu
    }

    // a List<String> of all the possible values of the explicitGCTriggers enum
    public static List<String> explicitGCTriggersStrList = Arrays.stream(explicitGCTriggers.values()).map(Enum::toString).collect(Collectors.toList());

    /** Simplify floats to ints when they represent the same value (e.g. show "1" instead of "1.0") **/
    public static String getSimplifiedFloatStr(final float floatNum) {
        if (floatNum == (int) floatNum && floatNum != 0)
            return String.valueOf((int) floatNum);
        else return String.valueOf(floatNum);
    }

    public static boolean areAnyWarningsEnabled() {
        return enableMinMemoryWarning.get() || enableMaxMemoryWarning.get() ||
                enableMinJavaVerWarning.get() || enableMaxJavaVerWarning.get() || enable64bitWarning.get();
    }

    public static void init() {

        @Nullable
        UnmodifiableCommentedConfig oldConfig = null;

        final File configFile = ConfigUtils.configDir.resolve("itlt-client.toml").toFile();
        final File oldConfigFile = ConfigUtils.configDir.resolve("itlt-client.toml.bak").toFile();
        String detectedConfigVersion = "Unknown";
        boolean shouldMigrate = false;

        // If a config file already exists, we make a backup and read that instead to avoid corrupting it
        if (configFile.exists()) {
            ConfigUtils.backup();
            try {
                oldConfig = ConfigUtils.readToml(oldConfigFile);
            } catch (final IOException e) {
                itlt.LOGGER.error(e);
                e.printStackTrace();
            }
        } else {
            itlt.LOGGER.info("Couldn't find a config file, making one...");
        }

        if (oldConfig != null) { // oldConfig will be null if no config file exists yet
            detectedConfigVersion = ConfigUtils.getConfigVersion(oldConfig);
            itlt.LOGGER.info("detectedConfigVersion: " + detectedConfigVersion);
            if (detectedConfigVersion.equals(itlt.VERSION)) {
                // Delete the backup (itlt-client.toml.bak) if no migration is necessary
                itlt.LOGGER.info("Removing backup as no migration is necessary");
                ConfigUtils.delete(oldConfigFile.toPath());
            } else {
                /* Delete the current config (itlt-client.toml) so that the code below makes the new config file, then
                 * once that's done we check shouldMigrate and run Migration.migrate() if true, which handles copying
                 * over the settings from the old format (itlt-client.toml.bak) to the new format (itlt-client.toml)
                 */
                ConfigUtils.delete(configFile.toPath());
                shouldMigrate = true;
            }
        }

        final ForgeConfigSpec.Builder clientConfigBuilder = new ForgeConfigSpec.Builder();

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
                                    " JVM args and have Xms and Xmx be the same value. Omit/don't include the AlwaysPreTouch",
                                    " arg for lower physical memory usage (thus allowing other apps to use memory the game isn't",
                                    " currently using, at the cost of memory allocation slowdowns when the game needs it).",
                                    " ",
                                    " Warning: This option has no effect if the -XX:+DisableExplicitGC JVM arg is present.",
                                    " ",
                                    " Turn this on to help prevent and/or reduce GC-related lag spikes, turn it off for",
                                    " Vanilla behaviour (only rely on the pressure-based automatic GC). This is off by",
                                    " default as it may actually hurt performance if Xms and Xmx aren't the same!")
                            .define("enableExplicitGC", false);

                    doExplicitGCWhen = clientConfigBuilder
                            .comment(" ",
                                    " A list of triggers of when to run explicit GC.",
                                    " ",
                                    " Pause: When the player pauses the game.",
                                    " Sleep: When the player is sleeping in a bed.",
                                    " Menu: When navigating one of the following opaque background screens: ",
                                    "     Singleplayer world selection, Multiplayer server selection,",
                                    "     Resource Pack selection, Language selection, Chat options, Controls options,",
                                    "     Accessibility options, Realms main screen and Stats menu.",
                                    " ",
                                    " Note: It's mainly useful to remove \"Pause\" from this list if you usually only",
                                    " have the game paused for a tiny amount of time (i.e. less than ~2s).",
                                    " ",
                                    " Note: It's mainly useful to remove \"Menu\" from this list for speedruns that",
                                    " start the timer when the main menu is shown.",
                                    " ",
                                    " Note: enableExplicitGC must be true for this to have any effect.")
                            .defineList("doExplicitGCWhen", explicitGCTriggersStrList, entry -> explicitGCTriggersStrList.contains(entry.toString()));

                } clientConfigBuilder.pop();

            } clientConfigBuilder.pop(); // end of Java.Advanced

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
                                    " the default guide's outdated or you're using your own custom launcher.")
                            .define("enableCustom64bitJavaGuide", false);
                    custom64bitJavaGuideURL = clientConfigBuilder
                            .comment(" ",
                                    " The URL of the guide you want users to visit when they want 64bit Java.",
                                    " ",
                                    " Note: enableCustom64bitJavaGuide must be enabled for this to take effect.",
                                    " Note: The URL must start with \"https://\" for security reasons.",
                                    " Note: itlt supports the following *optional* string insertions, useful for being",
                                    " able to show an exact guide: %launcher, %reason, %type, %desire, %subject.")
                            .define("custom64bitJavaGuideURL", "https://zlepper.github.io/itlt/guide?launcher=%launcher&reason=%reason&type=%type&desire=%desire&subject=%subject&debug=false");
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
                                        " and the default guide is outdated or you're using your own custom launcher.")
                                .define("enableCustomJavaUpgradeGuide", false);
                        customJavaUpgradeGuideURL = clientConfigBuilder
                                .comment(" ",
                                        " The URL of the guide you want users to visit when they want to upgrade Java.",
                                        " ",
                                        " Note: enableCustomJavaUpgradeGuide must be enabled for this to take effect.",
                                        " Note: The URL must start with \"https://\" for security reasons.",
                                        " Note: itlt supports the following *optional* string insertions, useful for being",
                                        " able to show an exact guide: %launcher, %reason, %type, %desire, %subject.")
                                .define("customJavaUpgradeGuideURL", "https://zlepper.github.io/itlt/guide?launcher=%launcher&reason=%reason&type=%type&desire=%desire&subject=%subject&debug=false");
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
                                .defineInRange("requiredMinJavaVersion", 8, 6, 127);
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
                        ignoreMinJavaVerWarningWhenVerForced = clientConfigBuilder
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
                                        " and the default guide is outdated or you're using your own custom launcher.")
                                .define("enableCustomJavaDowngradeGuide", false);
                        customJavaDowngradeGuideURL = clientConfigBuilder
                                .comment(" ",
                                        " The URL of the guide you want users to visit when they want 64bit Java.",
                                        " ",
                                        " Note: enableCustomJavaDowngradeGuide must be enabled for this to take effect.",
                                        " Note: The URL must start with \"https://\" for security reasons.",
                                        " Note: itlt supports the following *optional* string insertions, useful for being",
                                        " able to show an exact guide: %launcher, %reason, %type, %desire, %subject.")
                                .define("customJavaDowngradeGuideURL", "https://zlepper.github.io/itlt/guide?launcher=%launcher&reason=%reason&type=%type&desire=%desire&subject=%subject&debug=false");
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
                                .defineInRange("requiredMaxJavaVersion", 15, 6, 127);
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
                        ignoreMaxJavaVerWarningWhenVerForced = clientConfigBuilder
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
                                    " the default guide is outdated or you're using your own custom launcher.")
                            .define("enableCustomMemoryGuide", false);
                    customMemoryAllocGuideURL = clientConfigBuilder
                            .comment(" ",
                                    " The URL of the guide you want users to visit when they want to change their memory",
                                    " allocation settings.",
                                    " ",
                                    " Note: enableCustomJavaUpgradeGuide must be enabled for this to take effect",
                                    " ",
                                    " Note: The URL must start with \"https://\" for security reasons.",
                                    " Note: itlt supports the following *optional* string insertions, useful for being",
                                    " able to show an exact guide: %launcher, %reason, %type, %desire, %subject.")
                            .define("customMemoryAllocGuideURL", "https://zlepper.github.io/itlt/guide?launcher=%launcher&reason=%reason&type=%type&desire=%desire&subject=%subject&debug=false");
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
                                .comment(" ", " The maximum amount of allocated RAM in GB to be able to launch the modpack.")
                                .defineInRange("reqMaxMemoryAmountInGB", 16.0, 0.1, 1024.0);
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
                                .defineInRange("warnMaxMemoryAmountInGB", 14.0, 0.1, 1024.0);
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
        clientConfigBuilder.pop();

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
                                " The name you want your Minecraft window to be. Put \"%mc\" to include the original",
                                " window title's contents to help identify the Minecraft version for example.",
                                " ",
                                " Warning: Mojang have asked people to not change their branding entirely and made it",
                                " harder for modders to change it at all as of MC 1.15 and newer. They clearly don't like",
                                " people taking full credit for their work and I understand that.",
                                " Please make sure you keep the \"%mc\" in your customWindowTitleText as a sign of respect.",
                                " Keeping it also helps others troubleshoot your pack by knowing what Minecraft version",
                                " it's based on - especially useful if your modpack has multiple major releases that",
                                " span across different Minecraft versions.",
                                " ",
                                " Note: Put \"%autoName\" for your modpack's display name to be automatically detected",
                                " and used when launching from a supported launcher.",
                                " ",
                                " Examples:",
                                " - \"ModpackName - %mc\" = \"ModpackName - Minecraft* 1.16.5\"",
                                " - \"%mc - ModpackName\" = \"Minecraft* 1.16.5 - ModpackName\"",
                                " - \"ModpackName (%mc)\" = \"ModpackName (Minecraft* 1.16.5)\"",
                                " - \"%autoName (%mc)\" = \"ModpackName (Minecraft* 1.16.5)\"",
                                " - \"ModpackName v2 based on %mc\" = \"ModpackName v2 based on Minecraft* 1.16.5\"",
                                " ",
                                " Note: enableCustomWindowTitle must be enabled for this to take effect.")
                        .define("customWindowTitleText", "%autoName - %mc");
                enableUsingAutodetectedDisplayName = clientConfigBuilder
                        .comment(" ",
                                " Whether or not to automatically replace \"%autoName\" in customWindowTitleText with",
                                " your modpack's display name when launching from a supported launcher.",
                                " ",
                                " Note: enableCustomWindowTitle must be enabled for this to take effect.")
                        .define("enableUsingAutodetectedDisplayName", true);
                autoDetectedDisplayNameFallback = clientConfigBuilder
                        .comment(" ",
                                " The text to use in place of %autoDetect if we're unable to automatically detect your",
                                " modpack's display name.",
                                " ",
                                " Note: This value will always be used for %autoName if enableUsingAutodetectedDisplayName",
                                " is disabled, regardless of whether or not the pack is launched from a supported launcher.")
                        .define("autoDetectedDisplayNameFallback", "ModpackName");
            } clientConfigBuilder.pop();

            // Display.Icon
            clientConfigBuilder.push("Icon"); {
                enableEnhancedVanillaIcon = clientConfigBuilder
                        .comment(" ",
                                " Enable this if you want itlt to use its HiDPI-aware (aka Retina support) icon setting",
                                " feature with the Vanilla game icon, obtained directly from the game's resources.",
                                " ",
                                " Turning this on should give you a more crisp and detailed icon on higher resolution displays,",
                                " rather than the comparatively blurry 32px PNG that is normally used.")
                        .define("enableEnhancedVanillaIcon", true);
                enableCustomIcon = clientConfigBuilder
                        .comment(" ",
                                " Enable this if you want to change the window and taskbar icon of the Minecraft window.",
                                " ICO, ICNS and PNG icons provided to this mod are supported on all operating systems.",
                                " ",
                                " Note: The icon needs to be placed in config" + File.separator + "itlt" + File.separator + "icon.(ico/icns/png).",
                                " ",
                                " Note: See the itlt wiki for more info.",
                                " ",
                                " Note: This will override the enableEnhancedVanillaIcon when a valid custom icon is found.")
                        .define("enableCustomIcon", true);
                enableUsingAutodetectedIcon = clientConfigBuilder
                        .comment(" ",
                                " Enable this if you want itlt to automatically detect your modpack's icon when launching",
                                " from a supported launcher. If unable to auto-detect, it will fallback to a provided",
                                " icon.ico/icon.icns/icon.png file if available.",
                                " ",
                                " Note: enableCustomIcon must be enabled for this to take effect.")
                        .define("enableUsingAutodetectedIcon", true); // Currently supported launchers: Technic, MultiMC.
            } clientConfigBuilder.pop();

            // Display.WelcomeScreen
            clientConfigBuilder.push("WelcomeScreen"); {
                enableWelcomeScreen = clientConfigBuilder
                        .comment(" ",
                                " Enable this if you want to show a welcome screen to your users the first time they",
                                " start your modpack. You can customise the text shown using a text file.",
                                " ",
                                " Note: The text file needs to be placed in config" + File.separator + "itlt" + File.separator + "welcome.txt",
                                " ",
                                " Warning: This feature is experimental and may change in future v2.x releases. Check the",
                                " changelog before updating if you use this. The changelog will make any breaking changes",
                                " to this feature clear. If there's no mention of this feature in the changelog, rest assured",
                                " you can update without needing to make any changes to your welcome.txt.")
                        .define("enableWelcomeScreen", false);

                enableUsingCustomWelcomeHeaderModpackDisplayName = clientConfigBuilder
                        .comment(" ",
                                " Enable this if you want to change the modpack name that shows up on the heading of the",
                                " welcome screen.",
                                " ",
                                " Note: If you leave this disabled, itlt will use the contents of %autoName (auto-detected",
                                " modpack name - see the enableUsingAutodetectedDisplayName and autoDetectedDisplayNameFallback",
                                " options for details).")
                        .define("enableUsingCustomWelcomeHeaderModpackDisplayName", false);

                customWelcomeHeaderModpackDisplayName = clientConfigBuilder
                        .comment(" ",
                                " If enableUsingCustomWelcomeHeaderModpackDisplayName is true, the welcome screen header will show \"Welcome to x\"",
                                " where x is what you put here.")
                        .define("customWelcomeHeaderModpackDisplayName", "ModpackName");
            } clientConfigBuilder.pop();

        } clientConfigBuilder.pop(); // end of Display section

        // Server list section
        clientConfigBuilder.push("ServerList"); {
            enableCustomServerListEntries = clientConfigBuilder
                    .comment(" ",
                            " Enable this to have itlt add default servers to the Multiplayer list from a JSON file.",
                            " ",
                            " Note: The JSON needs to be placed in config" + File.separator + "itlt" + File.separator + "servers.json",
                            " ",
                            " Warning: This feature is experimental and may change in future v2.x releases. Check the",
                            " changelog before updating if you use this. The changelog will make any breaking changes",
                            " to this feature clear. If there's no mention of this feature in the changelog, rest assured",
                            " you can update without needing to make any changes to your servers.json.")
                    .define("enableCustomServerListEntries", false);
        } clientConfigBuilder.pop(); // end of Server list section

        // Internal section
        clientConfigBuilder.push("Internal"); {
            configVersion = clientConfigBuilder
                    .comment(" ",
                            " The version of itlt that created this config file. Intended to be used for migrating",
                            " config changes when you update the mod. Please don't touch this, this is for itlt itself to change.")
                    .define("configVersion", "2.1.0");
        } clientConfigBuilder.pop();

        // Build the config
        clientConfig = clientConfigBuilder.build();

        // Manually load the config early so that it can be used immediately
        final CommentedFileConfig configData =
                CommentedFileConfig.builder(ConfigUtils.configDir.resolve("itlt-client.toml"))
                        .sync().autoreload().autosave().charset(StandardCharsets.UTF_8).writingMode(WritingMode.REPLACE).build();
        configData.load();
        clientConfig.setConfig(configData);

        // copy over the old config format's settings to the new format if necessary, determined near the start of ClientConfig.init()
        if (shouldMigrate) Migration.migrate(detectedConfigVersion, itlt.VERSION, oldConfig);

        validate();
    }

    private static void validate() {
        if (enableMaxJavaVerRequirement.get() && enableMinJavaVerRequirement.get() && requiredMaxJavaVersion.get() < requiredMinJavaVersion.get())
            itlt.LOGGER.error("Impossible Java version requirements set: requiredMinJavaVersion cannot be higher than requiredMaxJavaVersion.");

        if (enableCustom64bitJavaGuide.get() && !custom64bitJavaGuideURL.get().toLowerCase().startsWith("https://"))
            itlt.LOGGER.error("The custom64bitJavaGuideURL must start with \"https://\"");
        else if (enableCustomJavaDowngradeGuide.get() && !customJavaDowngradeGuideURL.get().toLowerCase().startsWith("https://"))
            itlt.LOGGER.error("The customJavaDowngradeGuideURL must start with \"https://\"");
        else if (enableCustomJavaUpgradeGuide.get() && !customJavaUpgradeGuideURL.get().toLowerCase().startsWith("https://"))
            itlt.LOGGER.error("The customJavaUpgradeGuideURL must start with \"https://\"");
        else if (enableCustomMemoryAllocGuide.get() && !customMemoryAllocGuideURL.get().toLowerCase().startsWith("https://"))
            itlt.LOGGER.error("The customMemoryAllocGuideURL must start with \"https://\"");

    }
}
