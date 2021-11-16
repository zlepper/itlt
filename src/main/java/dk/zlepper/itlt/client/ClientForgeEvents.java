package dk.zlepper.itlt.client;

import com.mojang.realmsclient.RealmsMainScreen;
import dk.zlepper.itlt.client.screens.FirstLaunchScreen;
import dk.zlepper.itlt.itlt;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = itlt.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientForgeEvents {

    static boolean showFirstLaunchScreen = true;

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

        //if (screen instanceof TitleScreen && showFirstLaunchScreen) {
        if (screen instanceof SelectWorldScreen) {
            //showFirstLaunchScreen = false; // todo: save to config file
            //event.setGui(new FirstLaunchScreen(new TitleScreen(true), new TranslatableComponent("resourcePack.title")));
            event.setGui(new FirstLaunchScreen(new TitleScreen(), new TranslatableComponent("itlt.welcomeScreen.title", ClientConfig.autoDetectedDisplayNameFallback.get())));
        }
    }
}
