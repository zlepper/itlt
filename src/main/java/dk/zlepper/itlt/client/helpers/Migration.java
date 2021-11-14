package dk.zlepper.itlt.client.helpers;

import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import dk.zlepper.itlt.client.ClientConfig;
import dk.zlepper.itlt.itlt;

import java.util.List;

public class Migration {

    // Copy over the differing values to the new latest spec's equivalents
    public static void migrate(String from, final String to, final UnmodifiableCommentedConfig oldConfig) {
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
                from += " (auto-detected)";

                // [Display] useTechnicIcon -> [Display.Icon] enableUsingAutodetectedIcon
                // [Display] loadCustomIcon -> [Display.Icon] enableCustomIcon
                ClientConfig.enableUsingAutodetectedIcon.set(oldConfig.get(List.of("Display", "useTechnicIcon")));
                ClientConfig.enableCustomIcon.set(oldConfig.get(List.of("Display", "loadCustomIcon")));

                // [Display] useTechnicDisplayName -> [Display.WindowTitle] enableUsingAutodetectedDisplayName
                ClientConfig.enableUsingAutodetectedDisplayName.set(oldConfig.get(List.of("Display", "useTechnicDisplayName")));

                // [Server] AddDedicatedServer -> [ServerList] enableCustomServerListEntries
                final boolean addDedicatedServer = oldConfig.get(List.of("Server", "AddDedicatedServer"));
                if (addDedicatedServer) {
                    ClientConfig.enableCustomServerListEntries.set(true);
                    itlt.LOGGER.warn(String.format("Unable to migrate SERVER_SERVER_NAME option from v%s to v%s", from, to));
                    itlt.LOGGER.warn(String.format("Unable to migrate SERVER_SERVER_IP option from v%s to v%s", from, to));
                }

                ClientConfig.configVersion.set("2.1.0");
            }
            default -> itlt.LOGGER.error(String.format("Migration failed: Unknown config version \"v%s\"", from));
        }
    }
}
