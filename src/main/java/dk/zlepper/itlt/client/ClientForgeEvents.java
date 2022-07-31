package dk.zlepper.itlt.client;

import com.mojang.realmsclient.RealmsMainScreen;
import dk.zlepper.itlt.client.helpers.ClientUtils;
import dk.zlepper.itlt.client.screens.FirstLaunchScreen;
import dk.zlepper.itlt.itlt;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static dk.zlepper.itlt.client.ClientModEvents.itltDir;

@Mod.EventBusSubscriber(modid = itlt.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientForgeEvents {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onGuiOpen(final ScreenEvent.Opening event) {
        final Screen screen = event.getNewScreen();
        if (screen == null) return;

        if (ClientConfig.enableExplicitGC.get()) {
            itlt.LOGGER.debug("Screen: " + screen);

            // Tell the GC to run whenever the user does certain non-latency-critical actions such as being on the
            // pause screen or opening an opaque bg screen such as the Resource Pack or Controls screens.
            // Doing this can help reduce memory usage in certain situations and also slightly reduces the chances
            // of a large GC happening in the middle of gameplay.
            ClientConfig.doExplicitGCWhen.get().forEach(trigger -> {
                switch (ClientConfig.explicitGCTriggers.valueOf(trigger)) {
                    case Pause -> {
                        if (screen.isPauseScreen()) Runtime.getRuntime().gc();
                    }
                    case Sleep -> {
                        if (screen instanceof InBedChatScreen) Runtime.getRuntime().gc();
                    }
                    case Menu -> {
                        if (screen instanceof SelectWorldScreen || screen instanceof JoinMultiplayerScreen
                                || screen instanceof DirectJoinServerScreen || screen instanceof PackSelectionScreen
                                || screen instanceof LanguageSelectScreen || screen instanceof ChatOptionsScreen
                                || screen instanceof ControlsScreen || screen instanceof AccessibilityOptionsScreen
                                || screen instanceof RealmsMainScreen || screen instanceof StatsScreen)
                            Runtime.getRuntime().gc();
                    }
                }
            });
        }

        if (ClientConfig.enableWelcomeScreen.get() && screen instanceof TitleScreen) {
            // make sure the config/itlt/ folder exists
            if (itltDir == null) {
                itlt.LOGGER.warn("itlt folder in the config folder is missing");
                itlt.LOGGER.warn("Please create a folder named \"itlt\" (case sensitive) in the config folder.");
                return;
            }

            // if no welcome.txt is found, try copying the example one embedded inside the jar to config/itlt/welcome.txt
            final var welcomeFilePath = itltDir.toPath().resolve("itlt/welcome.txt");
            if (!welcomeFilePath.toFile().exists()) {
                final Path embeddedWelcomeFile = ModList.get().getModFileById("itlt").getFile().findResource("welcome.txt");
                try {
                    Files.copy(embeddedWelcomeFile, welcomeFilePath, StandardCopyOption.REPLACE_EXISTING);
                } catch (final IOException ignored) {}
            }

            // show the welcome screen
            event.setNewScreen(new FirstLaunchScreen(
                    new TitleScreen(), Component.translatable("itlt.welcomeScreen.title", ClientConfig.enableUsingCustomWelcomeHeaderModpackDisplayName.get() ? ClientConfig.customWelcomeHeaderModpackDisplayName.get() : ClientUtils.getAutoDetectedDisplayName()))
            );
        }
    }
}
