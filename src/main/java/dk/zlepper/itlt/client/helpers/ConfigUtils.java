package dk.zlepper.itlt.client.helpers;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import dk.zlepper.itlt.itlt;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public class ConfigUtils {

    public static final Path configDir = FMLPaths.CONFIGDIR.get();

    public static UnmodifiableCommentedConfig readToml(final File tomlFile) throws IOException {
        return new TomlParser().parse(new FileReader(tomlFile.getPath(), StandardCharsets.UTF_8));
    }

    public static void writeToml(final UnmodifiableCommentedConfig config, final File tomlFile) throws IOException {
        new TomlWriter().write(config, new FileWriter(tomlFile.getPath(), StandardCharsets.UTF_8));
    }

    public static String getConfigVersion(final File tomlFile) throws IOException {
        return getConfigVersion(readToml(tomlFile));
    }

    public static String getConfigVersion(final UnmodifiableCommentedConfig config) {
        final Map<String, Object> configMap = config.valueMap();

        String configVer = "Unknown";

        // v2.1+
        try {
            configVer = ((UnmodifiableCommentedConfig) configMap.get("Internal")).valueMap().get("configVersion").toString();
        } catch (final Exception ignored) {}

        // v2.0
        try {
            final Map<String, Object> javaSection = ((UnmodifiableCommentedConfig) configMap.get("Java")).valueMap();
            configVer = ((UnmodifiableCommentedConfig) javaSection.get("Internal")).valueMap().get("configVersion").toString();
        } catch (final Exception ignored) {}

        /*
        // v1.0
        try {
            if (((UnmodifiableCommentedConfig) configMap.get("Display")).valueMap().size() > 0)
                configVer = "1.0.3";
        } catch (final Exception ignored) {}

        // v0.0.1
        try {
            configMap.get("bitdetection");
            configVer = "0.0.1";
        } catch (final Exception ignored) {}
        */

        return configVer.equals("unset") ? "Unknown" : configVer;
    }

    public static void backup() {
        final Path currentConfigPath = ConfigUtils.configDir.resolve("itlt-client.toml");
        final Path backupConfigPath = ConfigUtils.configDir.resolve("itlt-client.toml.bak");

        itlt.LOGGER.info("Backing up itlt config...");
        if (currentConfigPath.toFile().exists() && !currentConfigPath.toFile().isDirectory()) {
            try {
                Files.copy(currentConfigPath, backupConfigPath, StandardCopyOption.REPLACE_EXISTING);
                //if (Files.exists(backupConfigPath)) Files.delete(currentConfigPath);
                itlt.LOGGER.info("itlt config backup completed successfully");
            } catch (final IOException e) {
                itlt.LOGGER.warn("Failed to make a backup of itlt-client.toml");
                e.printStackTrace();
            }
        }
    }
}
