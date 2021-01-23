package dk.zlepper.itlt.server;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;

import java.nio.file.Path;

public final class ServerConfig {
    private static final ForgeConfigSpec.Builder serverConfigBuilder = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec serverConfig;

    static {
        // Anti-cheat section
        serverConfigBuilder.push("Anti-cheat");

        serverConfigBuilder.pop();

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
