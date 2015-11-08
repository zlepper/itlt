package dk.zlepper.itlt.eventhandlers;

import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;

public class ChatGuiEventHandler {

    private GuiTextField inputField;
    private KeyPressedInChatGuiEventHandler handler = null;

    public ChatGuiEventHandler() {
        System.out.println("Registered event handler");

        handler = new KeyPressedInChatGuiEventHandler(this);
    }

    @SubscribeEvent
    public void onGuiInitEvent(GuiScreenEvent.InitGuiEvent.Post event){
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

                FMLCommonHandler.instance().bus().register(handler);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @SubscribeEvent
    public void OnGuiOpenEvent(GuiOpenEvent e) {
        if(e.gui == null && handler != null) {
            try {
                FMLCommonHandler.instance().bus().unregister(handler);
            } catch(NullPointerException ex) {
                // Ignored
            }
        }
    }

    public void clearInputField() {
        inputField = null;
    }
}
