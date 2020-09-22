package dk.zlepper.itlt.client.helpers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dk.zlepper.itlt.client.ClientConfig;
import dk.zlepper.itlt.client.ClientModEvents;
import dk.zlepper.itlt.itlt;
import io.seruco.encoding.base62.Base62;
import net.minecraft.client.Minecraft;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.fml.ModList;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.lktk.NativeBLAKE3;
import io.lktk.NativeBLAKE3Util;

public class ClientUtils {

    public static Object[] getFileChecksum(final File file) throws IOException, NoSuchAlgorithmException, NativeBLAKE3Util.InvalidNativeOutput {
        // for systems where the NativeBLAKE3 lib has not been compiled for, fallback to SHA-512
        if (!NativeBLAKE3.isEnabled())
            return getFileChecksumFallback(file);

        // setup the BLAKE3 hasher
        final NativeBLAKE3 hasher = new NativeBLAKE3();
        hasher.initDefault();

        // open the file to hash
        final FileInputStream fis = new FileInputStream(file);

        // create byte array to read data in chunks
        byte[] byteArray = new byte[1024];

        // read file data in chunks of 1KB and send it off to the hasher
        while ((fis.read(byteArray)) != -1) {
            hasher.update(byteArray);
        }

        // we're finished reading the file, so close it
        fis.close();

        // get the hasher's output
        final byte[] bytes = hasher.getOutput();

        // the NaitveBLAKE3 JNI is a C lib so we need to manually tell it to free up the memory now that we're done with it
        if (hasher.isValid())
            hasher.close();

        // convert to hex
        /*final StringBuilder sb = new StringBuilder();
        for (byte aByte : decimalBytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }*/

        // convert to Base64
        //final String encodedBase64 = Base64.encodeBase64String(bytes);

        // convert to Base62
        final Base62 base62 = Base62.createInstance();
        final String encodedBase62 = new String(base62.encode(bytes));

        return new Object[]{ChecksumType.Modern, encodedBase62};
    }

    public static Object[] getFileChecksumFallback(final File file) throws IOException, NoSuchAlgorithmException {
        final MessageDigest digest = MessageDigest.getInstance("SHA-512");
        final FileInputStream fis = new FileInputStream(file);

        byte[] byteArray = new byte[1024];
        int bytesCount;

        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }
        fis.close();

        // Get the hash's bytes
        byte[] bytes = digest.digest();

        // convert to hex
        /*final StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }*/

        // convert to Base64
        //final String encodedBase64 = Base64.encodeBase64String(bytes);

        // convert to Base62
        final Base62 base62 = Base62.createInstance();
        final String encodedBase62 = new String(base62.encode(bytes));

        // return the completed hash
        return new Object[]{ChecksumType.Fallback, encodedBase62};
    }

    public enum ChecksumType {
        Fallback,
        Modern
    }

    public static void setWindowIcon(final File icon, final Minecraft mcInstance) {
        try (final InputStream is1 = new FileInputStream(icon.getAbsoluteFile())) {
            try (final InputStream is2 = new FileInputStream(icon.getAbsoluteFile())) {
                mcInstance.getMainWindow().setWindowIcon(is1, is2);
                itlt.LOGGER.debug("Set window icon without issues");
            }
        } catch (final FileNotFoundException e) {
            itlt.LOGGER.error("Failed to open icon that we just confirmed was there???", e);
        } catch (final IOException e) {
            itlt.LOGGER.error("Something went wrong when reading the icon file", e);
        }
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
                messageBody = new TranslationTextComponent("itlt.java.memory.min.require.body").getUnformattedComponentText().replaceFirst("%s", ClientConfig.reqMinMemoryAmountInGB.get().toString()).replaceFirst("%sb", String.valueOf(ClientModEvents.currentMem));
                if (ClientConfig.enableCustomMemoryAllocGuide.get()) { guideURL = ClientConfig.customMemoryAllocGuideURL.get(); } else { guideURL = "https://ozli.ga"; }
                leftButtonText = new TranslationTextComponent("itlt.java.memory.min.require.guideButtonText").getUnformattedComponentText();
                middleButtonText = new TranslationTextComponent("itlt.java.memory.min.require.closeButtonText").getUnformattedComponentText();
                rightButtonText = ".";
                break;
            case WantsMoreMemory:
                messageType = "warn";
                messageTitle = new TranslationTextComponent("itlt.java.memory.min.warning.title").getUnformattedComponentText();
                messageBody = new TranslationTextComponent("itlt.java.memory.min.warning.body").getUnformattedComponentText().replaceFirst("%s", ClientConfig.warnMinMemoryAmountInGB.get().toString()).replaceFirst("%sb", String.valueOf(ClientModEvents.currentMem));
                if (ClientConfig.enableCustomMemoryAllocGuide.get()) { guideURL = ClientConfig.customMemoryAllocGuideURL.get(); } else { guideURL = "https://ozli.ga"; }
                leftButtonText = new TranslationTextComponent("itlt.java.memory.min.warning.guideButtonText").getUnformattedComponentText();
                middleButtonText = new TranslationTextComponent("itlt.java.memory.min.warning.askLaterButtonText").getUnformattedComponentText();
                rightButtonText = new TranslationTextComponent("itlt.java.memory.min.warning.dontAskAgainButtonText").getUnformattedComponentText();
                break;
            case NeedsLessMemory:
                messageType = "require";
                messageTitle = new TranslationTextComponent("itlt.java.memory.max.require.title").getUnformattedComponentText();
                messageBody = new TranslationTextComponent("itlt.java.memory.max.require.body").getUnformattedComponentText().replaceFirst("%s", ClientConfig.reqMaxMemoryAmountInGB.get().toString()).replaceFirst("%sb", String.valueOf(ClientModEvents.currentMem));
                if (ClientConfig.enableCustomMemoryAllocGuide.get()) { guideURL = ClientConfig.customMemoryAllocGuideURL.get(); } else { guideURL = "https://ozli.ga"; }
                leftButtonText = new TranslationTextComponent("itlt.java.memory.max.require.guideButtonText").getUnformattedComponentText();
                middleButtonText = new TranslationTextComponent("itlt.java.memory.max.require.closeButtonText").getUnformattedComponentText();
                rightButtonText = ".";
                break;
            case WantsLessMemory:
                messageType = "warn";
                messageTitle = new TranslationTextComponent("itlt.java.memory.max.warning.title").getUnformattedComponentText();
                messageBody = new TranslationTextComponent("itlt.java.memory.max.warning.body").getUnformattedComponentText().replaceFirst("%s", ClientConfig.warnMaxMemoryAmountInGB.get().toString()).replaceFirst("%sb", String.valueOf(ClientModEvents.currentMem));
                if (ClientConfig.enableCustomMemoryAllocGuide.get()) { guideURL = ClientConfig.customMemoryAllocGuideURL.get(); } else { guideURL = "https://ozli.ga"; }
                leftButtonText = new TranslationTextComponent("itlt.java.memory.max.warning.guideButtonText").getUnformattedComponentText();
                middleButtonText = new TranslationTextComponent("itlt.java.memory.max.warning.askLaterButtonText").getUnformattedComponentText();
                rightButtonText = new TranslationTextComponent("itlt.java.memory.max.warning.dontAskAgainButtonText").getUnformattedComponentText();
                break;
            case NeedsNewerJava:
                messageType = "require";
                messageTitle = new TranslationTextComponent("itlt.java.version.require.title").getUnformattedComponentText();
                messageBody = new TranslationTextComponent("itlt.java.version.require.body").getUnformattedComponentText().replaceFirst("%s", ClientConfig.requiredMinJavaVersion.get().toString()).replaceFirst("%sb", String.valueOf(ClientModEvents.javaVerInt));
                if (ClientConfig.enableCustomJavaUpgradeGuide.get()) { guideURL = ClientConfig.customJavaUpgradeGuideURL.get(); } else { guideURL = "https://ozli.ga"; }
                leftButtonText = new TranslationTextComponent("itlt.java.version.require.guideButtonText").getUnformattedComponentText();
                middleButtonText = new TranslationTextComponent("itlt.java.version.require.closeButtonText").getUnformattedComponentText();
                rightButtonText = ".";
                break;
            case WantsNewerJava:
                messageType = "warn";
                messageTitle = new TranslationTextComponent("itlt.java.version.warning.title").getUnformattedComponentText();
                messageBody = new TranslationTextComponent("itlt.java.version.warning.body").getUnformattedComponentText().replaceFirst("%s", ClientConfig.warnMinJavaVersion.get().toString()).replaceFirst("%sb", String.valueOf(ClientModEvents.javaVerInt));
                if (ClientConfig.enableCustomJavaUpgradeGuide.get()) { guideURL = ClientConfig.customJavaUpgradeGuideURL.get(); } else { guideURL = "https://ozli.ga"; }
                leftButtonText = new TranslationTextComponent("itlt.java.version.warning.guideButtonText").getUnformattedComponentText();
                middleButtonText = new TranslationTextComponent("itlt.java.version.warning.askLaterButtonText").getUnformattedComponentText();
                rightButtonText = new TranslationTextComponent("itlt.java.version.warning.dontAskAgainButtonText").getUnformattedComponentText();
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

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getLatestDefinitions(final Minecraft mcInstance, final ChecksumType checksumType) throws IOException, ClassCastException {
        final String definitionsURLString;
        if (checksumType == ChecksumType.Modern) definitionsURLString = "https://raw.githubusercontent.com/zlepper/itlt/1.16-2.0-rewrite/definitionsAPI/v1/definitions.json";
        else definitionsURLString = "https://raw.githubusercontent.com/zlepper/itlt/1.16-2.0-rewrite/definitionsAPI/v1/definitions-fallback.json";

        // download the definitions and put it in the string "definitionsJson"
        final URLConnection connection = new URL(definitionsURLString).openConnection();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        final String definitionsJson = reader.lines().collect(Collectors.joining("\n"));

        // convert the definitionsJson string to a Map
        final Type type = new TypeToken<Map<String, Object>>(){}.getType();
        final Map<String, Object> definitionsMap = new Gson().fromJson(definitionsJson, type);

        // trim off the version patch number, accounting for cases like 1.17.10 that have a double-digit patch number
        String mcVersion = SharedConstants.getVersion().getName();
        final String[] splitMcVersion = mcVersion.split(Pattern.quote("."));
        mcVersion = splitMcVersion[0] + "." + splitMcVersion[1];

        itlt.LOGGER.debug("mcVersion: " + mcVersion);
        itlt.LOGGER.debug("definitionsMap: " + definitionsMap.toString());

        return Collections.unmodifiableMap((Map<String, Object>) definitionsMap.getOrDefault(mcVersion, Collections.<String, Object>emptyMap()));

        /* The definitions JSON looks something like this:
         *  {
         *      "1.16": {
         *          "modIds": [ "modid1", "modid2", "modid3" ],
         *          "checksums": [ "aabc", "e102", "8fc2" ]
         *      },
         *      "1.15": {
         *          (...)
         *      }
         *  }
         *
         * Patch releases such as 1.16.1, 1.16.2, etc... all point to 1.16
         */
    }
}
