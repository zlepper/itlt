package dk.zlepper.itlt.eventhandlers;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import dk.zlepper.itlt.Itlt;
import dk.zlepper.itlt.KeyBindings;

/**
 * Created by Rasmus on 7/17/2015.
 */
public class KeyPressedInChatGuiEventHandler {

    private ChatGuiEventHandler handler;
    public KeyPressedInChatGuiEventHandler(ChatGuiEventHandler handler) {
        Itlt.logger.info("Creating keyPressedHandler");
        this.handler = handler;
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Itlt.logger.info("Key pressed");
        if(KeyBindings.isEnterPressed()) {
            Itlt.logger.info("It was the enter key");
            handler.clearInputField();
        }
    }
}
