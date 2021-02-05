package dk.zlepper.itlt.common;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dk.zlepper.itlt.itlt;
import net.minecraft.util.SharedConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AnticheatUtils {

    private static Triple<LocalDate, ChecksumType, Map<String, Object>> cachedDefinitions = null;

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getDefinitionsMap(ChecksumType checksumType) throws IOException, ClassCastException {
        if (checksumType == ChecksumType.Default) checksumType = ChecksumType.BLAKE3_224; // wow, an actual use of Java's mutable-by-default function args!

        final Map<String, Object> definitionsMap;

        // if there are definitions cached in memory, they're from today and they're of the same ChecksumType, use them
        if (cachedDefinitions != null
                && cachedDefinitions.getLeft().isEqual(LocalDate.now())
                && cachedDefinitions.getMiddle() == checksumType) {
            definitionsMap = cachedDefinitions.getRight();
        } else {
            // otherwise download the latest definitions and put them in the cache
            final String definitionsJson;
            if (checksumType == ChecksumType.None) {
                definitionsJson = downloadToString(new URL(
                        "https://raw.githubusercontent.com/zlepper/itlt/api/forge/v1.0/definitions/modids.json"));
            } else {
                definitionsJson = downloadToString(new URL(
                        "https://raw.githubusercontent.com/zlepper/itlt/api/forge/v1.0/definitions/"
                                + checksumType.toString().toLowerCase() + ".json"));
            }

            // convert the definitionsJson String to a Map
            final Type type = new TypeToken<Map<String, Object>>() {}.getType();
            definitionsMap = new Gson().fromJson(definitionsJson, type);
            cachedDefinitions = Triple.of(LocalDate.now(), checksumType, definitionsMap);
        }

        // trim off the version patch number, accounting for cases like 1.17.10 that have a double-digit patch number
        String mcVersion = SharedConstants.getVersion().getName();
        final String[] splitMcVersion = mcVersion.split(Pattern.quote("."));
        mcVersion = splitMcVersion[0] + "." + splitMcVersion[1];

        itlt.LOGGER.debug("mcVersion: " + mcVersion);
        itlt.LOGGER.debug("definitionsMap: " + definitionsMap.toString());

        return Collections.unmodifiableMap(
                (Map<String, Object>) definitionsMap.getOrDefault(mcVersion, Collections.<String, Object>emptyMap()));

        /* The definitions JSON looks something like this:
         *  {
         *      "1.16": {
         *          "modIds": [ "modid1", "modid2", "modid3" ],
         *          "checksums": [ "aabc", "e102", "8fc2" ]
         *      },
         *      "1.15": {
         *          (...)
         *      }
         *  }
         *
         * Patch releases such as 1.16.1, 1.16.2, etc... all point to 1.16
         */
    }

    public static String downloadToString(final URL url) throws IOException {
        final URLConnection connection = url.openConnection();
        final BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        return reader.lines().collect(Collectors.joining("\n"));
    }

    public static Pair<HashSet<String>, HashSet<String>> getDefinitions(ChecksumType checksumType) {
        if (checksumType == ChecksumType.Default) checksumType = ChecksumType.BLAKE3_224;

        final HashSet<String> cheatModIds = new HashSet<>();
        final HashSet<String> cheatModChecksums = new HashSet<>();
        try {
            // grab the latest definitions for the requested checksum type
            final Map<String, Object> definitionsMap = getDefinitionsMap(checksumType);
            itlt.LOGGER.debug("definitionsMap: " + definitionsMap);

            // try to get the modIds section and fallback to an empty value and show a warning if unable to
            cheatModIds.addAll((Collection<String>) definitionsMap.get("modIds"));
            itlt.LOGGER.debug("cheatModIds: " + cheatModIds);

            // same with the checksums section
            if (checksumType != ChecksumType.None) {
                cheatModChecksums.addAll((Collection<String>) definitionsMap.get("checksums"));
                itlt.LOGGER.debug("cheatModChecksums: " + cheatModChecksums);
            }

        } catch (final IOException e) {
            itlt.LOGGER.error("Unable to get latest definitions");
            e.printStackTrace();
        } catch (final ClassCastException | NullPointerException e) {
            itlt.LOGGER.error("Unable to parse latest definitions");
            e.printStackTrace();
        }

        if (cheatModIds.isEmpty()) itlt.LOGGER.warn("modIds section missing from latest definitions");
        if (checksumType != ChecksumType.None && cheatModChecksums.isEmpty())
            itlt.LOGGER.warn("checksums section missing from latest definitions");

        return Pair.of(cheatModIds, cheatModChecksums);
    }

    public static HashSet<String> getModIdDefinitions() {
        return getDefinitions(ChecksumType.None).getLeft();
    }

    public static boolean hasModIdListGotKnownCheats(final List<String> modList, final HashSet<String> cheatModIds) {
        for (final String s : modList) {
            final String modId = s.toLowerCase();

            // skip Forge and MC modIds from checks (they're a part of every FML setup afaik)
            if (!modId.equals("forge") && !modId.equals("minecraft")) {

                // simple algorithm for xray modIds and a hard-coded list of known cheat modIds
                if ((modId.contains("xray") && !modId.contains("anti"))
                        || modId.equals("forgehax") || modId.equals("forgewurst")) {
                    return true;
                }

                // known cheat modIds from definition file
                if (cheatModIds.contains(modId)) return true;
            }
        }
        return false;
    }
}
