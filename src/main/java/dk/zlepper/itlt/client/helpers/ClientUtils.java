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
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import net.sf.image4j.codec.ico.ICODecoder;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.formats.icns.IcnsImageParser;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryStack;

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
        public String name, address;
        public boolean forceResourcePack;
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

        final String defaultGuideURL = "https://zlepper.github.io/itlt/guide?launcher=%launcher&reason=%reason&type=%type&desire=%desire&subject=%subject";
        String guideURL;
        switch (messageContent.msgSubject) {
            case Memory:
                if (ClientConfig.enableCustomMemoryAllocGuide.get()) guideURL = ClientConfig.customMemoryAllocGuideURL.get();
                else guideURL = defaultGuideURL;
                break;
            case Java:
                if (messageContent.msgDesire == Message.Desire.SixtyFourBit) {
                    if (ClientConfig.enableCustom64bitJavaGuide.get()) guideURL = ClientConfig.custom64bitJavaGuideURL.get();
                    else guideURL = defaultGuideURL;
                } else if (messageContent.msgDesire == Message.Desire.Newer) {
                    if (ClientConfig.enableCustomJavaUpgradeGuide.get()) guideURL = ClientConfig.customJavaUpgradeGuideURL.get();
                    else guideURL = defaultGuideURL;
                } else {
                    if (ClientConfig.enableCustomJavaDowngradeGuide.get()) guideURL = ClientConfig.customJavaDowngradeGuideURL.get();
                    else guideURL = defaultGuideURL;
                }
                break;
            default:
                guideURL = "N/A";
                break;
        }
        guideURL = guideURL.replaceAll("%launcher", ClientModEvents.detectedLauncher.getName())
                .replaceAll("%reason", messageContent.toString())
                .replaceAll("%type", messageContent.msgType.toString())
                .replaceAll("%desire", messageContent.msgDesire.toString())
                .replaceAll("%subject", messageContent.msgSubject.toString());

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
                messageBody = messageBody.replaceFirst("%sb", ClientConfig.getSimplifiedFloatStr(ClientModEvents.currentMem));
            } else if (messageContent.msgSubject == Message.Subject.Java) {
                messageBody = messageBody.replaceFirst("%sb", String.valueOf(getJavaVersion()));
            }

            switch (messageContent) {
                case NeedsMoreMemory:
                    messageBody = messageBody.replaceFirst("%s",
                            ClientConfig.getSimplifiedFloatStr(ClientConfig.reqMinMemoryAmountInGB.get().floatValue()));
                    break;
                case WantsMoreMemory:
                    messageBody = messageBody.replaceFirst("%s",
                            ClientConfig.getSimplifiedFloatStr(ClientConfig.warnMinMemoryAmountInGB.get().floatValue()));
                    break;
                case NeedsLessMemory:
                    messageBody = messageBody.replaceFirst("%s",
                            ClientConfig.getSimplifiedFloatStr(ClientConfig.reqMaxMemoryAmountInGB.get().floatValue()));
                    break;
                case WantsLessMemory:
                    messageBody = messageBody.replaceFirst("%s",
                            ClientConfig.getSimplifiedFloatStr(ClientConfig.warnMaxMemoryAmountInGB.get().floatValue()));
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
                    break;
                case WantsOlderJava:
                    messageTitle = messageTitle.replaceFirst("%s", ClientConfig.warnMaxJavaVersion.get().toString());
                    messageBody = messageBody.replaceFirst("%s", ClientConfig.warnMaxJavaVersion.get().toString());
                    break;
                default:
                    break;
            }
        }

        messageGuideError = messageGuideError.replaceFirst("%s", guideURL);

        itlt.LOGGER.info("messageContent: " + messageContent.toString());
        itlt.LOGGER.debug("messageType: " + messageContent.msgType.toString());
        itlt.LOGGER.info("messageTitle: " + messageTitle);
        itlt.LOGGER.info("messageBody: " + messageBody);
        itlt.LOGGER.info("guideURL: " + guideURL);
        itlt.LOGGER.debug("messageGuideError: " + messageGuideError);
        itlt.LOGGER.debug("left: " + leftButtonText);
        itlt.LOGGER.debug("middle: " + middleButtonText);
        itlt.LOGGER.debug("right: " + rightButtonText);

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
                itlt.LOGGER.fatal("The unmet requirement is: \"" + messageContent + "\".");
                if (messageBody.isEmpty())
                    itlt.LOGGER.error("The requirement details are blank, please report this bug on itlt's GitHub issues");
                else itlt.LOGGER.error("Requirement details: " + messageBody);

                System.exit(1);
            }
        }
    }

    public static String readString(final Path path) throws IOException {
        return new String(Files.readAllBytes(path));
    }
}
