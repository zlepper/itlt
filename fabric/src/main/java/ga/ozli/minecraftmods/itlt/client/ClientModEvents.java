package ga.ozli.minecraftmods.itlt.client;

import com.mojang.blaze3d.systems.RenderSystem;
import ga.ozli.minecraftmods.itlt.platform.Services;
import ga.ozli.minecraftmods.itlt.shared.CommonConfig;
import ga.ozli.minecraftmods.itlt.shared.client.ClientConfig;
import ga.ozli.minecraftmods.itlt.shared.client.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

public final class ClientModEvents {
    private static boolean ranLoadComplete = false;

    public static void onClientSetup(Minecraft client) {
        RenderSystem.assertOnRenderThread();

        ClientConfig.ready = true;

        if (ClientConfig.Graphics.WRONG_GPU && ClientUtils.runningOnWrongGPU()) {
            Services.PLATFORM.addWarning("Running on wrong GPU");
        }
    }

    public static void onLoadComplete(Minecraft client, Screen screen, int scaledWidth, int scaledHeight) {
        if (ranLoadComplete)
            return;

        if (screen instanceof TitleScreen) {
            ranLoadComplete = true;

            ClientUtils.setCustomIcon();

            if (CommonConfig.Java.Advanced.GC_ON_STARTUP) {
                System.gc();
            }
        }
    }

    private ClientModEvents() {}
}
