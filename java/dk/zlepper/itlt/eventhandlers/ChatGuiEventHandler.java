package dk.zlepper.itlt.eventhandlers;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;

public class ChatGuiEventHandler {

    private Minecraft mc;
    private String lastMessage = "";

    public ChatGuiEventHandler() {
        System.out.println("Registered event handler");
        mc = Minecraft.getMinecraft();
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event){
        if(mc.currentScreen instanceof GuiChat) {
            System.out.println("In chat GUI");
            GuiChat chat = (GuiChat) mc.currentScreen;
        }

    }
}
