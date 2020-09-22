package dk.zlepper.itlt.client;

import dk.zlepper.itlt.itlt;
import dk.zlepper.itlt.client.helpers.ClientUtils;
import dk.zlepper.itlt.client.helpers.MessageContent;

import net.minecraft.client.Minecraft;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
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

        if (ClientConfig.enableMinJavaVerRequirement.get() && javaVerInt < ClientConfig.requiredMinJavaVersion.get())
            ClientUtils.startUIProcess(MessageContent.NeedsNewerJava);
        else if (ClientConfig.enableMinJavaVerWarning.get() && javaVerInt < ClientConfig.warnMinJavaVersion.get())
            ClientUtils.startUIProcess(MessageContent.WantsNewerJava);


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
        if (!isJava64bit)
            if (ClientConfig.enable64bitRequirement.get())
                ClientUtils.startUIProcess(MessageContent.NeedsJava64bit);
            else if (ClientConfig.enable64bitWarning.get())
                ClientUtils.startUIProcess(MessageContent.WantsJava64bit);


        // Custom window title text
        if (ClientConfig.enableCustomWindowTitle.get())
            mcInstance.getMainWindow().setWindowTitle(ClientConfig.customWindowTitleText.get() + " (" + mcInstance.getWindowTitle() + ")");


        // Custom window icon
        if (ClientConfig.enableCustomIcon.get()) {
            final File itltDir = Paths.get(FMLPaths.CONFIGDIR.get().toAbsolutePath().toString(), "itlt").toFile();
            if (itltDir.exists()) {
                final File icon = Paths.get(itltDir.getAbsolutePath(), "icon.png").toFile();
                if (!icon.exists()) itlt.LOGGER.warn("enableCustomIcon is true but the icon is missing.");
                if (icon.exists() && !icon.isDirectory()) ClientUtils.setWindowIcon(icon, mcInstance);
            } else {
                itlt.LOGGER.warn("itlt folder in the config folder is missing.");
                if (itltDir.mkdir())
                    itlt.LOGGER.info("The folder has been successfully created for you.");
                else
                    itlt.LOGGER.info("Please create a folder named \"itlt\" (case sensitive) in the config folder.");
            }
        }
    }
}