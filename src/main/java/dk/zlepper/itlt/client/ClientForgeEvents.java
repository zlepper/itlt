package dk.zlepper.itlt.client;

// todo: check with a webserver for latest modids and checksums
// todo: fingerprinting of known cheat mods (beyond just checksumming)
// todo: analysis of mods to detect suspiciously cheaty code in unknown cheat mods
// todo: detect FML tweaker type cheat mods
// todo: report to server with list of known cheats (if any)

import dk.zlepper.itlt.client.helpers.ClientUtils;
import dk.zlepper.itlt.itlt;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

@Mod.EventBusSubscriber(modid = itlt.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientForgeEvents {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        if (ClientConfig.enableAnticheat.get()) {
            try {
                ClientUtils.getLatestDefinitions(Minecraft.getInstance());
            } catch (final IOException e) {
                e.printStackTrace();
            }

            ArrayList<ModInfo> listOfcheatMods = new ArrayList<>();

            ModList.get().getMods().forEach(modInfo -> {
                // by modId
                final String modId = modInfo.getModId().toLowerCase();
                if ((modId.contains("xray") && !modId.contains("anti"))
                        || modId.equals("forgehax") || modId.equals("forgewurst")) {
                    listOfcheatMods.add(modInfo);
                }

                // by checksum
                final File modFile = modInfo.getOwningFile().getFile().getFilePath().toFile();
                try {
                    final String modFileChecksum = ClientUtils.getFileChecksum(modFile);
                    itlt.LOGGER.warn(modFileChecksum);
                } catch (final IOException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            });

            if (ClientConfig.enableAutoRemovalOfCheats.get()) {
                listOfcheatMods.forEach(cheatMod -> {
                    final File cheatModFile = cheatMod.getOwningFile().getFile().getFilePath().toFile();
                    if (!cheatModFile.delete()) // if it can't be deleted immediately,
                        cheatModFile.deleteOnExit(); // delete it when the game closes or crashes
                });
            }
        }
    }
}
