package dk.zlepper.itlt.eventhandlers;

import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
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
        if(event.getGui() instanceof GuiChat) {
            GuiChat chat = (GuiChat) event.getGui();

            try {
                Class<?> chatClass = chat.getClass();

                Field infi = chatClass.getDeclaredField("inputField");
                infi.setAccessible(true);

                GuiTextField newInputField = (GuiTextField) infi.get(chat);

                if(inputField != null) {
                    newInputField.setText(inputField.getText());
                }

                inputField = newInputField;

                MinecraftForge.EVENT_BUS.register(handler);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @SubscribeEvent
    public void OnGuiOpenEvent(GuiOpenEvent e) {
        if(e.getGui() == null && handler != null) {
            try {
                MinecraftForge.EVENT_BUS.unregister(handler);
            } catch(NullPointerException ex) {
                // Ignored
            }
        }
    }

    public void clearInputField() {
        inputField = null;
    }
}
