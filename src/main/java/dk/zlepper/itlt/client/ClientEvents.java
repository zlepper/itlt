package dk.zlepper.itlt.client;

import dk.zlepper.itlt.itlt;
import dk.zlepper.itlt.client.helpers.ClientUtils;
import dk.zlepper.itlt.client.helpers.MessageContent;

import net.minecraft.client.Minecraft;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.regex.Pattern;

@Mod.EventBusSubscriber(modid=itlt.MOD_ID, value=Dist.CLIENT, bus=Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {

    public static float currentMem = Runtime.getRuntime().maxMemory() / 1073741824.0F;
    public static String javaVer = System.getProperty("java.version");
    public static int javaVerInt;

    @SubscribeEvent
    public static void clientInit(FMLClientSetupEvent e) { // todo: look into if it's possible to run earlier than FMLClientSetupEvent for what we want to do
        final Minecraft mcInstance = Minecraft.getInstance();


        // 32bit Java requirement and warning
        final boolean isJava64bit = mcInstance.isJava64bit();
        itlt.LOGGER.debug("isJava64bit: " + isJava64bit);
        if (!isJava64bit) {
            if (ClientConfig.enable64bitRequirement.get()) {
                ClientUtils.startUIProcess(MessageContent.NeedsJava64bit);
            } else if (ClientConfig.enable64bitWarning.get()) {
                ClientUtils.startUIProcess(MessageContent.WantsJava64bit);
            }
        }


        // Minimum Java version requirement and warning
        javaVer = System.getProperty("java.version");
        final String[] splitJavaVer = javaVer.split(Pattern.quote("."));
        javaVerInt = Integer.parseInt(splitJavaVer[0]);
        if (javaVerInt == 1) javaVerInt = Integer.parseInt(splitJavaVer[1]); // account for older Java versions that advertise as "1.8" instead of "8".

        itlt.LOGGER.debug("javaVer: " + javaVer);
        itlt.LOGGER.debug("javaVerInt: " + javaVerInt);
        itlt.LOGGER.debug("requiredMinJavaVerion: " + ClientConfig.requiredMinJavaVerion.get());

        if (ClientConfig.enableMinJavaVerRequirement.get()) {
            if (javaVerInt < ClientConfig.requiredMinJavaVerion.get()) {
                ClientUtils.startUIProcess(MessageContent.NeedsNewerJava);
            }
        }


        // Memory-related requirements and warnings
        currentMem = Runtime.getRuntime().maxMemory() / 1073741824.0F;

        if (ClientConfig.enableMinMemoryRequirement.get()) {
            if (currentMem < ClientConfig.reqMinMemoryAmountInGB.get().floatValue())
                ClientUtils.startUIProcess(MessageContent.NeedsMoreMemory);
        } else if (ClientConfig.enableMinMemoryWarning.get()) {
            if (currentMem < ClientConfig.warnMinMemoryAmountInGB.get().floatValue())
                ClientUtils.startUIProcess(MessageContent.WantsMoreMemory);
        }

        if (ClientConfig.enableMaxMemoryRequirement.get()) {
            if (currentMem > ClientConfig.reqMaxMemoryAmountInGB.get().floatValue())
                ClientUtils.startUIProcess(MessageContent.NeedsLessMemory);
        } else if (ClientConfig.enableMaxMemoryWarning.get()) {
            if (currentMem > ClientConfig.warnMaxMemoryAmountInGB.get().floatValue())
                ClientUtils.startUIProcess(MessageContent.WantsLessMemory);
        }


        // Custom window title text
        if (ClientConfig.enableCustomWindowTitle.get())
            mcInstance.getMainWindow().func_230148_b_(ClientConfig.customWindowTitleText.get());


        // Custom window icon
        if (ClientConfig.enableCustomIcon.get()) {
            final File itltDir = Paths.get(FMLPaths.CONFIGDIR.get().toAbsolutePath().toString(), "itlt").toFile();
            if (itltDir.exists()) {
                final File icon = Paths.get(itltDir.getAbsolutePath(), "icon.png").toFile();
                if (!icon.exists()) itlt.LOGGER.warn("enableCustomIcon is true but the icon is missing.");
                if (icon.exists() && !icon.isDirectory())
                    ClientUtils.setWindowIcon(icon);
            } else {
                itlt.LOGGER.warn("itlt folder in the config folder is missing.");
                if (itltDir.mkdir()) {
                    itlt.LOGGER.info("The folder has been successfully created for you.");
                } else {
                    itlt.LOGGER.info("Please create a folder named \"itlt\" (case sensitive) in the config folder.");
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent e) {
        // Anti-cheat
        if (ClientConfig.enableAnticheat.get()) {
            // todo: report to server with list of known cheats (if any)

            if (ClientConfig.enableAutoRemovalOfCheats.get()) {
                // todo: check with a webserver for latest modids and checksums
                // todo: fingerprinting of known cheat mods (beyond just checksumming)
                // todo: analysis of mods to detect suspiciously cheaty code in unknown cheat mods
                // todo: detect FML tweaker type cheat mods
                ModList.get().getMods().forEach(modInfo -> {
                    final String modId = modInfo.getModId().toLowerCase();
                    final File modFile = modInfo.getOwningFile().getFile().getFilePath().toFile(); // todo: checksumming of cheat mods
                    if ((modId.contains("xray") && !modId.contains("anti")) // xray
                            || modId.equals("forgehax") || modId.equals("forgewurst")) {
                        final File cheatModFile = new File(String.valueOf(modInfo.getOwningFile().getFile().getFilePath()));
                        if (!cheatModFile.delete()) // if it can't be deleted immediately,
                            cheatModFile.deleteOnExit(); // delete it when the game closes or crashes
                    }
                });
            }
        }
    }
}
