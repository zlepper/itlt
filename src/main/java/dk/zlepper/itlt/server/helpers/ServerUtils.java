package dk.zlepper.itlt.server.helpers;

import dk.zlepper.itlt.common.AnticheatUtils;
import net.minecraftforge.fml.network.FMLHandshakeMessages;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class ServerUtils {

    public static class ConnectingPlayer {
        public static AtomicBoolean isCheating = new AtomicBoolean(false);
        public static AtomicBoolean hasItlt = new AtomicBoolean(false);
    }

    public static void itltHandleClientModListOnServer(final FMLHandshakeMessages.C2SModListReply clientModList,
                                                       final Supplier<NetworkEvent.Context> ctx) {
        //ctx.get().getNetworkManager().closeChannel(new StringTextComponent("Connection closed - cheats are not allowed on this server"));
        if (clientModList.getModList().contains("itlt")) ConnectingPlayer.hasItlt.set(true);
        ConnectingPlayer.isCheating.set(
                AnticheatUtils.hasModIdListGotKnownCheats(clientModList.getModList(), AnticheatUtils.getModIdDefinitions())
        );
    }
}