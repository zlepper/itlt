package dk.zlepper.itlt.common;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dk.zlepper.itlt.itlt;
import net.minecraft.util.SharedConstants;
import org.apache.commons.lang3.tuple.Triple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AnticheatUtils {

    private static Triple<LocalDate, ChecksumType, Map<String, Object>> cachedDefinitions = null;

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getLatestDefinitions(ChecksumType checksumType) throws IOException, ClassCastException {
        if (checksumType == ChecksumType.Default) checksumType = ChecksumType.BLAKE3_224; // wow, an actual use of Java's mutable-by-default function args!

        final Map<String, Object> definitionsMap;

        // if there are definitions cached in memory, they're from today and they're of the same ChecksumType, use them
        if (cachedDefinitions != null
                && cachedDefinitions.getLeft().isEqual(LocalDate.now())
                && cachedDefinitions.getMiddle() == checksumType) {
            definitionsMap = cachedDefinitions.getRight();
        } else {
            // otherwise download the latest definitions and put them in the cache
            final String definitionsJson = downloadToString(new URL(
                    "https://raw.githubusercontent.com/zlepper/itlt/api/forge/v1.0/definitions/"
                        + checksumType.toString().toLowerCase() + ".json"));

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

    private static String downloadToString(final URL url) throws IOException {
        final URLConnection connection = url.openConnection();
        final BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        return reader.lines().collect(Collectors.joining("\n"));
    }
}
