package dk.zlepper.itlt.server.helpers;

import dk.zlepper.itlt.server.ServerForgeEvents;
import net.minecraftforge.fml.network.FMLHandshakeMessages;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class ServerUtils {

    public static class ConnectingPlayer {
        public static AtomicBoolean isCheating = new AtomicBoolean(false);
        public static AtomicBoolean hasItlt = new AtomicBoolean(false);
    }

    public static void itltHandleClientModListOnServer(FMLHandshakeMessages.C2SModListReply clientModList, Supplier<NetworkEvent.Context> c) {
        //c.get().getNetworkManager().closeChannel(new StringTextComponent("Connection closed - cheats are not allowed on this server"));
        if (clientModList.getModList().contains("itlt")) ConnectingPlayer.hasItlt.set(true);
        ConnectingPlayer.isCheating.set(false);
    }
}