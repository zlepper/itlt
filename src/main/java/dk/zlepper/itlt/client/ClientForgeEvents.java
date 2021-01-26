package dk.zlepper.itlt.client;

// todo: caching of webserver definitions in case it fails or returns empty sections
// todo: fingerprinting of known cheat mods (beyond just checksumming)
// todo: analysis of mods to detect suspiciously cheaty code in unknown cheat mods
// todo: detect FML tweaker type cheat mods
// todo: report to server with list of known cheats (if any)
// todo: report to server when unable to get definitions or unable to check a mod
// todo: cache results of checks after first login and reuse for future logins for the game session
//       (purge cache on game close/crash/restart)
// todo: treat empty returned definitions as suspicious

import dk.zlepper.itlt.client.helpers.ClientUtils;
import dk.zlepper.itlt.itlt;
import io.lktk.NativeBLAKE3Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

@Mod.EventBusSubscriber(modid = itlt.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientForgeEvents {

    @SubscribeEvent(priority = EventPriority.HIGH)
    @SuppressWarnings("unchecked") // Casts are checked and handled using a try/catch block for ClassCastExceptions
    public static void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        if (ClientConfig.enableAnticheat.get()) {
            // definition updates
            HashSet<String> cheatModIds = new HashSet<>();
            HashSet<String> cheatModChecksums = new HashSet<>();
            try {
                // grab the latest definitions for the requested checksum type
                final Map<String, Object> latestDefinitions = ClientUtils.getLatestDefinitions(ClientUtils.ChecksumType.BLAKE3_224);
                itlt.LOGGER.debug("latestDefinitions: " + latestDefinitions);

                // try to get the modIds section and fallback to an empty value and show a warning if unable to
                cheatModIds.addAll((Collection<String>) latestDefinitions.get("modIds"));
                itlt.LOGGER.debug("cheatModIds: " + cheatModIds);

                // same with the checksums section
                cheatModChecksums.addAll((Collection<String>) latestDefinitions.get("checksums"));
                itlt.LOGGER.debug("cheatModChecksums: " + cheatModChecksums);

            } catch (final IOException e) {
                itlt.LOGGER.error("Unable to get latest definitions");
                e.printStackTrace();
            } catch (final ClassCastException | NullPointerException e) {
                itlt.LOGGER.error("Unable to parse latest definitions");
                e.printStackTrace();
            }

            ArrayList<ModInfo> listOfDetectedCheatMods = new ArrayList<>(0);

            final ModList modList = ModList.get();
            final Stream<ModInfo> modInfoStream;

            // there's a significant overhead involved in parallel streams that may make it run slower than sequential if
            // you're only doing a small amount of work, therefore we only use it if there's *a lot* of mods to iterate through
            if (modList.getMods().size() > ClientConfig.parallelModChecksThreshold.get()) modInfoStream = modList.getMods().parallelStream();
            else modInfoStream = modList.getMods().stream();
            itlt.LOGGER.debug("isParallel: " + modInfoStream.isParallel());

            if (cheatModIds.isEmpty()) itlt.LOGGER.warn("modIds section missing from latest definitions");
            if (cheatModChecksums.isEmpty()) itlt.LOGGER.warn("checksums section missing from latest definitions");
            final Set<String> finalCheatModIds = Collections.unmodifiableSet(cheatModIds);
            final Set<String> finalCheatModChecksums = Collections.unmodifiableSet(cheatModChecksums);

            modInfoStream.forEach(modInfo -> {
                // by modId
                final String modId = modInfo.getModId().toLowerCase();

                // skip Forge and MC modIds from checks (they're a part of every FML setup afaik)
                if (!modId.equals("forge") && !modId.equals("minecraft")) {

                    // simple algorithm for xray modIds and a hard-coded list of known cheat modIds
                    if ((modId.contains("xray") && !modId.contains("anti"))
                            || modId.equals("forgehax") || modId.equals("forgewurst")) {
                        listOfDetectedCheatMods.add(modInfo);
                    }

                    // known cheat modIds from definition file
                    if (finalCheatModIds.contains(modId)) listOfDetectedCheatMods.add(modInfo);

                    // by file checksum
                    final File modFile = modInfo.getOwningFile().getFile().getFilePath().toFile();
                    try {
                        final Pair<ClientUtils.ChecksumType, String> modFileChecksum = ClientUtils.getFileChecksum(modFile);
                        itlt.LOGGER.debug("");
                        itlt.LOGGER.debug("modId: " + modId);
                        itlt.LOGGER.debug("modFile: " + modFile.toPath().toString());
                        itlt.LOGGER.debug("modFileChecksum: " + modFileChecksum.toString());

                        // known cheat checksums from definition file
                        if (finalCheatModChecksums.contains(modFileChecksum.getRight()))
                            listOfDetectedCheatMods.add(modInfo);

                    } catch (final IOException | NativeBLAKE3Util.InvalidNativeOutput e) {
                        final StringBuilder warningMsgBuilder = new StringBuilder();
                        warningMsgBuilder.append("Unable to calculate checksum for \"").append(modFile.getPath()).append("\"");

                        final boolean fileNonExistentOrInaccessible =
                                !modFile.exists() || e.getMessage().toLowerCase().contains("access is denied");

                        if (fileNonExistentOrInaccessible)
                            warningMsgBuilder.append(" because itlt can't find or access it on the filesystem.");

                        itlt.LOGGER.warn(warningMsgBuilder.toString());
                        if (!fileNonExistentOrInaccessible) e.printStackTrace();
                    }
                }
            });

            listOfDetectedCheatMods.forEach(cheatMod ->
                    itlt.LOGGER.debug("Found cheat mod: \"" + cheatMod.getOwningFile().getFile().getFileName() + "\""));

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
