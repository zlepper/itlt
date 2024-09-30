package ga.ozli.minecraftmods.itlt.platform;

import ga.ozli.minecraftmods.itlt.platform.services.IPlatformHelper;
import ga.ozli.minecraftmods.itlt.shared.Constants;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.TitleScreen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class FabricPlatformHelper implements IPlatformHelper {
    private static final List<String> WARNINGS = new ArrayList<>();
    private static boolean registeredWarningHandler = false;

    @Override
    public void addWarning(String message) {
        WARNINGS.add(message);
        registerWarningHandlerIfNeeded();
    }

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public PhysicalSide getPhysicalSide() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? PhysicalSide.CLIENT : PhysicalSide.SERVER;
    }

    private static void registerWarningHandlerIfNeeded() {
        if (registeredWarningHandler)
            return;

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            // when on the main menu
            ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
                if (screen instanceof TitleScreen) {
                    WARNINGS.forEach(Constants.LOG::warn);
                    WARNINGS.clear();
                }
            });
        } else {
            ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> {
                WARNINGS.forEach(Constants.LOG::warn);
                WARNINGS.clear();
            });
        }

        registeredWarningHandler = true;
    }
}
