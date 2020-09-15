package dk.zlepper.itlt.client.helpers;

import dk.zlepper.itlt.itlt;
import dk.zlepper.itlt.client.ClientConfig;
import dk.zlepper.itlt.client.ClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.ModList;

import java.io.*;
import java.nio.file.Path;

public class ClientUtils {

    public static void setWindowIcon(final File icon) {
        try (final InputStream is1 = new FileInputStream(icon.getAbsoluteFile())) {
            try (final InputStream is2 = new FileInputStream(icon.getAbsoluteFile())) {
                Minecraft.getInstance().getMainWindow().setWindowIcon(is1, is2);
                itlt.LOGGER.info("Set window icon without issues");
            }
        } catch (FileNotFoundException e) {
            itlt.LOGGER.error("Failed to open icon that we just confirmed was there???", e);
        } catch (IOException e) {
            itlt.LOGGER.error("Something went wrong when reading the icon file", e);
        }
    }

    public static void startUIProcess(final MessageContent messageContent) {
        final Path modFile = ModList.get().getModFileById("itlt").getFile().getFilePath();
        final String messageType, messageTitle, messageBody, guideURL, leftButtonText, middleButtonText, rightButtonText;

        switch (messageContent) {
            case NeedsJava64bit:
                messageType = "require";
                messageTitle = "64bit Java required"; // todo: i18n/multilingual support
                messageBody = "Whoops! This pack requires 64bit Java but you appear to be using 32bit Java.";
                if (ClientConfig.enableCustom64bitJavaGuide.get()) { guideURL = ClientConfig.custom64bitJavaGuideURL.get(); } else { guideURL = "https://ozli.ga"; }
                leftButtonText = "Get 64bit Java";
                middleButtonText = "Close";
                rightButtonText = ".";
                break;
            case WantsJava64bit:
                messageType = "warn";
                messageTitle = "64bit Java recommended";
                messageBody = "Warning: You appear to be using 32bit Java - this pack recommends 64bit Java for the best experience.";
                if (ClientConfig.enableCustom64bitJavaGuide.get()) { guideURL = ClientConfig.custom64bitJavaGuideURL.get(); } else { guideURL = "https://ozli.ga"; }
                leftButtonText = "Get 64bit Java";
                middleButtonText = "Ask later";
                rightButtonText = "Don't ask again";
                break;
            case NeedsMoreMemory:
                messageType = "require";
                messageTitle = "More allocated RAM required";
                messageBody = "Whoops! This pack requires at least " + ClientConfig.reqMinMemoryAmountInGB.get() + "GB but you appear to be allocating " + ClientEvents.currentMem + "GB of RAM.";
                if (ClientConfig.enableCustomMemoryAllocGuide.get()) { guideURL = ClientConfig.customMemoryAllocGuideURL.get(); } else { guideURL = "https://ozli.ga"; }
                leftButtonText = "Show guide";
                middleButtonText = "Close";
                rightButtonText = ".";
                break;
            case WantsMoreMemory:
                messageType = "require";
                messageTitle = "More allocated RAM recommended";
                messageBody = "Warning: For the best experience, this pack must have at least " + ClientConfig.warnMinMemoryAmountInGB.get() + "GB but you appear to be allocating " + ClientEvents.currentMem + "GB of RAM.";
                if (ClientConfig.enableCustomMemoryAllocGuide.get()) { guideURL = ClientConfig.customMemoryAllocGuideURL.get(); } else { guideURL = "https://ozli.ga"; }
                leftButtonText = "Show guide";
                middleButtonText = "Ask later";
                rightButtonText = "Don't ask again";
                break;
            case NeedsLessMemory:
                messageType = "require";
                messageTitle = "Less allocated RAM required";
                messageBody = "Whoops! This pack must not have more than " + ClientConfig.reqMaxMemoryAmountInGB.get() + "GB but you appear to be allocating " + ClientEvents.currentMem + "GB of RAM.";
                if (ClientConfig.enableCustomMemoryAllocGuide.get()) { guideURL = ClientConfig.customMemoryAllocGuideURL.get(); } else { guideURL = "https://ozli.ga"; }
                leftButtonText = "Show guide";
                middleButtonText = "Close";
                rightButtonText = ".";
                break;
            case WantsLessMemory:
                messageType = "warn";
                messageTitle = "Less allocated RAM recommended";
                messageBody = "Warning: For the best experience, this pack must not have more than " + ClientConfig.warnMaxMemoryAmountInGB.get() + "GB but you appear to be allocating " + ClientEvents.currentMem + "GB of RAM.";
                if (ClientConfig.enableCustomMemoryAllocGuide.get()) { guideURL = ClientConfig.customMemoryAllocGuideURL.get(); } else { guideURL = "https://ozli.ga"; }
                leftButtonText = "Show guide";
                middleButtonText = "Ask later";
                rightButtonText = "Don't ask again";
                break;
            case NeedsNewerJava:
                messageType = "require";
                messageTitle = "Java " + ClientConfig.requiredMinJavaVerion.get().toString() + "+ required";
                messageBody = "Whoops! This pack requires Java " + ClientConfig.requiredMinJavaVerion.get() + " or newer but you appear to be using Java " + ClientEvents.javaVerInt + ".";
                if (ClientConfig.enableCustom64bitJavaGuide.get()) { guideURL = ClientConfig.custom64bitJavaGuideURL.get(); } else { guideURL = "https://ozli.ga"; }
                leftButtonText = "Get newer Java";
                middleButtonText = "Close";
                rightButtonText = ".";
                break; // todo: WantsNewerJava
            case WantsNewerJava:
                messageType = "warn";
                messageTitle = "Java " + ClientConfig.requiredMinJavaVerion.get().toString() + "+ recommended";
                messageBody = "Warning: For the best experience, this pack recommends Java " + ClientConfig.requiredMinJavaVerion.get() + " or newer but you appear to be using Java " + ClientEvents.javaVerInt + ".";
                if (ClientConfig.enableCustom64bitJavaGuide.get()) { guideURL = ClientConfig.custom64bitJavaGuideURL.get(); } else { guideURL = "https://ozli.ga"; }
                leftButtonText = "Get newer Java";
                middleButtonText = "Ask later";
                rightButtonText = "Don't ask again";
                break;
            default:
                messageType = "";
                messageTitle = "";
                messageBody = "";
                guideURL = "";
                leftButtonText = "";
                middleButtonText = "";
                rightButtonText = "";
                break;
        }

        final String errorMessage = "Error: Unable to launch a web browser with the guide. The link is: " + guideURL;

        try {
            final ProcessBuilder builder = new ProcessBuilder(
                    System.getProperty("java.home") + File.separator + "bin" + File.separator + "java", "-jar", modFile.toString(),
                    messageType, messageTitle, messageBody, leftButtonText, middleButtonText, rightButtonText, errorMessage, guideURL);
            builder.inheritIO();
            builder.start();

            // don't allow the pack to continue launching if a requirement isn't met
            if (messageType.equals("require")) {
                System.exit(1);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

}
