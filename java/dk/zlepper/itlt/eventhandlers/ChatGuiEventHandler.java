package dk.zlepper.itlt.eventhandlers;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;

import java.lang.reflect.Field;

public class ChatGuiEventHandler {

    private GuiTextField inputField;

    public ChatGuiEventHandler() {
        System.out.println("Registered event handler");
    }

    @SubscribeEvent
    public void onGuiOpenEvent(GuiScreenEvent.InitGuiEvent.Post event){
        if(event.gui instanceof GuiChat) {
            GuiChat chat = (GuiChat) event.gui;

            try {
                Class<?> chatClass = chat.getClass();

                Field infi = chatClass.getDeclaredField("inputField");
                infi.setAccessible(true);

                GuiTextField newInputField = (GuiTextField) infi.get(chat);

                if(inputField != null) {
                    newInputField.setText(inputField.getText());
                }

                inputField = newInputField;
                // TODO make sure to only remember if the chat was closed by escape, not by sending the message
            } catch(NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
