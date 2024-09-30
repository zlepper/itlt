package ga.ozli.minecraftmods.itlt.shared.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import ga.ozli.minecraftmods.itlt.platform.Services;
import ga.ozli.minecraftmods.itlt.shared.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ClientUtils {
    private static final Minecraft MC = Minecraft.getInstance();

    /**
     * Checks if MC is running on the wrong GPU (i.e. integrated graphics despite dedicated being available)
     * @return true if so, false if not or unsure
     */
    public static boolean runningOnWrongGPU() {
        var allGpus = ExtendedGraphicsCard.getAll();
        if (allGpus.size() == 1)
            return false; // only one GPU, so can't be wrong

        @Nullable var currentGpu = ExtendedGraphicsCard.getCurrent(allGpus);
        if (currentGpu == null)
            return false; // can't determine current GPU

        return currentGpu.isIntegrated();
    }

    public static void setCustomIcon() {
        if (Minecraft.ON_OSX)
            return; // todo: macOS support

        try {
            var icons = ClientUtils.getIcons();
            if (icons.isEmpty())
                return;

            ClientUtils.setWindowIcon(icons);
        } catch (IOException e) {
            Constants.LOG.error("Failed to set window icon: {}", e.toString());
        }
    }

    public static void setCustomWindowTitle() {
        // todo: mixin Minecraft.updateTitle() to redirect to this method
        MC.getWindow().setTitle(ClientConfig.Display.WINDOW_TITLE.replaceFirst("%mc", MC.createTitle()));
    }

    private static List<IoSupplier<InputStream>> getIcons() throws IOException {
        // look for icons in the itlt folder
        var itltFolder = Services.PLATFORM.getConfigDir().resolve("itlt");

        List<Path> iconCandidates;
        try (var files = Files.list(itltFolder)) {
            iconCandidates = files.filter(ClientUtils::isIcon).toList();
        }

        if (iconCandidates.isEmpty()) {
            Constants.LOG.debug("No icons found in {}", itltFolder);
            return List.of();
        }

        Constants.LOG.debug("Found icons: {}", iconCandidates);

        // for now, let's just use the first PNG we find
        // todo: support ICO and ICNS files
        return iconCandidates.stream()
                .filter(path -> getFileExtension(path).equals("png"))
                .map(IoSupplier::create)
                .limit(1)
                .toList();

//        switch (iconCandidates.size()) {
//            case 0 -> Constants.LOG.debug("No icons found in {}", itltFolder);
//            case 1 -> Constants.LOG.debug("Found icon: {}", iconCandidates.getFirst());
//            default -> {
//                Constants.LOG.debug("Found multiple icons: {}", iconCandidates);
//
//                // if we have an ico or icns file, use that and ignore the rest
//                var icoOrIcns = iconCandidates.stream()
//                        .filter(path -> getFileExtension(path).equals("ico") || getFileExtension(path).equals("icns"))
//                        .findFirst();
//
//                if (icoOrIcns.isPresent()) {
//                    // yay, we have an ico/icns file! let's extract all the different sizes from it
//                    var iconPath = icoOrIcns.get();
//                    // todo
//                } else {
//                    // no ico/icns file, do we have pngs?
//                }
//            }
//        }
    }

    private static boolean isIcon(Path iconPath) {
        return switch (getFileExtension(iconPath)) {
            case "png", "ico", "icns" -> true;
            default -> false;
        };
    }

    private static String getFileExtension(Path path) {
        var fileName = path.getFileName().toString();
        var lastDot = fileName.lastIndexOf('.');
        return lastDot == -1 ? "" : fileName.substring(lastDot + 1).toLowerCase(Locale.ROOT);
    }

    private static void setWindowIcon(List<IoSupplier<InputStream>> icons) throws IOException {
        RenderSystem.assertOnRenderThread();

        int platform = GLFW.glfwGetPlatform();
        switch (platform) {
            case GLFW.GLFW_PLATFORM_WIN32, GLFW.GLFW_PLATFORM_X11 -> {
                var setIcons = new ArrayList<ByteBuffer>(icons.size());

                try (var memoryStack = MemoryStack.stackPush()) {
                    GLFWImage.Buffer buffer = GLFWImage.malloc(icons.size(), memoryStack);

                    for (int i = 0; i < icons.size(); i++) {
                        try (var nativeimage = NativeImage.read(icons.get(i).get())) {
                            ByteBuffer bytebuffer = MemoryUtil.memAlloc(nativeimage.getWidth() * nativeimage.getHeight() * 4);
                            setIcons.add(bytebuffer);
                            bytebuffer.asIntBuffer().put(nativeimage.getPixelsRGBA());
                            buffer.position(i);
                            buffer.width(nativeimage.getWidth());
                            buffer.height(nativeimage.getHeight());
                            buffer.pixels(bytebuffer);
                        }
                    }

                    GLFW.glfwSetWindowIcon(MC.getWindow().getWindow(), buffer.position(0));
                } finally {
                    setIcons.forEach(MemoryUtil::memFree);
                }
            }
            // todo: macOS (aka GLFW.GLFW_PLATFORM_COCOA)
            case GLFW.GLFW_PLATFORM_COCOA, GLFW.GLFW_PLATFORM_WAYLAND, GLFW.GLFW_PLATFORM_NULL -> {}
            default -> Constants.LOG.warn("Not setting icon for unrecognized platform: {}", platform);
        }
    }

    private ClientUtils() {}
}
