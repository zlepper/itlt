package dk.zlepper.itlt.client;

import com.google.gson.Gson;
import dk.zlepper.itlt.client.helpers.LauncherUtils;
import dk.zlepper.itlt.itlt;
import dk.zlepper.itlt.client.helpers.ClientUtils;
import dk.zlepper.itlt.client.helpers.Message;

import net.minecraft.client.Minecraft;

import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

@Mod.EventBusSubscriber(modid=itlt.MOD_ID, value=Dist.CLIENT, bus=Mod.EventBusSubscriber.Bus.MOD)
public class ClientModEvents {

    public static float currentMem = Runtime.getRuntime().maxMemory() / 1073741824.0F;

    @SubscribeEvent(priority = EventPriority.HIGHEST) // run this as soon as possible to avoid wasting time if a requirement isn't met
    public static void commonInit(final FMLCommonSetupEvent event) {

        final int javaVerInt = ClientUtils.getJavaVersion();
        itlt.LOGGER.debug("javaVerInt: " + javaVerInt);

        // Minimum Java version requirement and warning
        itlt.LOGGER.debug("requiredMinJavaVerion: " + ClientConfig.requiredMinJavaVersion.get());
        itlt.LOGGER.debug("warnMinJavaVersion: " + ClientConfig.warnMinJavaVersion.get());
        if (ClientConfig.enableMinJavaVerRequirement.get() && javaVerInt < ClientConfig.requiredMinJavaVersion.get()) {
            ClientUtils.startUIProcess(Message.Content.NeedsNewerJava);
        } else if (ClientConfig.enableMinJavaVerWarning.get() && javaVerInt < ClientConfig.warnMinJavaVersion.get()) {
            if (ClientConfig.selectivelyIgnoreMinJavaVerWarning.get()) {
                final LauncherUtils.LauncherName detectedLauncher = LauncherUtils.detectLauncher();
                itlt.LOGGER.info("detectedLauncher: " + detectedLauncher.toString());
                if (detectedLauncher == LauncherUtils.LauncherName.CurseClient)
                    itlt.LOGGER.info("Skipping minJavaVerWarning as you appear to be using the " + detectedLauncher.toString()
                            + " launcher which currently does not allow changing Java version beyond Java 8. :(");
                else ClientUtils.startUIProcess(Message.Content.WantsNewerJava);
            } else {
                ClientUtils.startUIProcess(Message.Content.WantsNewerJava);
            }
        }

        // Max Java version requirement and warning
        itlt.LOGGER.debug("requiredMaxJavaVerion: " + ClientConfig.requiredMaxJavaVersion.get());
        itlt.LOGGER.debug("warnMaxJavaVersion: " + ClientConfig.warnMaxJavaVersion.get());
        if (ClientConfig.enableMaxJavaVerRequirement.get() && javaVerInt > ClientConfig.requiredMaxJavaVersion.get()) {
            ClientUtils.startUIProcess(Message.Content.NeedsOlderJava);
        } else if (ClientConfig.enableMaxJavaVerWarning.get() && javaVerInt > ClientConfig.warnMaxJavaVersion.get()) {
            if (ClientConfig.selectivelyIgnoreMaxJavaVerWarning.get()) {
                final LauncherUtils.LauncherName detectedLauncher = LauncherUtils.detectLauncher();
                itlt.LOGGER.info("detectedLauncher: " + detectedLauncher.toString());
                if (detectedLauncher == LauncherUtils.LauncherName.CurseClient)
                    itlt.LOGGER.info("Skipping maxJavaVerWarning as you appear to be using the " + detectedLauncher.toString()
                            + " launcher which currently does not allow changing Java version beyond Java 8. :(");
                else ClientUtils.startUIProcess(Message.Content.WantsOlderJava);
            } else {
                ClientUtils.startUIProcess(Message.Content.WantsOlderJava);
            }
        }

        // Memory-related requirements and warnings
        currentMem = Runtime.getRuntime().maxMemory() / 1073741824.0F;

        itlt.LOGGER.debug("currentMem: " + currentMem);
        itlt.LOGGER.debug("reqMinMemoryAmountInGB: " + ClientConfig.reqMinMemoryAmountInGB.get());
        itlt.LOGGER.debug("warnMinMemoryAmountInGB: " + ClientConfig.warnMinMemoryAmountInGB.get());
        itlt.LOGGER.debug("reqMaxMemoryAmountInGB: " + ClientConfig.reqMaxMemoryAmountInGB.get());
        itlt.LOGGER.debug("warnMaxMemoryAmountInGB: " + ClientConfig.warnMaxMemoryAmountInGB.get());

        if (ClientConfig.enableMinMemoryRequirement.get() && currentMem < ClientConfig.reqMinMemoryAmountInGB.get().floatValue())
            ClientUtils.startUIProcess(Message.Content.NeedsMoreMemory);
        else if (ClientConfig.enableMinMemoryWarning.get() && currentMem < ClientConfig.warnMinMemoryAmountInGB.get().floatValue())
            ClientUtils.startUIProcess(Message.Content.WantsMoreMemory);

        if (ClientConfig.enableMaxMemoryRequirement.get() && currentMem > ClientConfig.reqMaxMemoryAmountInGB.get().floatValue())
            ClientUtils.startUIProcess(Message.Content.NeedsLessMemory);
        else if (ClientConfig.enableMaxMemoryWarning.get() && currentMem > ClientConfig.warnMaxMemoryAmountInGB.get().floatValue())
            ClientUtils.startUIProcess(Message.Content.WantsLessMemory);
    }

    @SubscribeEvent
    public static void clientInit(final FMLClientSetupEvent event) {
        final Minecraft mcInstance = event.getMinecraftSupplier().get();

        // Java arch requirement and warning
        final boolean isJava64bit = mcInstance.isJava64bit();
        itlt.LOGGER.debug("isJava64bit: " + isJava64bit);
        if (!isJava64bit) {
            if (ClientConfig.enable64bitRequirement.get()) ClientUtils.startUIProcess(Message.Content.NeedsJava64bit);
            else if (ClientConfig.enable64bitWarning.get()) ClientUtils.startUIProcess(Message.Content.WantsJava64bit);
        }

        // Custom window title text
        if (ClientConfig.enableCustomWindowTitle.get()) {
            String customWindowTitle = ClientConfig.customWindowTitleText.get();

            if (ClientConfig.enableUsingAutodetectedDisplayName.get()) {
                final LauncherUtils.LauncherName detectedLauncher = LauncherUtils.detectLauncher();
                itlt.LOGGER.info("detectedLauncher: " + detectedLauncher.toString());
                try {
                    switch (detectedLauncher) {
                        case Technic:
                            // if running from the Technic Launcher, use the pack slug's displayName
                            customWindowTitle = LauncherUtils.getTechnicPackName();
                            break;
                        case MultiMC:
                            // if running from the MultiMC launcher, use the instance's user-friendly name
                            customWindowTitle = LauncherUtils.getMultiMCInstanceName();
                            break;
                        case CurseClient:
                            // if running from the Curse Client launcher, use the profile's name
                            customWindowTitle = LauncherUtils.getCurseClientProfileName();
                            break;
                        default:
                            break;
                    }
                } catch (final IOException e) {
                    itlt.LOGGER.warn("Unable to auto-detect modpack display name, falling back to customWindowTitleText in the config.");
                    e.printStackTrace();
                }
            }
            itlt.LOGGER.info("customWindowTitle: " + customWindowTitle);

            // set the new window title
            if (!customWindowTitle.equals(""))
                // func_230148_b_ == setWindowTitle, func_230149_ax_ == getWindowTitle
                mcInstance.getMainWindow().func_230148_b_(customWindowTitle + " (" + mcInstance.func_230149_ax_() + ")");
        }

        // Custom window icon
        if (ClientConfig.enableCustomIcon.get()) {
            File customIcon = null;

            final File itltDir = Paths.get(FMLPaths.CONFIGDIR.get().toAbsolutePath().toString(), "itlt").toFile();
            if (itltDir.exists()) {
                customIcon = Paths.get(itltDir.getAbsolutePath(), "icon.png").toFile();
            } else {
                if (!itltDir.mkdir())
                    itlt.LOGGER.warn("Unable to make an \"itlt\" folder inside the config folder. Please make it manually.");
            }

            if (ClientConfig.enableUsingAutodetectedIcon.get()) {
                final File autoDetectedIcon;
                final LauncherUtils.LauncherName detectedLauncher = LauncherUtils.detectLauncher();
                itlt.LOGGER.info("detectedLauncher: " + detectedLauncher.toString());
                switch (detectedLauncher) {
                    case Technic:
                        autoDetectedIcon = LauncherUtils.getTechnicPackIcon();
                        break;
                    case MultiMC:
                        autoDetectedIcon = LauncherUtils.getMultiMCInstanceIcon();
                        break;
                    default:
                        autoDetectedIcon = null;
                        break;
                }
                if (autoDetectedIcon != null) customIcon = autoDetectedIcon;
            }

            if (customIcon != null && customIcon.exists() && !customIcon.isDirectory()) {
                try {
                    ClientUtils.setWindowIcon(customIcon, mcInstance);
                } catch (final IOException e) {
                    itlt.LOGGER.error("Unable to set the window icon.");
                    e.printStackTrace();
                }
            }
            else itlt.LOGGER.warn("enableCustomIcon is true but icon.png is missing or invalid.");
        }

        // Custom server list entries
        // todo: revisit this at another time to simplify and cleanup the code
        if (ClientConfig.enableCustomServerListEntries.get()) {
            final File itltDir = Paths.get(FMLPaths.CONFIGDIR.get().toAbsolutePath().toString(), "itlt").toFile();

            if (itltDir.exists()) {
                final Path customServersJsonPath = Paths.get(itltDir.getAbsolutePath(), "servers.json");
                if (customServersJsonPath.toFile().exists()) {
                    String customServersJson = null;
                    try {
                        customServersJson = new String(Files.readAllBytes(customServersJsonPath));
                    } catch (final IOException e) {
                        itlt.LOGGER.error("Unable to read the contents of " + customServersJsonPath);
                        e.printStackTrace();
                    }

                    if (customServersJson != null) {
                        ClientUtils.CustomServerData[] featuredList = new Gson().fromJson(customServersJson, ClientUtils.CustomServerData[].class);
                        if (featuredList != null) {
                            ServerList serverList = new ServerList(mcInstance);
                            for (ClientUtils.CustomServerData customServerEntry : featuredList) {
                                ServerData servertoAdd = new ServerData(customServerEntry.name, customServerEntry.IP, false);
                                if (customServerEntry.forceResourcePack != null && customServerEntry.forceResourcePack)
                                    servertoAdd.setResourceMode(ServerData.ServerResourceMode.ENABLED);
                                if (!ClientUtils.alreadyInServerList(servertoAdd, serverList)) {
                                    itlt.LOGGER.info("Adding custom server entry");
                                    serverList.addServerData(servertoAdd);
                                    serverList.saveServerList();
                                }
                            }
                        }
                    }
                }
            } else {
                itlt.LOGGER.warn("itlt folder in the config folder is missing.");
                if (itltDir.mkdir()) itlt.LOGGER.info("The folder has been successfully created for you.");
                else itlt.LOGGER.warn("Please create a folder named \"itlt\" (case sensitive) in the config folder.");
            }
        }
    }
}
