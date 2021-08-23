package dk.zlepper.itlt.client;

import com.mojang.realmsclient.RealmsMainScreen;
import dk.zlepper.itlt.itlt;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraft.client.gui.screens.ChatOptionsScreen;
import net.minecraft.client.gui.screens.DirectJoinServerScreen;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.gui.screens.LanguageSelectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;

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
                    || (ClientConfig.doExplicitGCOnSleep.get() && screen instanceof InBedChatScreen)
                    || (ClientConfig.doExplicitGCOnMenu.get() && (
                            screen instanceof SelectWorldScreen || screen instanceof JoinMultiplayerScreen
                                    || screen instanceof DirectJoinServerScreen || screen instanceof PackSelectionScreen
                                    || screen instanceof LanguageSelectScreen || screen instanceof ChatOptionsScreen
                                    || screen instanceof ControlsScreen || screen instanceof AccessibilityOptionsScreen
                                    || screen instanceof RealmsMainScreen || screen instanceof StatsScreen))) {
                Runtime.getRuntime().gc();
            }
        }
    }
}
