package dk.zlepper.itlt.client;

// todo: caching of webserver definitions in case it fails or returns empty sections
// todo: fingerprinting of known cheat mods (beyond just checksumming)
// todo: analysis of mods to detect suspiciously cheaty code in unknown cheat mods
// todo: detect FML tweaker type cheat mods
// todo: report to server with list of known cheats (if any)

import dk.zlepper.itlt.client.helpers.ClientUtils;
import dk.zlepper.itlt.itlt;
import io.lktk.NativeBLAKE3;
import io.lktk.NativeBLAKE3Util;
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
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

@Mod.EventBusSubscriber(modid = itlt.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientForgeEvents {

    @SubscribeEvent(priority = EventPriority.HIGH)
    @SuppressWarnings("unchecked") // Cast checking is handled through the try/catch block and fallbacks of the same type, Java's just being overly paranoid here.
    public static void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        if (ClientConfig.enableAnticheat.get()) {

            // definition updates
            ArrayList<String> cheatModIds = new ArrayList<>();
            ArrayList<String> cheatModChecksums = new ArrayList<>();
            try {
                // for systems where the NativeBLAKE3 lib has not been compiled for, fallback to SHA-512
                final Map<String, Object> latestDefinitions;
                if (NativeBLAKE3.isEnabled()) latestDefinitions = ClientUtils.getLatestDefinitions(ClientUtils.ChecksumType.Modern);
                else latestDefinitions = ClientUtils.getLatestDefinitions(ClientUtils.ChecksumType.Fallback);
                itlt.LOGGER.debug("latestDefinitions: " + latestDefinitions);

                // try to get the modIds section and fallback to an empty value and show a warning if unable to
                cheatModIds = (ArrayList<String>) latestDefinitions.getOrDefault("modIds", new ArrayList<String>(0));
                if (cheatModIds.isEmpty()) itlt.LOGGER.warn("modIds section missing from latest definitions");
                itlt.LOGGER.debug("cheatModIds: " + cheatModIds);

                // same with the checksums section
                cheatModChecksums = (ArrayList<String>) latestDefinitions.getOrDefault("checksums", new ArrayList<String>(0));
                if (cheatModChecksums.isEmpty()) itlt.LOGGER.warn("checksums section missing from latest definitions");
                itlt.LOGGER.debug("cheatModChecksums: " + cheatModChecksums);
            } catch (final IOException e) {
                itlt.LOGGER.error("Unable to get latest definitions");
                e.printStackTrace();
            } catch (final ClassCastException | NullPointerException e) {
                itlt.LOGGER.error("Unable to parse latest definitions");
                e.printStackTrace();
            }
            final ArrayList<String> finalCheatModIds = cheatModIds;
            final ArrayList<String> finalCheatModChecksums = cheatModChecksums;

            ArrayList<ModInfo> listOfDetectedCheatMods = new ArrayList<>();

            // there's a significant overhead involved in parallel streams that may make it run slower than sequential if
            // you're only doing a small amount of work, therefore we only use it if there's *a lot* of mods to iterate through
            final Stream<ModInfo> modInfoStream;
            if (ModList.get().getMods().size() > 100) modInfoStream = ModList.get().getMods().parallelStream();
            else modInfoStream = ModList.get().getMods().stream();
            itlt.LOGGER.debug("isParallel: " + modInfoStream.isParallel());

            modInfoStream.forEach(modInfo -> {
                // by modId
                final String modId = modInfo.getModId().toLowerCase();

                // simple algorithm for xray modIds and a hard-coded list of known cheat modIds
                if ((modId.contains("xray") && !modId.contains("anti"))
                        || modId.equals("forgehax") || modId.equals("forgewurst")) {
                    listOfDetectedCheatMods.add(modInfo);
                }

                // known cheat modIds from definition file
                finalCheatModIds.forEach(cheatModId -> {
                    if (modId.equalsIgnoreCase(cheatModId)) listOfDetectedCheatMods.add(modInfo);
                });

                // by checksum
                final File modFile = modInfo.getOwningFile().getFile().getFilePath().toFile();
                try {
                    final Object[] modFileChecksum = ClientUtils.getFileChecksum(modFile);
                    itlt.LOGGER.debug("");
                    itlt.LOGGER.debug("modId: " + modId);
                    itlt.LOGGER.debug("modFile: " + modFile.toPath().toString());
                    itlt.LOGGER.debug("modFileChecksum: " + Arrays.toString(modFileChecksum));

                    // known cheat checksums from definition file
                    finalCheatModChecksums.forEach(cheatModChecksum -> {
                        if (modFileChecksum[1].toString().equals(cheatModChecksum)) listOfDetectedCheatMods.add(modInfo);
                    });
                } catch (final IOException | NoSuchAlgorithmException | NativeBLAKE3Util.InvalidNativeOutput e) {
                    itlt.LOGGER.warn("Unable to calculate checksum for " + modFile.getPath());
                    e.printStackTrace();
                }
            });

            listOfDetectedCheatMods.forEach(cheatMod -> itlt.LOGGER.debug("Found cheat mod: \"" + cheatMod.getOwningFile().getFile().getFileName() + "\""));

            if (ClientConfig.enableAutoRemovalOfCheats.get()) {
                listOfDetectedCheatMods.forEach(cheatMod -> {
                    final File cheatModFile = cheatMod.getOwningFile().getFile().getFilePath().toFile();
                    if (!cheatModFile.delete()) // if it can't be deleted immediately,
                        cheatModFile.deleteOnExit(); // delete it when the game closes or crashes
                });
            }
        }
    }
}
