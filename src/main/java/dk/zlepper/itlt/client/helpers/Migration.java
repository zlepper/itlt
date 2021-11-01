package dk.zlepper.itlt.client.helpers;

import com.electronwill.nightconfig.core.CommentedConfig;
import dk.zlepper.itlt.client.ClientConfig;
import dk.zlepper.itlt.itlt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class Migration {

    /*public enum Config {
        v2_1_0, v2_0_0;

        public Map<String, Object> map;
        Config(final Map<String, Object> map) {
            this.map = map;
        }

        Config() {}
    }*/

    // Copy over the differing values to the new latest spec's equivalents
    public static void migrate(final String from, final String to, final CommentedConfig oldConfig) {
        switch (from) {
            case "2.0.1", "2.0.0" -> {
                // v2.1.0 fixed a bug where all config options were inside the Java group.
                // e.g. [Java.Display.WindowTitle] in v2.0.0 becomes [Display.WindowTitle] in v2.1.0
                ClientConfig.enableCustomWindowTitle.set(oldConfig.get(List.of("Java", "Display", "WindowTitle", "enableCustomWindowTitle")));
                ClientConfig.customWindowTitleText.set(oldConfig.get(List.of("Java", "Display", "WindowTitle", "customWindowTitleText")));
                ClientConfig.enableUsingAutodetectedDisplayName.set(oldConfig.get(List.of("Java", "Display", "WindowTitle", "enableUsingAutodetectedDisplayName")));
                ClientConfig.autoDetectedDisplayNameFallback.set(oldConfig.get(List.of("Java", "Display", "WindowTitle", "autoDetectedDisplayNameFallback")));

                // [Java.Display.Icon] -> [Display.Icon]
                ClientConfig.enableEnhancedVanillaIcon.set(oldConfig.get(List.of("Java", "Display", "Icon", "enableEnhancedVanillaIcon")));
                ClientConfig.enableCustomIcon.set(oldConfig.get(List.of("Java", "Display", "Icon", "enableCustomIcon")));
                ClientConfig.enableUsingAutodetectedIcon.set(oldConfig.get(List.of("Java", "Display", "Icon", "enableUsingAutodetectedIcon")));

                // [Java.ServerList] -> [ServerList]
                ClientConfig.enableCustomServerListEntries.set(oldConfig.get(List.of("Java", "ServerList", "enableCustomServerListEntries")));

                ClientConfig.configVersion.set("2.1.0");
            }
            case "1.0.3" -> {
                // todo
            }
            default -> itlt.LOGGER.error(String.format("Migration failed: Unknown config version \"%s\"", from));
        }
    }
}
