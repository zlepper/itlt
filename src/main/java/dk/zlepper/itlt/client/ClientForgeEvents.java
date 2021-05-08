package dk.zlepper.itlt.client;

import com.mojang.realmsclient.RealmsMainScreen;
import dk.zlepper.itlt.itlt;
import net.minecraft.client.gui.AccessibilityScreen;
import net.minecraft.client.gui.screen.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = itlt.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientForgeEvents {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onGuiOpen(final GuiOpenEvent event) {
        if (ClientConfig.enableExplicitGC.get()) {
            final Screen screen = event.getGui();
            if (screen == null) return;
            itlt.LOGGER.debug("Screen: " + screen);

            // Tell the GC to run whenever the user does certain non-latency-critical actions such as being on the
            // pause screen or opening an opaque bg screen such as the Resource Pack or Controls screens.
            // Doing this can help reduce memory usage in certain situations and also slightly reduces the chances
            // of a large GC happening in the middle of gameplay.
            if ((ClientConfig.doExplicitGCOnPause.get() && screen.isPauseScreen())
                    || (ClientConfig.doExplicitGCOnSleep.get() && screen instanceof SleepInMultiplayerScreen)
                    || (ClientConfig.doExplicitGCOnMenu.get() && (
                            screen instanceof WorldSelectionScreen || screen instanceof MultiplayerScreen
                                    || screen instanceof ServerListScreen || screen instanceof PackScreen
                                    || screen instanceof LanguageScreen || screen instanceof ChatOptionsScreen
                                    || screen instanceof ControlsScreen || screen instanceof AccessibilityScreen
                                    || screen instanceof RealmsMainScreen || screen instanceof StatsScreen))) {
                Runtime.getRuntime().gc();
            }
        }
    }
}
