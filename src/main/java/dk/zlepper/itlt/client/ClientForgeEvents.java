package dk.zlepper.itlt.client;

// todo: caching of webserver definitions in case it fails or returns empty sections
// todo: fingerprinting of known cheat mods (beyond just checksumming)
// todo: analysis of mods to detect suspiciously cheaty code in unknown cheat mods
// todo: detect FML tweaker type cheat mods
// todo: report to server with list of known cheats (if any)
// todo: report to server when unable to get definitions or unable to check a mod
// todo: the references of modIDs should be removed from a copy of the file before checksumming
// todo: treat empty returned definitions as suspicious

import com.mojang.realmsclient.RealmsMainScreen;
import dk.zlepper.itlt.client.helpers.ChecksumUtils;
import dk.zlepper.itlt.common.AnticheatUtils;
import dk.zlepper.itlt.common.ChecksumType;
import dk.zlepper.itlt.itlt;
import io.lktk.NativeBLAKE3Util;
import net.minecraft.client.gui.AccessibilityScreen;
import net.minecraft.client.gui.screen.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Mod.EventBusSubscriber(modid = itlt.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientForgeEvents {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onGuiOpen(final GuiOpenEvent event) {
        if (ClientConfig.enableExplicitGC.get()) {
            final Screen screen = event.getGui();
            if (screen == null) return;
            itlt.LOGGER.debug("Screen: " + screen);

            // Tell the GC to run whenever the user does certain non-latency-critical actions such as being on the
            // pause screen or opening an opaque bg screen such as the Resource Pack or Controls screens.
            // Doing this can help reduce memory usage in certain situations and also slightly reduces the chances
            // of a large GC happening in the middle of gameplay.
            if ((ClientConfig.doExplicitGCOnPause.get() && screen.isPauseScreen())
                    || (ClientConfig.doExplicitGCOnSleep.get() && screen instanceof SleepInMultiplayerScreen)
                    || (ClientConfig.doExplicitGCOnMenu.get() && (
                            screen instanceof WorldSelectionScreen || screen instanceof MultiplayerScreen
                                    || screen instanceof PackScreen || screen instanceof LanguageScreen
                                    || screen instanceof ChatOptionsScreen || screen instanceof ControlsScreen
                                    || screen instanceof AccessibilityScreen || screen instanceof RealmsMainScreen
                                    || screen instanceof StatsScreen))) {
                Runtime.getRuntime().gc();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        // don't do anti-cheat stuff if enableAnticheat is false
        if (!ClientConfig.enableAnticheat.get()) return;

        // grab the definitions
        final Pair<HashSet<String>, HashSet<String>> definitions =
                AnticheatUtils.getDefinitions(ClientConfig.preferredChecksumType.get());
        final Set<String> cheatModIds = Collections.unmodifiableSet(definitions.getLeft());
        final Set<String> cheatModChecksums = Collections.unmodifiableSet(definitions.getRight());

        // assume the best in people until they prove otherwise - set the initial capacity of the ArrayList to 0
        final ArrayList<ModInfo> listOfDetectedCheatMods = new ArrayList<>(0);

        final List<ModInfo> modInfoList = ModList.get().getMods();
        final Stream<ModInfo> modInfoStream;

        // there's an overhead involved in parallel streams that may make it run slower than sequential if you're only
        // doing a small amount of work, so only use it if there's *a lot* of mods to iterate through
        if (modInfoList.size() > ClientConfig.parallelModChecksThreshold.get()) modInfoStream = modInfoList.parallelStream();
        else modInfoStream = modInfoList.stream();
        itlt.LOGGER.debug("isParallel: " + modInfoStream.isParallel());

        modInfoStream.forEach(modInfo -> {
            // by modId
            final String modId = modInfo.getModId().toLowerCase();

            // skip Forge and MC modIds from checks (they're a part of every FML setup afaik)
            if (!modId.equals("forge") && !modId.equals("minecraft")) {

                if (AnticheatUtils.hasModIdStringGotKnownCheats(modId, cheatModIds))
                    listOfDetectedCheatMods.add(modInfo);

                // by class checksum
                final ModFile modFile = modInfo.getOwningFile().getFile();
                itlt.LOGGER.debug("-----");
                itlt.LOGGER.debug("modId: " + modId);
                itlt.LOGGER.debug("modFile: " + modFile.getFilePath());

                try {
                    final ZipFile zipFile = new ZipFile(modFile.getFilePath().toFile());

                    // for each file in the jar
                    final Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
                    while (enumeration.hasMoreElements()) {
                        final ZipEntry entry = enumeration.nextElement();
                        final String entryName = entry.getName();

                        // Skip the assets folder and non-class files
                        if (!entryName.endsWith(".class")) continue;
                        if (entryName.startsWith("assets/") && !entryName.endsWith(".class")) continue;

                        // Log the path of the class file found in the jar
                        itlt.LOGGER.debug("modClassFilePath: " + entryName);

                        // Extract the class file from the jar to a byte[]
                        final BufferedInputStream bufferedInputStream = new BufferedInputStream(zipFile.getInputStream(entry));
                        final int bufferSize = bufferedInputStream.available();
                        itlt.LOGGER.debug("bufferSize: " + bufferSize);
                        final byte[] entryBytes = new byte[bufferSize];
                        final int readResult = bufferedInputStream.read(entryBytes, 0, bufferSize);
                        bufferedInputStream.close();

                        // https://www.jrebel.com/blog/solution-smallest-java-class-file-challenge
                        // discovered min is around 38, putting 32 here just to be safe
                        if (readResult < 32)
                            if (readResult == 0) throw new IOException("Read zero bytes, expected ~" + bufferSize);
                            else throw new IOException("Read fewer bytes than the theoretical minimum of a valid class file");

                        // Hash the byte array
                        final Pair<ChecksumType, String> modClassChecksum = ChecksumUtils.getChecksum(entryBytes);
                        itlt.LOGGER.debug("modClassChecksum: " + modClassChecksum.toString());

                        // if the mod class' checksum is in the definition file, add it to the list of detected cheat mods
                        if (cheatModChecksums.contains(modClassChecksum.getRight())) listOfDetectedCheatMods.add(modInfo);
                    }

                    zipFile.close();
                } catch (final IOException | NativeBLAKE3Util.InvalidNativeOutput e) {
                    itlt.LOGGER.warn("Unable to calculate checksum for a class in \"" + modFile.getFilePath() + "\"");
                    e.printStackTrace();
                }
            }
        });
        itlt.LOGGER.debug("-----");

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
