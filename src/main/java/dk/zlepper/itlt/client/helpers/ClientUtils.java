package dk.zlepper.itlt.client.helpers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dk.zlepper.itlt.client.ClientConfig;
import dk.zlepper.itlt.client.ClientModEvents;
import dk.zlepper.itlt.itlt;
import io.github.rctcwyvrn.blake3.Blake3;
import io.seruco.encoding.base62.Base62;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.ModList;

import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.lktk.NativeBLAKE3;
import io.lktk.NativeBLAKE3Util;
import net.minecraftforge.userdev.FMLDevClientLaunchProvider;
import net.minecraftforge.userdev.FMLDevServerLaunchProvider;
import org.apache.commons.lang3.tuple.Pair;
import org.imgscalr.Scalr;

import javax.annotation.Nullable;
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

    private static Path getItltJarPath() {
        final Path itltJarPath = ModList.get().getModFileById("itlt").getFile().getFilePath();
        itlt.LOGGER.debug("itltJarPath: " + itltJarPath); // should be something like ???\mcRoot\mods\itlt.jar
        return itltJarPath;
    }

    public static LauncherName detectLauncher() {
        final Path itltJarPath = getItltJarPath();

        // jumping up a few directories should theoretically take us out of the mods folder and into the root folder of
        // the Twitch launcher. If the file "installedPacks" exists in this folder and there's also a folder called
        // "modpacks" in here, we know we're running within a Technic Launcher modpack
        final Path theoreticalTechnicPath = itltJarPath.getParent().getParent().getParent().getParent();
        final boolean isTechnicLauncher =
                Files.exists(theoreticalTechnicPath.resolve("installedPacks")) &&
                Files.exists(theoreticalTechnicPath.resolve("modpacks"));
        itlt.LOGGER.debug("theoreticalTechnicPath: " + theoreticalTechnicPath);
        itlt.LOGGER.debug("isTechnicLauncher: " + isTechnicLauncher);
        if (isTechnicLauncher) return LauncherName.Technic;

        // if the .minecraft folder has mmc-pack.json and instance.cfg files beside it then we know we're running
        // within a MultiMC modpack.
        final Path theoreticalMultiMCPath = itltJarPath.getParent().getParent().getParent();
        final boolean isMultiMCLauncher =
                Files.exists(theoreticalMultiMCPath.resolve("mmc-pack.json")) &&
                Files.exists(theoreticalMultiMCPath.resolve("instance.cfg"));
        itlt.LOGGER.debug("theoreticalMultiMCPath: " + theoreticalMultiMCPath);
        itlt.LOGGER.debug("isMultiMCLauncher: " + isMultiMCLauncher);
        if (isMultiMCLauncher) return LauncherName.MultiMC;

        // if the .minecraft folder has .curseclient and minecraftinstance.json files inside it then we know we're
        // running within the CurseClient
        final Path theoreticalCurseClientPath = itltJarPath.getParent().getParent();
        final boolean isCurseClientLauncher =
                Files.exists(theoreticalCurseClientPath.resolve(".curseclient")) &&
                Files.exists(theoreticalCurseClientPath.resolve("minecraftinstance.json"));
        itlt.LOGGER.debug("theoreticalCurseClientPath: " + theoreticalCurseClientPath);
        itlt.LOGGER.debug("isCurseClientLauncher: " + isCurseClientLauncher);
        if (isCurseClientLauncher) return LauncherName.CurseClient;

        final Path theoreticalFTBAppPath = itltJarPath.getParent().getParent().getParent().getParent();
        final boolean isFTBAppLauncher =
                Files.exists(theoreticalFTBAppPath.resolve("ftbapp.log"));
        itlt.LOGGER.debug("theoreticalFTBAppPath: " + theoreticalFTBAppPath);
        itlt.LOGGER.debug("isFTBAppLauncher: " + isFTBAppLauncher);
        if (isFTBAppLauncher) return LauncherName.FTBApp; // todo: determine if there's any benefit to supporting this launcher

        final boolean isForgeDevEnv = isForgeDevEnv();
        itlt.LOGGER.debug("isForgeDevEnv: " + isForgeDevEnv);
        if (isForgeDevEnv) return LauncherName.ForgeDevEnv;

        return LauncherName.Unknown;
    }

    public static boolean isForgeDevEnv() {
        try {
            if (new FMLDevClientLaunchProvider().name().equals("fmldevclient")) return true;
            if (new FMLDevServerLaunchProvider().name().equals("fmldevserver")) return true;
        } catch (Exception ignored) {}
        return false;
    }

    public enum LauncherName {
        Unknown,
        Technic,
        MultiMC,
        CurseClient,
        FTBApp,
        ForgeDevEnv
    }

    public static String getTechnicPackName() throws IOException {
        final Path itltJarPath = getItltJarPath();

        // get the pack slug
        final String packSlug = itltJarPath.getParent().getParent().getFileName().toString();

        // open the cache.json for the associated slug to get the pack's displayName
        final Path cacheJsonPath = itltJarPath.resolve("../../../../assets/packs" + packSlug + "/cache.json");
        final Reader reader = Files.newBufferedReader(cacheJsonPath);

        // convert the cacheJson String to a Map
        final Type type = new TypeToken<Map<String, Object>>(){}.getType();
        final Map<String, Object> definitionsMap = new Gson().fromJson(reader, type);

        final String packDisplayName = definitionsMap.get("displayName").toString();

        reader.close();

        itlt.LOGGER.debug("packDisplayName: " + packDisplayName);
        return packDisplayName;
    }

    public static String getMultiMCInstanceName() throws IOException {
        final Path itltJarPath = getItltJarPath();

        final String instanceCfg = itltJarPath.resolve("../../../instance.cfg").toString();

        // attempt to load the instance.cfg file and parse it
        final Properties parsedInstanceCfg = new Properties();
        parsedInstanceCfg.load(new FileInputStream(instanceCfg));

        final String instanceName = parsedInstanceCfg.getProperty("name");
        itlt.LOGGER.debug("instanceName: " + instanceName);

        return instanceName;
    }

    public static String getCurseClientProfileName() throws IOException {
        final Path itltJarPath = getItltJarPath();

        // open the minecraftinstance.json file
        final Reader reader = Files.newBufferedReader(itltJarPath.resolve("../../minecraftinstance.json"));

        // parse the json file to a Map with keys of type String
        final Type type = new TypeToken<Map<String, Object>>(){}.getType();
        final Map<String, Object> map = new Gson().fromJson(reader, type);

        // get the "name" key from the Map
        final String profileName = map.get("name").toString();

        // close the json file
        reader.close();

        itlt.LOGGER.debug("profileName: " + profileName);
        return profileName;
    }

    @Nullable
    public static File getTechnicPackIcon() {
        final Path itltJarPath = getItltJarPath();

        // get the pack slug
        final String packSlug = itltJarPath.getParent().getParent().getFileName().toString();

        // get the icon from the associated pack's slug
        final Path iconPath = itltJarPath.resolve("../../../../assets/packs/" + packSlug + "/icon.png");

        if (iconPath.toFile().exists()) return iconPath.toFile();
        else return null;
    }

    @Nullable
    public static File getMultiMCInstanceIcon() {
        final Path itltJarPath = getItltJarPath();

        final Path iconPath = itltJarPath.getParent().getParent().resolve("icon.png");

        if (iconPath.toFile().exists()) return iconPath.toFile();
        else return null;
    }

    public static Pair<ChecksumType, String> getFileChecksum(final File file, ChecksumType checksumType)
            throws IOException, NativeBLAKE3Util.InvalidNativeOutput {
        // for systems where the NativeBLAKE3 lib has not been compiled for, fallback to the Java implementation
        if (!NativeBLAKE3.isEnabled())
            return getFileChecksumFallback(file, checksumType);

        if (checksumType == ChecksumType.Default) checksumType = ChecksumType.BLAKE3_224; // default to BLAKE3_224

        // setup the BLAKE3 hasher
        final NativeBLAKE3 hasher = new NativeBLAKE3();
        hasher.initDefault();

        final FileInputStream fis = new FileInputStream(file); // open the file to hash
        byte[] byteArray = new byte[16384]; // create byte array to read data in chunks / as a buffer

        // a 16KB buffer is what the BLAKE3 team seems to recommend as a minimum for best performance
        // please open an issue on the itlt github with an explanation and correction if I've misunderstood this, I'm
        // not sure if internally Java works best with 8KB file reads instead or something...
        // https://github.com/BLAKE3-team/BLAKE3/blob/3a8204f5f38109aae08f4ae58b275663e1cfebab/b3sum/src/main.rs#L256

        // read file data in chunks of 16KB and send it off to the hasher
        while ((fis.read(byteArray)) != -1) hasher.update(byteArray);

        fis.close(); // we're finished reading the file, so close it

        // get the hasher's output, with the number arg being the number of output bytes, prior to hex encoding
        // our default is 28, aka BLAKE3-224. for BLAKE3-256, use 32
        final byte[] bytes;
        switch (checksumType) {
            case BLAKE3_256: {
                bytes = hasher.getOutput(32);
                break;
            }
            case BLAKE3_224:
            default: {
                bytes = hasher.getOutput(28);
                break;
            }
        }

        // the NativeBLAKE3 JNI is a C lib so we need to manually tell it to free up the memory now that
        // we're done with it
        if (hasher.isValid()) hasher.close();

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
        return Pair.of(checksumType, encodedBase62);
    }

    public static Pair<ChecksumType, String> getFileChecksum(final File file)
            throws IOException, NativeBLAKE3Util.InvalidNativeOutput {
        return getFileChecksum(file, ChecksumType.Default);
    }

    public static Pair<ChecksumType, String> getFileChecksumFallback(final File file, ChecksumType checksumType)
            throws IOException {
        if (checksumType == ChecksumType.Default) checksumType = ChecksumType.BLAKE3_224; // default to BLAKE3_224

        final Blake3 hasher = Blake3.newInstance();

        final FileInputStream fis = new FileInputStream(file);
        byte[] byteArray = new byte[16384];

        // read file data in chunks of 16KB and send it off to the hasher
        while ((fis.read(byteArray)) != -1) hasher.update(byteArray);

        fis.close();

        final byte[] bytes;
        switch (checksumType) {
            case BLAKE3_256: {
                bytes = hasher.digest(32);
                break;
            }
            case BLAKE3_224:
            default: {
                bytes = hasher.digest(28);
                break;
            }
        }

        // convert to Base62
        final Base62 base62 = Base62.createInstance();
        final String encodedBase62 = new String(base62.encode(bytes));

        // return the completed hash
        return Pair.of(checksumType, encodedBase62);
    }

    public enum ChecksumType {
        BLAKE3_256,
        BLAKE3_224,
        Default
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
         * note that the resizing maintains the existing aspect ratios - a weird 16:9 icon will still look like 16:9
         * but in a square file and be visually small as a result
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
            resizedLarge = Scalr.resize(inputIcon, 24, 32);
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

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getLatestDefinitions(ChecksumType checksumType) throws IOException, ClassCastException {
        if (checksumType == ChecksumType.Default) checksumType = ChecksumType.BLAKE3_224; // wow, an actual use of Java's mutable-by-default function args!
        final String definitionsURLString =
                "https://raw.githubusercontent.com/zlepper/itlt/api/forge/v1.0/definitions/" +
                checksumType.toString().toLowerCase() + ".json";

        // download the definitions and put it in the string "definitionsJson"
        final URLConnection connection = new URL(definitionsURLString).openConnection();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        final String definitionsJson = reader.lines().collect(Collectors.joining("\n"));

        // convert the definitionsJson String to a Map
        final Type type = new TypeToken<Map<String, Object>>(){}.getType();
        final Map<String, Object> definitionsMap = new Gson().fromJson(definitionsJson, type);

        // trim off the version patch number, accounting for cases like 1.17.10 that have a double-digit patch number
        String mcVersion = SharedConstants.getVersion().getName();
        final String[] splitMcVersion = mcVersion.split(Pattern.quote("."));
        mcVersion = splitMcVersion[0] + "." + splitMcVersion[1];

        itlt.LOGGER.debug("mcVersion: " + mcVersion);
        itlt.LOGGER.debug("definitionsMap: " + definitionsMap.toString());

        return Collections.unmodifiableMap(
                (Map<String, Object>) definitionsMap.getOrDefault(mcVersion, Collections.<String, Object>emptyMap()));

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
