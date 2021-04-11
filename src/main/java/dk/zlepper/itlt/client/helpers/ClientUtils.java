package dk.zlepper.itlt.client.helpers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dk.zlepper.itlt.client.ClientConfig;
import dk.zlepper.itlt.client.ClientModEvents;
import dk.zlepper.itlt.itlt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraftforge.fml.ModList;

import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.imgscalr.Scalr;

import javax.imageio.ImageIO;

public class ClientUtils {

    public static byte getJavaVersion() {
        final String javaVerStr = System.getProperty("java.version");
        final String[] splitJavaVer = javaVerStr.split(Pattern.quote("."));
        byte javaVer = Byte.parseByte(splitJavaVer[0]);

        // account for older Java versions that advertise as "1.8" instead of "8".
        if (javaVer == 1) javaVer = Byte.parseByte(splitJavaVer[1]);

        return javaVer;
    }

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
        // convert BufferedImage to ByteArrayOutputStream without a double copy behind the scenes, only for our
        // specific use-case. https://stackoverflow.com/a/12253091/3944931
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

    public static void startUIProcess(final Message.Content messageContent) {
        final Path modFile = ModList.get().getModFileById("itlt").getFile().getFilePath();
        String messageTitle, messageBody, leftButtonText, middleButtonText, rightButtonText, messageGuideError;

        final String msgTranslationKeyTemplate =
                "itlt." + messageContent.msgType.name().toLowerCase()
                        + "." + messageContent.msgDesire.name().toLowerCase()
                        + "." + messageContent.msgSubject.name().toLowerCase();
        messageTitle = msgTranslationKeyTemplate + ".title";
        messageBody = msgTranslationKeyTemplate + ".body";
        messageGuideError = "itlt.cantOpenGuideErrorMsg";

        leftButtonText = msgTranslationKeyTemplate + ".guideButtonText";
        if (messageContent.msgType == Message.Type.Needs) {
            // Requirement messages only have two buttons: show guide and close
            middleButtonText = msgTranslationKeyTemplate + ".closeButtonText";
            rightButtonText = ".";
        } else {
            // Warning messages have three buttons: show guide, ask later and don't ask again
            middleButtonText = msgTranslationKeyTemplate + ".askLaterButtonText";
            rightButtonText = msgTranslationKeyTemplate + ".dontAskAgainButtonText";
        }

        final String guideURL;
        switch (messageContent.msgSubject) {
            case Memory:
                if (ClientConfig.enableCustomMemoryAllocGuide.get()) guideURL = ClientConfig.customMemoryAllocGuideURL.get();
                else guideURL = "https://ozli.ga";
                break;
            case Java:
                if (messageContent.msgDesire == Message.Desire.SixtyFourBit) {
                    if (ClientConfig.enableCustom64bitJavaGuide.get()) guideURL = ClientConfig.custom64bitJavaGuideURL.get();
                    else guideURL = "https://ozli.ga";
                } else if (messageContent.msgDesire == Message.Desire.Newer) {
                    if (ClientConfig.enableCustomJavaUpgradeGuide.get()) guideURL = ClientConfig.customJavaUpgradeGuideURL.get();
                    else guideURL = "https://ozli.ga";
                } else {
                    if (ClientConfig.enableCustomJavaDowngradeGuide.get()) guideURL = ClientConfig.customJavaDowngradeGuideURL.get();
                    else guideURL = "https://ozli.ga";
                }
                break;
            default:
                guideURL = "N/A";
                break;
        }

        // translate the keys manually as they aren't able to be translated by the game until after the main menu's shown

        // determine which language json we need to read
        final String lang = Minecraft.getInstance().gameSettings.language;

        // read the determined language json embedded inside the itlt jar, falling back to en_us if not found (to match vanilla behaviour)
        InputStream embeddedLangJsonInStream = itlt.class.getClassLoader().getResourceAsStream("assets/itlt/lang/" + lang + ".json");
        if (embeddedLangJsonInStream == null)
            embeddedLangJsonInStream = itlt.class.getClassLoader().getResourceAsStream("assets/itlt/lang/en_us.json");

        // todo: null check
        final Reader embeddedLangJson = new InputStreamReader(embeddedLangJsonInStream, StandardCharsets.UTF_8);

        // read the json and query what is currently lang keys for each variable
        // (e.g. messageTitle = "itlt.java.version.warning.title")
        final Type type = new TypeToken<HashMap<String, String>>(){}.getType();
        final HashMap<String, String> translationsMap = new Gson().fromJson(embeddedLangJson, type);
        if (translationsMap != null) {
            // replace the contents of each variable with the associated key's value,
            // falling back to the lang's key name if it fails
            messageTitle = translationsMap.getOrDefault(messageTitle, messageTitle);
            messageBody = translationsMap.getOrDefault(messageBody, messageBody);
            leftButtonText = translationsMap.getOrDefault(leftButtonText, leftButtonText);
            middleButtonText = translationsMap.getOrDefault(middleButtonText, middleButtonText);
            rightButtonText = translationsMap.getOrDefault(rightButtonText, rightButtonText);
            messageGuideError = translationsMap.getOrDefault(messageGuideError, messageGuideError);

            // insert some values where they're needed
            if (messageContent.msgSubject == Message.Subject.Memory) {
                messageBody = messageBody.replaceFirst("%sb", ClientConfig.getSimplifiedDoubleStr(ClientModEvents.currentMem));
            } else if (messageContent.msgSubject == Message.Subject.Java) {
                messageBody = messageBody.replaceFirst("%sb", String.valueOf(getJavaVersion()));
            }

            switch (messageContent) {
                case NeedsMoreMemory:
                    messageBody = messageBody.replaceFirst("%s",
                            ClientConfig.getSimplifiedDoubleStr(ClientConfig.reqMinMemoryAmountInGB.get()));
                    break;
                case WantsMoreMemory:
                    messageBody = messageBody.replaceFirst("%s",
                            ClientConfig.getSimplifiedDoubleStr(ClientConfig.warnMinMemoryAmountInGB.get()));
                    break;
                case NeedsLessMemory:
                    messageBody = messageBody.replaceFirst("%s",
                            ClientConfig.getSimplifiedDoubleStr(ClientConfig.reqMaxMemoryAmountInGB.get()));
                    break;
                case WantsLessMemory:
                    messageBody = messageBody.replaceFirst("%s",
                            ClientConfig.getSimplifiedDoubleStr(ClientConfig.warnMaxMemoryAmountInGB.get()));
                    break;
                case NeedsNewerJava:
                    messageTitle = messageTitle.replaceFirst("%s", ClientConfig.requiredMinJavaVersion.get().toString());
                    messageBody = messageBody.replaceFirst("%s", ClientConfig.requiredMinJavaVersion.get().toString());
                    break;
                case WantsNewerJava:
                    messageTitle = messageTitle.replaceFirst("%s", ClientConfig.warnMinJavaVersion.get().toString());
                    messageBody = messageBody.replaceFirst("%s", ClientConfig.warnMinJavaVersion.get().toString());
                    break;
                case NeedsOlderJava:
                    messageTitle = messageTitle.replaceFirst("%s", ClientConfig.requiredMaxJavaVersion.get().toString());
                    messageBody = messageBody.replaceFirst("%s", ClientConfig.requiredMaxJavaVersion.get().toString());
                case WantsOlderJava:
                    messageTitle = messageTitle.replaceFirst("%s", ClientConfig.warnMaxJavaVersion.get().toString());
                    messageBody = messageBody.replaceFirst("%s", ClientConfig.warnMaxJavaVersion.get().toString());
                default:
                    break;
            }
        }

        messageGuideError = messageGuideError.replaceFirst("%s", guideURL);

        itlt.LOGGER.info("messageContent: " + messageContent.toString());
        itlt.LOGGER.info("messageType: " + messageContent.msgType.toString());
        itlt.LOGGER.info("messageTitle: " + messageTitle);
        itlt.LOGGER.info("messageBody: " + messageBody);
        itlt.LOGGER.info("guideURL: " + guideURL);
        itlt.LOGGER.info("messageGuideError: " + messageGuideError);
        itlt.LOGGER.info("left: " + leftButtonText);
        itlt.LOGGER.info("middle: " + middleButtonText);
        itlt.LOGGER.info("right: " + rightButtonText);

        try {
            final ProcessBuilder builder = new ProcessBuilder(
                    System.getProperty("java.home") + File.separator + "bin" + File.separator + "java",
                    "-Dapple.awt.application.appearance=system", // macOS dark theme support in Java 14+ (JDK-8235363)
                    "-jar", modFile.toString(), messageContent.msgType.toString().toLowerCase(), messageTitle, messageBody,
                    leftButtonText, middleButtonText, rightButtonText, messageGuideError, guideURL, messageContent.toString());
            builder.inheritIO();
            builder.start();
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            // don't allow the pack to continue launching if a requirement isn't met
            if (messageContent.msgType == Message.Type.Needs) {
                itlt.LOGGER.fatal("Can't launch the game as a requirement isn't met. ");
                itlt.LOGGER.fatal("The unmet requirement is: \"" + messageContent.toString() + "\".");
                if (messageBody.isEmpty())
                    itlt.LOGGER.error("The requirement details are blank, please report this bug on itlt's GitHub issues");
                else itlt.LOGGER.error("Requirement details: " + messageBody);

                System.exit(1);
            }
        }
    }

    // Use Java 11's more efficient Files.readString() if available with a fallback to `new String(Files.readAllBytes(path))`
    public static String readString(final Path path) throws IOException {
        // If you're a dev getting a build error here, build with JDK 11+ and set the language level to 8.
        // As long as the lang level is still 8 it'll run fine on Java 8 - you just need 11+ javac to build
        if (getJavaVersion() >= 11) return Files.readString(path);
        else return new String(Files.readAllBytes(path));
    }
}
