package dk.zlepper.itlt.server;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import dk.zlepper.itlt.common.ChecksumType;
import net.minecraftforge.common.ForgeConfigSpec;

import java.nio.file.Path;

public final class ServerConfig {
    private static final ForgeConfigSpec.Builder serverConfigBuilder = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec serverConfig;

    public static ForgeConfigSpec.BooleanValue enableAnticheat;

    public static ForgeConfigSpec.ConfigValue<Integer> parallelModChecksThreshold;

    public static ForgeConfigSpec.ConfigValue<ChecksumType> preferredChecksumType;

    static {
        // Anti-cheat section
        serverConfigBuilder.push("Anti-cheat"); {
            enableAnticheat = serverConfigBuilder
                    .comment("")
                    .define("enableAnticheat", true);

            // Anti-cheat.Advanced
            /*serverConfigBuilder.push("Advanced"); {
                parallelModChecksThreshold = serverConfigBuilder
                        .comment("\r\nTo check if a known cheat mod is present, itlt needs to iterate through each " +
                                        "currently loaded mod.",

                                "These checks are pretty fast, but can still benefit from multithreading if there " +
                                        "are *a lot* of mods.",

                                "You can change the threshold for how many mods are needed before multithreading is " +
                                        "attempted here. The default is >100 mods.",

                                "Warning: There's an overhead associated with multithreading so having the threshold " +
                                        "too low can actually hurt performance.")
                        .defineInRange("parallelModChecksThreshold", 100, 1, 1024);
                preferredChecksumType = serverConfigBuilder
                        .comment("")
                        .defineEnum("preferredChecksumType", ChecksumType.Default);
            }*/
        } serverConfigBuilder.pop();

        // Build the config
        serverConfig = serverConfigBuilder.build();
    }

    public static void loadConfig(ForgeConfigSpec spec, final Path path) {
        final CommentedFileConfig configData = CommentedFileConfig.builder(path)
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();

        configData.load();
        spec.setConfig(configData);
    }
}
