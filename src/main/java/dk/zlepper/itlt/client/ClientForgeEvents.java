package dk.zlepper.itlt.client;

import dk.zlepper.itlt.itlt;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.achievement.GuiStats;
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
            final GuiScreen screen = event.getGui();
            if (screen == null) return;
            itlt.LOGGER.debug("Screen: " + screen);

            // Tell the GC to run whenever the user does certain non-latency-critical actions such as being on the
            // pause screen or opening an opaque bg screen such as the Resource Pack or Controls screens.
            // Doing this can help reduce memory usage in certain situations and also slightly reduces the chances
            // of a large GC happening in the middle of gameplay.
            if ((ClientConfig.doExplicitGCOnPause.get() && screen instanceof GuiIngameMenu)
                    || (ClientConfig.doExplicitGCOnSleep.get() && screen instanceof GuiSleepMP)
                    || (ClientConfig.doExplicitGCOnMenu.get() && (
                            screen instanceof GuiWorldSelection || screen instanceof GuiMultiplayer
                                    || screen instanceof GuiScreenServerList || screen instanceof GuiScreenResourcePacks
                                    || screen instanceof GuiLanguage || screen instanceof ScreenChatOptions
                                    || screen instanceof GuiControls || screen instanceof GuiStats))) {
                Runtime.getRuntime().gc();
            }
        }
    }
}
