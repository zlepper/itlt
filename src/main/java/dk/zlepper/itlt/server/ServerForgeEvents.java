package dk.zlepper.itlt.server;

import dk.zlepper.itlt.itlt;
import dk.zlepper.itlt.server.helpers.ServerUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = itlt.MOD_ID, value = Dist.DEDICATED_SERVER, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerForgeEvents {

    @SubscribeEvent
    public static void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        if (ServerConfig.enableAnticheat.get() && ServerUtils.ConnectingPlayer.isCheating.get()) {
            final PlayerEntity player = event.getPlayer();
            if (player instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) player).connection.disconnect(ServerUtils.ConnectingPlayer.hasItlt.get() ?
                        new TranslationTextComponent("itlt.multiplayer.disconnect.cheats_not_allowed") :
                        new StringTextComponent("Cheats are not allowed on this server"));
            }
            ServerUtils.ConnectingPlayer.isCheating.set(false);
        }
    }
}
