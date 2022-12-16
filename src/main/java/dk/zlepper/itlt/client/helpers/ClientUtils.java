package dk.zlepper.itlt.client.helpers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dk.zlepper.itlt.client.ClientConfig;
import dk.zlepper.itlt.client.ClientModEvents;
import dk.zlepper.itlt.itlt;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.server.packs.resources.IoSupplier;
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

import static dk.zlepper.itlt.client.ClientModEvents.detectedLauncher;

public class ClientUtils {

    public static String getAutoDetectedDisplayName() {
        String autoDetectedDisplayName = ClientConfig.autoDetectedDisplayNameFallback.get();
        if (ClientConfig.enableUsingAutodetectedDisplayName.get()) {
            try {
                final String tmp = detectedLauncher.getModpackDisplayName();
                if (tmp != null) autoDetectedDisplayName = tmp;
            } catch (final IOException e) {
                itlt.LOGGER.warn("Unable to auto-detect modpack display name, falling back to autoDetectedDisplayNameFallback in the config.");
                e.printStackTrace();
            }
        }
        return autoDetectedDisplayName;
    }

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

        for (int i = 0; i < list.size(); i++) {
            final ServerData serverData = list.get(i);
            if (serverData.name.equalsIgnoreCase(server.name) && serverData.ip.equalsIgnoreCase(server.ip))
                return true;
        }

        return false;
    }

    public static String getCustomWindowTitle(final Minecraft mcInstance) {
        if (ClientConfig.enableCustomWindowTitle == null)
            return "Minecraft* " + SharedConstants.getCurrentVersion().getName();

        if (!ClientConfig.enableCustomWindowTitle.get())
            return mcInstance.createTitle();

        String customWindowTitle = ClientConfig.customWindowTitleText.get();

        String autoDetectedDisplayName = getAutoDetectedDisplayName();
        customWindowTitle = customWindowTitle.replaceFirst("%autoName", autoDetectedDisplayName);

        // replace %mc with the Vanilla window title from getWindowTitle() (createTitle == getWindowTitle)
        customWindowTitle = customWindowTitle.replaceFirst("%mc", mcInstance.createTitle());

        if (customWindowTitle.isEmpty()) return mcInstance.createTitle();
        else return customWindowTitle;
    }

    public static void setWindowIcon(final InputStream inputIconInStream, final Minecraft mcInstance,
                                     final File itltDir, final String fileExtension) throws IOException {
        // write the InputStream to a temporary file
        Files.copy(inputIconInStream, Paths.get(itltDir.getAbsolutePath(), "temp." + fileExtension), StandardCopyOption.REPLACE_EXISTING);
        final File tmpIconFile = new File(itltDir.getAbsolutePath(), "temp." + fileExtension);

        // use the temporary file to set the window icon
        setWindowIcon(tmpIconFile, mcInstance);

        // delete the temporary file now that we're done with it, if unable then delete it once the game quits
        if (!tmpIconFile.delete()) tmpIconFile.deleteOnExit();
    }

    public static void setWindowIcon(final File inputIconFile, final Minecraft mcInstance) throws IOException {
        final List<InputStream> iconsList = new ArrayList<>();
        List<BufferedImage> bufferedImageList = new ArrayList<>(0);

        final String inputIconFilenameAndExt = inputIconFile.getName().toLowerCase();
        if (inputIconFilenameAndExt.endsWith(".ico")) {
            // load all of the images inside the `.ico` file as a list of `BufferedImage`s
            bufferedImageList = ICODecoder.read(inputIconFile);
        } else if (inputIconFilenameAndExt.endsWith(".icns")) {
            // try to load all of the images inside the `.icns` file as a list of `BufferedImage`s
            try {
                bufferedImageList = new IcnsImageParser().getAllBufferedImages(inputIconFile);
            } catch (final ImageReadException e) {
                e.printStackTrace();
                return;
            }
        } else if (inputIconFilenameAndExt.endsWith(".png")) {
            // load the `.png` file directly as an `InputStream`
            iconsList.add(new FileInputStream(inputIconFile));
        }

        itlt.LOGGER.debug("Icon file: \"" + inputIconFilenameAndExt + "\"");

        if (inputIconFilenameAndExt.endsWith(".ico") || inputIconFilenameAndExt.endsWith(".icns")) {
            // convert from List<BufferedImage> to List<InputStream> and filter out invalid image types
            for (final BufferedImage bufferedImage : bufferedImageList) {
                itlt.LOGGER.debug("---");
                itlt.LOGGER.debug("Type: " + bufferedImage.getType());
                itlt.LOGGER.debug("Width: " + bufferedImage.getWidth());
                itlt.LOGGER.debug("Height: " + bufferedImage.getHeight());
                itlt.LOGGER.debug("Transparency: " + bufferedImage.getTransparency());

                // only convert icons that are 8bit per channel, non-premultiplied RGBA as that's what GLFW expects.
                // icons that aren't converted are not included in the List<InputStream>
                if (bufferedImage.getType() == BufferedImage.TYPE_INT_ARGB) {

                    // handle special case for ICNS where icon sizes above 48px aren't being properly decoded yet
                    if (inputIconFilenameAndExt.endsWith(".icns")) {
                        if (bufferedImage.getWidth() <= 48) {
                            iconsList.add(convertToInputStream(bufferedImage));
                            itlt.LOGGER.debug("Added embedded image");
                        } else {
                            itlt.LOGGER.debug("Skipped embedded image");
                        }
                    } else {
                        iconsList.add(convertToInputStream(bufferedImage));
                        itlt.LOGGER.debug("Added embedded image");
                    }

                } else {
                    itlt.LOGGER.debug("Skipped embedded image");
                }
            }
        }

        itlt.LOGGER.debug("Final iconsList size: " + iconsList.size());

        final GLFWImage.Buffer buffer = loadIconsIntoBuffer(iconsList, mcInstance);
        GLFW.glfwSetWindowIcon(mcInstance.getWindow().window, buffer);
    }

    public static GLFWImage.Buffer loadIconsIntoBuffer(final List<InputStream> iconsList,
                                                       final Minecraft mcInstance) throws IOException {
        return loadIconsIntoBuffer(iconsList, mcInstance, 1);
    }

    public static GLFWImage.Buffer loadIconsIntoBuffer(final List<InputStream> iconsList, final Minecraft mcInstance,
                                                       final int attempt) throws IOException {
        final MemoryStack memoryStack = MemoryStack.stackPush();
        final GLFWImage.Buffer buffer = GLFWImage.malloc(iconsList.size(), memoryStack);

        final IntBuffer intBufferX = memoryStack.mallocInt(1);
        final IntBuffer intBufferY = memoryStack.mallocInt(1);
        final IntBuffer intBufferChannels = memoryStack.mallocInt(1);

        // load each icon in the iconsList and append it to the GLFWImage.Buffer, keeping track of any errors that
        // may occur when trying to load each icon
        short iconCounter = 0;
        short errorCounter = 0;
        List<InputStream> newIconsList = new ArrayList<>();
        for (final InputStream inStream : iconsList) {
            final ByteBuffer byteBuffer;
            try {
                byteBuffer = mcInstance.getWindow().readIconPixels(() -> inStream, intBufferX, intBufferY, intBufferChannels);
                if (byteBuffer == null) throw new IOException("byteBuffer is null");
            } catch (final IOException e) {
                itlt.LOGGER.debug("Unable to load image #" + iconCounter + " inside iconsList, skipping...");
                errorCounter++;
                continue;
            }
            buffer.position(iconCounter);
            buffer.width(intBufferX.get(0));
            buffer.height(intBufferY.get(0));
            buffer.pixels(byteBuffer);
            iconCounter++;
            newIconsList.add(inStream);
        }

        if (errorCounter == iconsList.size()) {
            // if there was an error loading all of the icons inside the .ico/.icns, throw an error and don't try setting the
            // window icon as an empty buffer.
            throw new IOException("Unable to load icon(s): Failed to load all embedded images");
        }

        // GLFW expects the allocated stack capacity to match the used capacity otherwise it'll crash
        if (errorCounter > 0) {
            if (attempt > 2) {
                throw new IOException("Unable to load icon(s): Too many failed attempts");
            } else {
                // Allocate a new stack without erroneous icons and use that instead
                return loadIconsIntoBuffer(newIconsList, mcInstance, attempt + 1);
            }
        } else {
            buffer.position(0);
            return buffer;
        }
    }

    public static ByteArrayInputStream convertToInputStream(final BufferedImage bufferedImage) throws IOException {
        // convert BufferedImage to ByteArrayOutputStream without a double copy behind the scenes, only for our
        // specific use-case. This is faster and more efficient, but can be problematic when performing other operations
        // on the same instance of a ByteArrayOutputStream or BufferedImage.
        // https://stackoverflow.com/a/12253091/3944931 (warning: unsafe - not suitable for all use-cases!)
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
                "itlt." + (messageContent.msgType.name()
                        + "." + messageContent.msgDesire.name()
                        + "." + messageContent.msgSubject.name()).toLowerCase();
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
        String guideURL = switch (messageContent.msgSubject) {
            case Memory -> {
                if (ClientConfig.enableCustomMemoryAllocGuide.get()) yield ClientConfig.customMemoryAllocGuideURL.get();
                else yield defaultGuideURL;
            }
            case Java -> {
                if (messageContent.msgDesire == Message.Desire.SixtyFourBit) {
                    if (ClientConfig.enableCustom64bitJavaGuide.get()) yield ClientConfig.custom64bitJavaGuideURL.get();
                    else yield defaultGuideURL;
                } else if (messageContent.msgDesire == Message.Desire.Newer) {
                    if (ClientConfig.enableCustomJavaUpgradeGuide.get()) yield ClientConfig.customJavaUpgradeGuideURL.get();
                    else yield defaultGuideURL;
                } else {
                    if (ClientConfig.enableCustomJavaDowngradeGuide.get()) yield ClientConfig.customJavaDowngradeGuideURL.get();
                    else yield defaultGuideURL;
                }
            }
        };
        guideURL = guideURL.replaceAll("%launcher", detectedLauncher.getName())
                .replaceAll("%reason", messageContent.toString())
                .replaceAll("%type", messageContent.msgType.toString())
                .replaceAll("%desire", messageContent.msgDesire.toString())
                .replaceAll("%subject", messageContent.msgSubject.toString());

        // translate the keys manually as they aren't able to be translated by the game until after the main menu's shown

        // determine which language json we need to read
        final String lang = Minecraft.getInstance().options.languageCode;

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

            messageBody = switch (messageContent) {
                case NeedsMoreMemory -> messageBody.replaceFirst("%s",
                        ClientConfig.getSimplifiedFloatStr(ClientConfig.reqMinMemoryAmountInGB.get().floatValue()));
                case WantsMoreMemory -> messageBody.replaceFirst("%s",
                        ClientConfig.getSimplifiedFloatStr(ClientConfig.warnMinMemoryAmountInGB.get().floatValue()));
                case NeedsLessMemory -> messageBody.replaceFirst("%s",
                        ClientConfig.getSimplifiedFloatStr(ClientConfig.reqMaxMemoryAmountInGB.get().floatValue()));
                case WantsLessMemory -> messageBody.replaceFirst("%s",
                        ClientConfig.getSimplifiedFloatStr(ClientConfig.warnMaxMemoryAmountInGB.get().floatValue()));
                case NeedsNewerJava -> {
                    messageTitle = messageTitle.replaceFirst("%s", ClientConfig.requiredMinJavaVersion.get().toString());
                    yield messageBody.replaceFirst("%s", ClientConfig.requiredMinJavaVersion.get().toString());
                }
                case WantsNewerJava -> {
                    messageTitle = messageTitle.replaceFirst("%s", ClientConfig.warnMinJavaVersion.get().toString());
                    yield messageBody.replaceFirst("%s", ClientConfig.warnMinJavaVersion.get().toString());
                }
                case NeedsOlderJava -> {
                    messageTitle = messageTitle.replaceFirst("%s", ClientConfig.requiredMaxJavaVersion.get().toString());
                    yield messageBody.replaceFirst("%s", ClientConfig.requiredMaxJavaVersion.get().toString());
                }
                case WantsOlderJava -> {
                    messageTitle = messageTitle.replaceFirst("%s", ClientConfig.warnMaxJavaVersion.get().toString());
                    yield messageBody.replaceFirst("%s", ClientConfig.warnMaxJavaVersion.get().toString());
                }
                default -> messageBody; // NeedsJava64bit, WantsJava64bit
            };
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
            final var builder = new ProcessBuilder(
                    System.getProperty("java.home") + File.separator + "bin" + File.separator + "java",
                    "-Dapple.awt.application.appearance=system", // macOS dark theme support in Java 14+ (JDK-8235363)
                    "-XX:+IgnoreUnrecognizedVMOptions", "--add-opens=java.desktop/sun.awt.shell=ALL-UNNAMED", // allow access to Win32ShellFolder2 on Java 16+
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
}
