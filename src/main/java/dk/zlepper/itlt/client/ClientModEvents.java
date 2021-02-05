package dk.zlepper.itlt.client;

import com.google.gson.Gson;
import dk.zlepper.itlt.client.helpers.LauncherUtils;
import dk.zlepper.itlt.itlt;
import dk.zlepper.itlt.client.helpers.ClientUtils;
import dk.zlepper.itlt.client.helpers.MessageContent;

import net.minecraft.client.Minecraft;

import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
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
    public static String javaVer = System.getProperty("java.version");
    public static int javaVerInt;

    @SubscribeEvent(priority = EventPriority.HIGHEST) // run this as soon as possible to avoid wasting time if a requirement isn't met
    public static void commonInit(final FMLCommonSetupEvent event) {
        // Minimum Java version requirement and warning
        javaVer = System.getProperty("java.version");
        final String[] splitJavaVer = javaVer.split(Pattern.quote("."));
        javaVerInt = Integer.parseInt(splitJavaVer[0]);
        if (javaVerInt == 1) javaVerInt = Integer.parseInt(splitJavaVer[1]); // account for older Java versions that advertise as "1.8" instead of "8".

        itlt.LOGGER.debug("javaVer: " + javaVer);
        itlt.LOGGER.debug("javaVerInt: " + javaVerInt);
        itlt.LOGGER.debug("requiredMinJavaVerion: " + ClientConfig.requiredMinJavaVersion.get());
        itlt.LOGGER.debug("warnMinJavaVersion: " + ClientConfig.warnMinJavaVersion.get());

        if (ClientConfig.enableMinJavaVerRequirement.get() && javaVerInt < ClientConfig.requiredMinJavaVersion.get()) {
            ClientUtils.startUIProcess(MessageContent.NeedsNewerJava);
        } else if (ClientConfig.enableMinJavaVerWarning.get() && javaVerInt < ClientConfig.warnMinJavaVersion.get()) {
            if (ClientConfig.selectivelyIgnoreMinJavaVerWarning.get()) {
                final LauncherUtils.LauncherName detectedLauncher = LauncherUtils.detectLauncher();
                itlt.LOGGER.info("detectedLauncher: " + detectedLauncher.toString());
                if (detectedLauncher == LauncherUtils.LauncherName.CurseClient)
                    itlt.LOGGER.info("Skipping minJavaVerWarning as you appear to be using the " + detectedLauncher.toString()
                            + " launcher which currently does not allow changing Java version beyond Java 8. :(");
                else ClientUtils.startUIProcess(MessageContent.WantsNewerJava);
            } else {
                ClientUtils.startUIProcess(MessageContent.WantsNewerJava);
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
            ClientUtils.startUIProcess(MessageContent.NeedsMoreMemory);
        else if (ClientConfig.enableMinMemoryWarning.get() && currentMem < ClientConfig.warnMinMemoryAmountInGB.get().floatValue())
            ClientUtils.startUIProcess(MessageContent.WantsMoreMemory);

        if (ClientConfig.enableMaxMemoryRequirement.get() && currentMem > ClientConfig.reqMaxMemoryAmountInGB.get().floatValue())
            ClientUtils.startUIProcess(MessageContent.NeedsLessMemory);
        else if (ClientConfig.enableMaxMemoryWarning.get() && currentMem > ClientConfig.warnMaxMemoryAmountInGB.get().floatValue())
            ClientUtils.startUIProcess(MessageContent.WantsLessMemory);
    }

    @SubscribeEvent
    public static void clientInit(final FMLClientSetupEvent event) {
        final Minecraft mcInstance = event.getMinecraftSupplier().get();

        // Java arch requirement and warning
        final boolean isJava64bit = mcInstance.isJava64bit();
        itlt.LOGGER.debug("isJava64bit: " + isJava64bit);
        if (!isJava64bit) {
            if (ClientConfig.enable64bitRequirement.get()) ClientUtils.startUIProcess(MessageContent.NeedsJava64bit);
            else if (ClientConfig.enable64bitWarning.get()) ClientUtils.startUIProcess(MessageContent.WantsJava64bit);
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
                mcInstance.getMainWindow().setWindowTitle(customWindowTitle + " (" + mcInstance.getWindowTitle() + ")");
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
