package dk.zlepper.itlt.client;

import com.mojang.realmsclient.RealmsMainScreen;
import dk.zlepper.itlt.itlt;
import net.minecraft.client.gui.AccessibilityScreen;
import net.minecraft.client.gui.screen.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
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
    public static void onGuiOpen(final GuiOpenEvent event) {
        final Screen screen = event.getGui();
        if (screen == null) return;

        if (ClientConfig.enableExplicitGC.get()) {
            itlt.LOGGER.debug("Screen: " + screen);

            // Tell the GC to run whenever the user does certain non-latency-critical actions such as being on the
            // pause screen or opening an opaque bg screen such as the Resource Pack or Controls screens.
            // Doing this can help reduce memory usage in certain situations and also slightly reduces the chances
            // of a large GC happening in the middle of gameplay.
            ClientConfig.doExplicitGCWhen.get().forEach(trigger -> {
                switch (ClientConfig.explicitGCTriggers.valueOf(trigger)) {
                    case Pause: {
                        if (screen.isPauseScreen()) Runtime.getRuntime().gc();
                        break;
                    }
                    case Sleep: {
                        if (screen instanceof SleepInMultiplayerScreen) Runtime.getRuntime().gc();
                        break;
                    }
                    case Menu: {
                        if (screen instanceof WorldSelectionScreen || screen instanceof MultiplayerScreen
                                || screen instanceof ServerListScreen || screen instanceof PackScreen
                                || screen instanceof LanguageScreen || screen instanceof ChatOptionsScreen
                                || screen instanceof ControlsScreen || screen instanceof AccessibilityScreen
                                || screen instanceof RealmsMainScreen || screen instanceof StatsScreen)
                            Runtime.getRuntime().gc();
                        break;
                    }
                }
            });
        }

        if (ClientConfig.enableWelcomeScreen.get() && screen instanceof MainMenuScreen) {
            // make sure the config/itlt/ folder exists
            if (itltDir == null) {
                itlt.LOGGER.warn("itlt folder in the config folder is missing");
                itlt.LOGGER.warn("Please create a folder named \"itlt\" (case sensitive) in the config folder.");
                return;
            }

            // if no welcome.txt is found, try copying the example one embedded inside the jar to config/itlt/welcome.txt
            final Path welcomeFilePath = itltDir.toPath().resolve("itlt/welcome.txt");
            if (!welcomeFilePath.toFile().exists()) {
                final Path embeddedWelcomeFile = ModList.get().getModFileById("itlt").getFile().findResource("welcome.txt");
                try {
                    Files.copy(embeddedWelcomeFile, welcomeFilePath, StandardCopyOption.REPLACE_EXISTING);
                } catch (final IOException ignored) {
                }
            }

            // show the welcome screen
            /*event.setGui(new FirstLaunchScreen(
                    new MainMenuScreen(), new TranslationTextComponent("itlt.welcomeScreen.title", ClientConfig.enableUsingCustomWelcomeHeaderModpackDisplayName.get() ? ClientConfig.customWelcomeHeaderModpackDisplayName.get() : ClientUtils.getAutoDetectedDisplayName()))
            );*/
        }
    }
}
