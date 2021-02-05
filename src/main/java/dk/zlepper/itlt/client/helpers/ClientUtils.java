package dk.zlepper.itlt.client.helpers;

import dk.zlepper.itlt.client.ClientConfig;
import dk.zlepper.itlt.client.ClientModEvents;
import dk.zlepper.itlt.itlt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.ModList;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;

import org.imgscalr.Scalr;

import javax.imageio.ImageIO;

public class ClientUtils {

    public static class CustomServerData {
        public String name, IP;
        public Boolean forceResourcePack;
    }

    public static boolean alreadyInServerList(final ServerData server, final ServerList list) {
        if (list == null)
            return false;

        for (int i = 0; i < list.countServers(); i++) {
            final ServerData serverData = list.getServerData(i);
            if (serverData.serverName != null && serverData.serverIP != null &&
                    serverData.serverName.equalsIgnoreCase(server.serverName) &&
                    serverData.serverIP.equalsIgnoreCase(server.serverIP)) {
                return true;
            }
        }

        return false;
    }

    public static void setWindowIcon(final File inputIconFile, final Minecraft mcInstance) throws IOException {
        final BufferedImage inputIcon = ImageIO.read(inputIconFile);

        // make the 16x16 icon
        final BufferedImage resizedSmall = Scalr.resize(inputIcon, 16, 16);

        final int inputWidth = inputIcon.getWidth();
        final int inputHeight = inputIcon.getHeight();

        /* this ensures that icons are the right size, resizing to the middle of a boundary if necessary
         * here's some examples...
         * - a 28x31 icon would be resized to 32x32 as the longest side (31px in this case) is between 24 and 48
         * - a 64x64 icon would be used directly as it's already a valid size and aspect ratio
         * - a 48x32 icon would be resized to 48x48 as its longest side (48px) is between 32 and 64
         * - a 140x100 icon would be resized to 128x128 as one of its sides is over 128px
         * - a tiny 12x12 icon would be resized to 16x16 as both sides are under 16px
         * note that the resizing should maintain the existing aspect ratios - a weird 16:9 icon will still look like
         * 16:9 but in a square file and be visually small as a result
         */
        final BufferedImage resizedLarge;
        if ((inputWidth == 128 && inputHeight == 128) || (inputWidth == 96 && inputHeight == 96) ||
            (inputWidth == 64 && inputHeight == 64)   || (inputWidth == 48 && inputHeight == 48) ||
            (inputWidth == 32 && inputHeight == 32)   || (inputWidth == 24 && inputHeight == 24) ||
            (inputWidth == 16 && inputHeight == 16)) {
            // if the icon is already a valid size and aspect ratio, use it directly without resizing
            resizedLarge = inputIcon;
        } else if (inputWidth > 128 || inputHeight > 128)
            // one of the sides are too big, resize to 128px²
            resizedLarge = Scalr.resize(inputIcon, 128, 128);
        else if ((inputWidth > 64 && inputWidth < 128) || (inputHeight > 64 && inputHeight < 128))
            // if a side is between 64 and 128px, resize to 96px²
            resizedLarge = Scalr.resize(inputIcon, 96, 96);
        else if ((inputWidth > 48 && inputWidth < 96) || (inputHeight > 48 && inputHeight < 96))
            // if a side is between 48 and 96px, resize to 64px²
            resizedLarge = Scalr.resize(inputIcon, 64, 64);
        else if ((inputWidth > 32 && inputWidth < 64) || (inputHeight > 32 && inputHeight < 64))
            // etc...
            resizedLarge = Scalr.resize(inputIcon, 48, 48);
        else if ((inputWidth > 24 && inputWidth < 48) || (inputHeight > 24 && inputHeight < 48))
            resizedLarge = Scalr.resize(inputIcon, 32, 32);
        else if ((inputWidth > 16 && inputWidth < 32) || (inputHeight > 16 && inputHeight < 32))
            resizedLarge = Scalr.resize(inputIcon, 24, 24);
        else
            // if both sides are <=16px, resize to 16px²
            resizedLarge = Scalr.resize(inputIcon, 16, 16);

        inputIcon.flush();

        mcInstance.getMainWindow().setWindowIcon(convertToInputStream(resizedSmall), convertToInputStream(resizedLarge));
    }

    public static ByteArrayInputStream convertToInputStream(final BufferedImage bufferedImage) throws IOException {
        // convert BufferedImage to ByteArrayOutputStream without a double copy behind the scenes
        // https://stackoverflow.com/a/12253091/3944931
        final ByteArrayOutputStream output = new ByteArrayOutputStream() {
            @Override
            public synchronized byte[] toByteArray() {
                return this.buf;
            }
        };
        ImageIO.write(bufferedImage, "png", output);
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray(), 0, output.size());
        bufferedImage.flush();
        return inputStream;
    }

    public static void startUIProcess(final MessageContent messageContent) {
        final Path modFile = ModList.get().getModFileById("itlt").getFile().getFilePath();
        final String messageType, messageTitle, messageBody, guideURL, leftButtonText, middleButtonText, rightButtonText;

        switch (messageContent) {
            case NeedsJava64bit:
                messageType = "require";
                messageTitle = new TranslationTextComponent("itlt.java.arch.require.title").getUnformattedComponentText();
                messageBody = new TranslationTextComponent("itlt.java.arch.require.body").getUnformattedComponentText();
                if (ClientConfig.enableCustom64bitJavaGuide.get()) { guideURL = ClientConfig.custom64bitJavaGuideURL.get(); } else { guideURL = "https://ozli.ga"; }
                leftButtonText = new TranslationTextComponent("itlt.java.arch.require.guideButtonText").getUnformattedComponentText();
                middleButtonText = new TranslationTextComponent("itlt.java.arch.require.closeButtonText").getUnformattedComponentText();
                rightButtonText = ".";
                break;
            case WantsJava64bit:
                messageType = "warn";
                messageTitle = new TranslationTextComponent("itlt.java.arch.warning.title").getUnformattedComponentText();
                messageBody = new TranslationTextComponent("itlt.java.arch.warning.body").getUnformattedComponentText();
                if (ClientConfig.enableCustom64bitJavaGuide.get()) { guideURL = ClientConfig.custom64bitJavaGuideURL.get(); } else { guideURL = "https://ozli.ga"; }
                leftButtonText = new TranslationTextComponent("itlt.java.arch.warning.guideButtonText").getUnformattedComponentText();
                middleButtonText = new TranslationTextComponent("itlt.java.arch.warning.askLaterButtonText").getUnformattedComponentText();
                rightButtonText = new TranslationTextComponent("itlt.java.arch.warning.dontAskAgainButtonText").getUnformattedComponentText();
                break;
            case NeedsMoreMemory:
                messageType = "require";
                messageTitle = new TranslationTextComponent("itlt.java.memory.min.require.title").getUnformattedComponentText();
                messageBody = new TranslationTextComponent("itlt.java.memory.min.require.body").getUnformattedComponentText()
                        .replaceFirst("%s", ClientConfig.reqMinMemoryAmountInGB.get().toString()).replaceFirst("%sb", String.valueOf(ClientModEvents.currentMem));
                if (ClientConfig.enableCustomMemoryAllocGuide.get()) { guideURL = ClientConfig.customMemoryAllocGuideURL.get(); } else { guideURL = "https://ozli.ga"; }
                leftButtonText = new TranslationTextComponent("itlt.java.memory.min.require.guideButtonText").getUnformattedComponentText();
                middleButtonText = new TranslationTextComponent("itlt.java.memory.min.require.closeButtonText").getUnformattedComponentText();
                rightButtonText = ".";
                break;
            case WantsMoreMemory:
                messageType = "warn";
                messageTitle = new TranslationTextComponent("itlt.java.memory.min.warning.title").getUnformattedComponentText();
                messageBody = new TranslationTextComponent("itlt.java.memory.min.warning.body").getUnformattedComponentText()
                        .replaceFirst("%s", ClientConfig.warnMinMemoryAmountInGB.get().toString()).replaceFirst("%sb", String.valueOf(ClientModEvents.currentMem));
                if (ClientConfig.enableCustomMemoryAllocGuide.get()) { guideURL = ClientConfig.customMemoryAllocGuideURL.get(); } else { guideURL = "https://ozli.ga"; }
                leftButtonText = new TranslationTextComponent("itlt.java.memory.min.warning.guideButtonText").getUnformattedComponentText();
                middleButtonText = new TranslationTextComponent("itlt.java.memory.min.warning.askLaterButtonText").getUnformattedComponentText();
                rightButtonText = new TranslationTextComponent("itlt.java.memory.min.warning.dontAskAgainButtonText").getUnformattedComponentText();
                break;
            case NeedsLessMemory:
                messageType = "require";
                messageTitle = new TranslationTextComponent("itlt.java.memory.max.require.title").getUnformattedComponentText();
                messageBody = new TranslationTextComponent("itlt.java.memory.max.require.body").getUnformattedComponentText()
                        .replaceFirst("%s", ClientConfig.reqMaxMemoryAmountInGB.get().toString()).replaceFirst("%sb", String.valueOf(ClientModEvents.currentMem));
                if (ClientConfig.enableCustomMemoryAllocGuide.get()) { guideURL = ClientConfig.customMemoryAllocGuideURL.get(); } else { guideURL = "https://ozli.ga"; }
                leftButtonText = new TranslationTextComponent("itlt.java.memory.max.require.guideButtonText").getUnformattedComponentText();
                middleButtonText = new TranslationTextComponent("itlt.java.memory.max.require.closeButtonText").getUnformattedComponentText();
                rightButtonText = ".";
                break;
            case WantsLessMemory:
                messageType = "warn";
                messageTitle = new TranslationTextComponent("itlt.java.memory.max.warning.title").getUnformattedComponentText();
                messageBody = new TranslationTextComponent("itlt.java.memory.max.warning.body").getUnformattedComponentText()
                        .replaceFirst("%s", ClientConfig.warnMaxMemoryAmountInGB.get().toString()).replaceFirst("%sb", String.valueOf(ClientModEvents.currentMem));
                if (ClientConfig.enableCustomMemoryAllocGuide.get()) { guideURL = ClientConfig.customMemoryAllocGuideURL.get(); } else { guideURL = "https://ozli.ga"; }
                leftButtonText = new TranslationTextComponent("itlt.java.memory.max.warning.guideButtonText").getUnformattedComponentText();
                middleButtonText = new TranslationTextComponent("itlt.java.memory.max.warning.askLaterButtonText").getUnformattedComponentText();
                rightButtonText = new TranslationTextComponent("itlt.java.memory.max.warning.dontAskAgainButtonText").getUnformattedComponentText();
                break;
            case NeedsNewerJava:
                messageType = "require";
                messageTitle = new TranslationTextComponent("itlt.java.version.require.title").getUnformattedComponentText();
                messageBody = new TranslationTextComponent("itlt.java.version.require.body").getUnformattedComponentText()
                        .replaceFirst("%s", ClientConfig.requiredMinJavaVersion.get().toString()).replaceFirst("%sb", String.valueOf(ClientModEvents.javaVerInt));
                if (ClientConfig.enableCustomJavaUpgradeGuide.get()) { guideURL = ClientConfig.customJavaUpgradeGuideURL.get(); } else { guideURL = "https://ozli.ga"; }
                leftButtonText = new TranslationTextComponent("itlt.java.version.require.guideButtonText").getUnformattedComponentText();
                middleButtonText = new TranslationTextComponent("itlt.java.version.require.closeButtonText").getUnformattedComponentText();
                rightButtonText = ".";
                break;
            case WantsNewerJava:
                messageType = "warn";
                messageTitle = new TranslationTextComponent("itlt.java.version.warning.title").getUnformattedComponentText();
                messageBody = new TranslationTextComponent("itlt.java.version.warning.body").getUnformattedComponentText()
                        .replaceFirst("%s", ClientConfig.warnMinJavaVersion.get().toString()).replaceFirst("%sb", String.valueOf(ClientModEvents.javaVerInt));
                if (ClientConfig.enableCustomJavaUpgradeGuide.get()) { guideURL = ClientConfig.customJavaUpgradeGuideURL.get(); } else { guideURL = "https://ozli.ga"; }
                leftButtonText = new TranslationTextComponent("itlt.java.version.warning.guideButtonText").getUnformattedComponentText();
                middleButtonText = new TranslationTextComponent("itlt.java.version.warning.askLaterButtonText").getUnformattedComponentText();
                rightButtonText = new TranslationTextComponent("itlt.java.version.warning.dontAskAgainButtonText").getUnformattedComponentText();
                break;
            default:
                messageType = ".";
                messageTitle = ".";
                messageBody = ".";
                guideURL = ".";
                leftButtonText = ".";
                middleButtonText = ".";
                rightButtonText = ".";
                break;
        }

        // todo: Also show this error message in the GUI
        final String errorMessage = "Error: Unable to launch a web browser with the guide. The link is: " + guideURL;

        try {
            final ProcessBuilder builder = new ProcessBuilder(
                    System.getProperty("java.home") + File.separator + "bin" + File.separator + "java",
                    "-jar", modFile.toString(), messageType, messageTitle, messageBody, leftButtonText,
                    middleButtonText, rightButtonText, errorMessage, guideURL);
            builder.inheritIO();
            builder.start();
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            // don't allow the pack to continue launching if a requirement isn't met
            if (messageType.equals("require")) {
                itlt.LOGGER.fatal("Can't launch the game as a requirement isn't met. ");
                itlt.LOGGER.fatal("The unmet requirement is: \"" + messageContent.toString() + "\".");

                System.exit(1);
            }
        }
    }
}
