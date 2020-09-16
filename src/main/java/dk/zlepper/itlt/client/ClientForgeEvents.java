package dk.zlepper.itlt.client;

import dk.zlepper.itlt.itlt;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.io.File;

@Mod.EventBusSubscriber(modid = itlt.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientForgeEvents {

    @SubscribeEvent
    public static void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        // Anti-cheat
        if (ClientConfig.enableAnticheat.get()) {
            // todo: report to server with list of known cheats (if any)

            if (ClientConfig.enableAutoRemovalOfCheats.get()) {
                // todo: check with a webserver for latest modids and checksums
                // todo: fingerprinting of known cheat mods (beyond just checksumming)
                // todo: analysis of mods to detect suspiciously cheaty code in unknown cheat mods
                // todo: detect FML tweaker type cheat mods
                ModList.get().getMods().forEach(modInfo -> {
                    final String modId = modInfo.getModId().toLowerCase();
                    final File modFile = modInfo.getOwningFile().getFile().getFilePath().toFile(); // todo: checksumming of cheat mods
                    if ((modId.contains("xray") && !modId.contains("anti")) // xray
                            || modId.equals("forgehax") || modId.equals("forgewurst")) {
                        final File cheatModFile = new File(String.valueOf(modInfo.getOwningFile().getFile().getFilePath()));
                        if (!cheatModFile.delete()) // if it can't be deleted immediately,
                            cheatModFile.deleteOnExit(); // delete it when the game closes or crashes
                    }
                });
            }
        }
    }
}
