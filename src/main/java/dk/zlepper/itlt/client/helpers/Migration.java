package dk.zlepper.itlt.client.helpers;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import dk.zlepper.itlt.client.ClientConfig;
import dk.zlepper.itlt.itlt;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.versions.forge.ForgeVersion;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class Migration {
    final static Path configDir = FMLPaths.CONFIGDIR.get();

    //@Nullable
    static String detectedConfigVer = "unset";

    public static void start() {
        /*
         * The forge config api overwrites unrecognised content, removing if not used. Usually this is fine but for the
         * purposes of migrating from one config schema to another it makes this more challenging.
         *
         * This method makes two copies of the config file so each spec can be read in a non-destructive manner:
         * - itlt-client.toml.bak: backup in case the user wants to revert changes or downgrade
         * - itlt-client-2.0.0.toml: copy of the config file we're given to be read in the v2.0.0 format
         *
         * Once the copies are made, the 2.0.0 copy is read first with the v2.0.0 spec reader. By attempting to read the
         * configVersion field in the v2.0.0 spec, it will return "unset" if the config file is not from v2.0.0. If it is
         * from v2.0.0/1, it'll return "2.0.0" or "2.0.1".
         *
         * Next we read the original itlt-client.toml file with the latest config spec reader. If it returns "unset", this
         * means the config file is new, so write the version of itlt we're running to the config and skip migration. If it
         * returns a version, check that it matches what we're running and start migration if different.
         */

        final Path currentConfigPath = configDir.resolve("itlt-client.toml");
        final Path v2_0_0_configPath = configDir.resolve("itlt-client-2.0.0.toml");

        // no config exists yet, so make one and skip migration
        if (!currentConfigPath.toFile().exists()) {
            ClientConfig.init();
            return;
        }

        backup();

        // Make a copy of itlt-client.toml and call it itlt-client-2.0.0.toml
        if (!copy(currentConfigPath, v2_0_0_configPath)) {
            // if it fails to copy, we can't proceed with migration
            itlt.LOGGER.error("Migration failed: Unable to create a copy of the config");
            return;
        }

        ConfigSpecs.v2_0_0.init(v2_0_0_configPath);
        if (!ConfigSpecs.v2_0_0.configVersion.get().equals("unset"))
            detectedConfigVer = ConfigSpecs.v2_0_0.configVersion.get();

        ClientConfig.init();
        if (detectedConfigVer.equals("unset") && ClientConfig.configVersion.get().equals("unset")) {
            ClientConfig.configVersion.set(itlt.VERSION);
            delete(v2_0_0_configPath);
            return;
        }

        if (detectedConfigVer.equals(itlt.VERSION)) {
            itlt.LOGGER.info("Skipping migration as detected same config version as itlt version");
            delete(v2_0_0_configPath);
            return;
        }

        switch (detectedConfigVer) {
            case "2.0.0", "2.0.1" -> {
                itlt.LOGGER.info("BEFORE");
                itlt.LOGGER.info("enableCustomWindowTitle: " + ConfigSpecs.v2_0_0.enableCustomWindowTitle.get());
                itlt.LOGGER.info("enableCustomServerListEntries: " + ConfigSpecs.v2_0_0.enableCustomServerListEntries.get());

                // Copy over the differing values to the new latest spec's equivalents
                {
                    // v2.0.2 fixed a bug where all config options were inside the Java group.
                    // e.g. [Java.Display.WindowTitle] in v2.0.0 becomes [Display.WindowTitle] in v2.0.2
                    ClientConfig.enableCustomWindowTitle.set(ConfigSpecs.v2_0_0.enableCustomWindowTitle.get());
                    ClientConfig.customWindowTitleText.set(ConfigSpecs.v2_0_0.customWindowTitleText.get());
                    ClientConfig.enableUsingAutodetectedDisplayName.set(ConfigSpecs.v2_0_0.enableUsingAutodetectedDisplayName.get());
                    ClientConfig.autoDetectedDisplayNameFallback.set(ConfigSpecs.v2_0_0.autoDetectedDisplayNameFallback.get());

                    ClientConfig.enableEnhancedVanillaIcon.set(ConfigSpecs.v2_0_0.enableEnhancedVanillaIcon.get());
                    ClientConfig.enableCustomIcon.set(ConfigSpecs.v2_0_0.enableCustomIcon.get());
                    ClientConfig.enableUsingAutodetectedIcon.set(ConfigSpecs.v2_0_0.enableUsingAutodetectedIcon.get());

                    ClientConfig.enableCustomServerListEntries.set(ConfigSpecs.v2_0_0.enableCustomServerListEntries.get());
                }

                // Now that the migration is finished, update the config version and save it
                ClientConfig.configVersion.set("2.0.2");
                ClientConfig.clientConfig.save();

                itlt.LOGGER.info("AFTER");
                itlt.LOGGER.info("enableCustomWindowTitle: " + ClientConfig.enableCustomWindowTitle.get());
                itlt.LOGGER.info("enableCustomServerListEntries: " + ClientConfig.enableCustomServerListEntries.get());
            }
            default -> itlt.LOGGER.error("Migration failed: Unknown config version");
        }

        delete(v2_0_0_configPath);
    }

    static void backup() {
        final Path currentConfigPath = configDir.resolve("itlt-config.toml");
        itlt.LOGGER.info("Backing up itlt config...");
        if (currentConfigPath.toFile().exists() && !currentConfigPath.toFile().isDirectory()) {
            try {
                Files.copy(currentConfigPath, configDir.resolve("itlt-config.toml.bak"), StandardCopyOption.REPLACE_EXISTING);
                itlt.LOGGER.info("itlt config backup completed successfully");
            } catch (final IOException e) {
                itlt.LOGGER.warn("Failed to make a backup of itlt-client.toml");
            }
        }
    }

    static boolean copy(final Path from, final Path to) {
        try {
            Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
            itlt.LOGGER.info(String.format("Copied from \"%s\" to \"%s\" successfully", from, to));
            return true;
        } catch (final IOException e) {
            itlt.LOGGER.warn(String.format("Failed to copy from \"%s\" to \"%s\"", from, to));
            return false;
        }
    }

    static void delete(final Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (final IOException e) {
            try {
                path.toFile().deleteOnExit();
            } catch (final Exception ignored) {}
        }
    }

    static class ConfigSpecs {

        static class v2_0_0 {
            static ForgeConfigSpec clientConfig;

            static ForgeConfigSpec.BooleanValue
                    enable64bitRequirement, enable64bitWarning,
                    enableCustom64bitJavaGuide, enableCustomJavaUpgradeGuide, enableCustomJavaDowngradeGuide,
                    enableMinMemoryRequirement, enableMinMemoryWarning,
                    enableMaxMemoryRequirement, enableMaxMemoryWarning,
                    enableNearMaxMemoryWarning,
                    enableCustomMemoryAllocGuide,
                    enableMinJavaVerRequirement, enableMinJavaVerWarning, selectivelyIgnoreMinJavaVerWarning,
                    enableMaxJavaVerRequirement, enableMaxJavaVerWarning, selectivelyIgnoreMaxJavaVerWarning,
                    enableCustomWindowTitle, enableUsingAutodetectedDisplayName,
                    enableEnhancedVanillaIcon, enableCustomIcon, enableUsingAutodetectedIcon,
                    enableCustomServerListEntries,
                    enableExplicitGC, doExplicitGCOnPause, doExplicitGCOnSleep, doExplicitGCOnMenu;

            static ForgeConfigSpec.ConfigValue<String>
                    customWindowTitleText,
                    custom64bitJavaGuideURL,
                    customJavaUpgradeGuideURL, customJavaDowngradeGuideURL,
                    customMemoryAllocGuideURL,
                    autoDetectedDisplayNameFallback,
                    configVersion;

            static ForgeConfigSpec.ConfigValue<Double>
                    reqMinMemoryAmountInGB, reqMaxMemoryAmountInGB,
                    warnMinMemoryAmountInGB, warnMaxMemoryAmountInGB,
                    warnNearMaxMemoryWarningInGB;

            static ForgeConfigSpec.IntValue
                    requiredMinJavaVersion, warnMinJavaVersion,
                    requiredMaxJavaVersion, warnMaxJavaVersion;

            static void init(final Path configFilePath) {
                final ForgeConfigSpec.Builder clientConfigBuilder = new ForgeConfigSpec.Builder();

                clientConfigBuilder.push("Java"); {
                    clientConfigBuilder.push("Advanced"); {
                        clientConfigBuilder.push("ExplicitGC"); {
                            enableExplicitGC = clientConfigBuilder.define("enableExplicitGC", false);
                            doExplicitGCOnPause = clientConfigBuilder.define("explicitGCOnPause", true);
                            doExplicitGCOnSleep = clientConfigBuilder.define("explicitGCOnSleep", true);
                            doExplicitGCOnMenu = clientConfigBuilder.define("explicitGCOnMenu", true);
                        } clientConfigBuilder.pop();
                    } clientConfigBuilder.pop();

                    clientConfigBuilder.push("Arch"); {
                        clientConfigBuilder.push("Guide"); {
                            enableCustom64bitJavaGuide = clientConfigBuilder.define("enableCustom64bitJavaGuide", false);
                            custom64bitJavaGuideURL = clientConfigBuilder.define("custom64bitJavaGuideURL", "https://zlepper.github.io/itlt/guide?launcher=%launcher&reason=%reason&type=%type&desire=%desire&subject=%subject&debug=false");
                        } clientConfigBuilder.pop();
                        clientConfigBuilder.push("Requirement"); {
                            enable64bitRequirement = clientConfigBuilder.define("enable64bitRequirement", true);
                        } clientConfigBuilder.pop();
                        clientConfigBuilder.push("Warning"); {
                            enable64bitWarning = clientConfigBuilder.define("enable64bitWarning", true);
                        } clientConfigBuilder.pop();
                    } clientConfigBuilder.pop();

                    clientConfigBuilder.push("Version"); {
                        clientConfigBuilder.push("Min"); {
                            clientConfigBuilder.push("Guide"); {
                                enableCustomJavaUpgradeGuide = clientConfigBuilder.define("enableCustomJavaUpgradeGuide", false);
                                customJavaUpgradeGuideURL = clientConfigBuilder.define("customJavaUpgradeGuideURL", "https://zlepper.github.io/itlt/guide?launcher=%launcher&reason=%reason&type=%type&desire=%desire&subject=%subject&debug=false");
                            } clientConfigBuilder.pop();
                            clientConfigBuilder.push("Requirement"); {
                                enableMinJavaVerRequirement = clientConfigBuilder.define("enableMinJavaVerRequirement", true);
                                requiredMinJavaVersion = clientConfigBuilder.defineInRange("requiredMinJavaVerion", 16, 6, 127);
                            } clientConfigBuilder.pop();
                            clientConfigBuilder.push("Warning"); {
                                enableMinJavaVerWarning = clientConfigBuilder.define("enableMinJavaVerWarning", true);
                                warnMinJavaVersion = clientConfigBuilder.defineInRange("warnMinJavaVersion", 16, 6, 127);
                                selectivelyIgnoreMinJavaVerWarning = clientConfigBuilder.define("ignoreMinJavaVerWarningWhenVerForced", true);
                            } clientConfigBuilder.pop();
                        } clientConfigBuilder.pop();

                        clientConfigBuilder.push("Max"); {
                            clientConfigBuilder.push("Guide"); {
                                enableCustomJavaDowngradeGuide = clientConfigBuilder.define("enableCustomJavaDowngradeGuide", false);
                                customJavaDowngradeGuideURL = clientConfigBuilder.define("customJavaDowngradeGuideURL", "https://zlepper.github.io/itlt/guide?launcher=%launcher&reason=%reason&type=%type&desire=%desire&subject=%subject&debug=false");
                            } clientConfigBuilder.pop();
                            clientConfigBuilder.push("Requirement"); {
                                enableMaxJavaVerRequirement = clientConfigBuilder.define("enableMaxJavaVerRequirement", false);
                                requiredMaxJavaVersion = clientConfigBuilder.defineInRange("requiredMaxJavaVerion", 17, 6, 127);
                            } clientConfigBuilder.pop();
                            clientConfigBuilder.push("Warning"); {
                                enableMaxJavaVerWarning = clientConfigBuilder.define("enableMaxJavaVerWarning", false);
                                warnMaxJavaVersion = clientConfigBuilder.defineInRange("warnMaxJavaVersion", 17, 6, 127);
                                selectivelyIgnoreMaxJavaVerWarning = clientConfigBuilder.define("ignoreMaxJavaVerWarningWhenVerForced", true);
                            } clientConfigBuilder.pop();
                        } clientConfigBuilder.pop();
                    } clientConfigBuilder.pop();

                    clientConfigBuilder.push("Memory"); {
                        clientConfigBuilder.push("Guide"); {
                            enableCustomMemoryAllocGuide = clientConfigBuilder.define("enableCustomMemoryGuide", false);
                            customMemoryAllocGuideURL = clientConfigBuilder.define("customMemoryAllocGuideURL", "https://zlepper.github.io/itlt/guide?launcher=%launcher&reason=%reason&type=%type&desire=%desire&subject=%subject&debug=false");
                        } clientConfigBuilder.pop();
                        clientConfigBuilder.push("Min"); {
                            clientConfigBuilder.push("Requirement"); {
                                enableMinMemoryRequirement = clientConfigBuilder.define("enableMinMemoryRequirement", true);
                                reqMinMemoryAmountInGB = clientConfigBuilder.defineInRange("reqMinMemoryAmountInGB", 0.5, 0.1, 1024.0);
                            } clientConfigBuilder.pop();
                            clientConfigBuilder.push("Warning"); {
                                enableMinMemoryWarning = clientConfigBuilder.define("enableMinMemoryWarning", true);
                                warnMinMemoryAmountInGB = clientConfigBuilder.defineInRange("warnMinMemoryAmountInGB", 1.0, 0.1, 1024.0);
                            } clientConfigBuilder.pop();
                        } clientConfigBuilder.pop();
                        clientConfigBuilder.push("Max"); {
                            clientConfigBuilder.push("Requirement"); {
                                enableMaxMemoryRequirement = clientConfigBuilder.define("enableMaxMemoryRequirement", true);
                                reqMaxMemoryAmountInGB = clientConfigBuilder.define("reqMaxMemoryAmountInGB", 16.0);
                            } clientConfigBuilder.pop();
                            clientConfigBuilder.push("Warning"); {
                                enableMaxMemoryWarning = clientConfigBuilder.define("enableMaxMemoryWarning", true);
                                warnMaxMemoryAmountInGB = clientConfigBuilder.define("warnMaxMemoryAmountInGB", 14.0);
                            } clientConfigBuilder.pop();
                        } clientConfigBuilder.pop();
                        clientConfigBuilder.push("NearMax"); {
                            clientConfigBuilder.push("Warning"); {
                                enableNearMaxMemoryWarning = clientConfigBuilder.define("enableNearMaxMemoryWarning", true);
                                warnNearMaxMemoryWarningInGB = clientConfigBuilder.defineInRange("warnNearMaxMemoryWarningInGB", 1.0, 0.1, 2.0);
                            }
                        } clientConfigBuilder.pop();
                    } clientConfigBuilder.pop();
                } clientConfigBuilder.pop();
                clientConfigBuilder.push("Display"); {
                    clientConfigBuilder.push("WindowTitle"); {
                        enableCustomWindowTitle = clientConfigBuilder.define("enableCustomWindowTitle", true);
                        customWindowTitleText = clientConfigBuilder.define("customWindowTitleText", "%autoName - %mc");
                        enableUsingAutodetectedDisplayName = clientConfigBuilder.define("enableUsingAutodetectedDisplayName", true);
                        autoDetectedDisplayNameFallback = clientConfigBuilder.define("autoDetectedDisplayNameFallback", "ModpackName");
                    } clientConfigBuilder.pop();
                    clientConfigBuilder.push("Icon"); {
                        enableEnhancedVanillaIcon = clientConfigBuilder.define("enableEnhancedVanillaIcon", true);
                        enableCustomIcon = clientConfigBuilder.define("enableCustomIcon", true);
                        enableUsingAutodetectedIcon = clientConfigBuilder.define("enableUsingAutodetectedIcon", true);
                    } clientConfigBuilder.pop();
                } clientConfigBuilder.pop();
                clientConfigBuilder.push("ServerList"); {
                    enableCustomServerListEntries = clientConfigBuilder.define("enableCustomServerListEntries", false);
                } clientConfigBuilder.pop();
                clientConfigBuilder.push("Internal"); {
                    configVersion = clientConfigBuilder.define("configVersion", "unset");
                } clientConfigBuilder.pop();

                // Build the config
                clientConfig = clientConfigBuilder.build();

                // Manually load the config file into the clientConfig variable
                final var configData =
                        CommentedFileConfig.builder(FMLPaths.CONFIGDIR.get().resolve(configFilePath))
                                .sync().charset(StandardCharsets.UTF_8).writingMode(WritingMode.REPLACE).build();
                configData.load();
                clientConfig.setConfig(configData);
            }
        }
    }
}
