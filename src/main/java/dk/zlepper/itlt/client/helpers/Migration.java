package dk.zlepper.itlt.client.helpers;

import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import dk.zlepper.itlt.client.ClientConfig;
import dk.zlepper.itlt.itlt;
import net.minecraftforge.common.ForgeConfigSpec;

import java.lang.reflect.Field;
import java.util.*;

public class Migration {

    private static HashMap<String, Field> configFields;

    /**
     * Recursively applies any config options from the oldConfig that are the same format as the currently loaded version.
     * @param oldConfig the config file to migrate from
     * @param blacklist a Set of values to ignore from the old config so that they are skipped
     */
    private static void migrateSameFormat(final UnmodifiableCommentedConfig oldConfig, final Set<String> blacklist) {
        if (configFields == null) {
            configFields = new HashMap<>(ClientConfig.class.getDeclaredFields().length);

            // load the variables from ClientConfig.java as a map of Fields
            for (final Field declaredField : ClientConfig.class.getDeclaredFields()) {
                // filter out fields that aren't used for this migration process
                if (declaredField.getType().toString().contains("ForgeConfigSpec") && !declaredField.getName().equals("clientConfig"))
                    configFields.put(declaredField.getName(), declaredField);
            }
        }

        for (final Map.Entry<String, Object> entry : oldConfig.valueMap().entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();

            // todo: make sure this line is working as intended
            if (value instanceof final UnmodifiableCommentedConfig valueAsMap)
                migrateSameFormat(valueAsMap, blacklist);

            // skip any keys on that are in the blacklist
            if (blacklist.contains(key)) continue;

            // if we find a variable of type ForgeConfigSpec.* in ClientConfig that matches a key in the oldConfig
            if (configFields.containsKey(key)) {
                try {
                    Field field = ClientConfig.class.getDeclaredField(key);

                    // determine the type of the variable and call its underlying set() method, casting the key's
                    // corresponding value to the appropriate type

                    // note: the set() method here calls ForgeConfigSpec.*'s set() - we don't set the variable directly
                    // so that NightConfig is aware a change has been made and updates the underlying file accordingly
                    if (field.getType().equals(ForgeConfigSpec.BooleanValue.class))
                        ((ForgeConfigSpec.BooleanValue) field.get(Object.class)).set((boolean) value);
                    else if (field.getType().equals(ForgeConfigSpec.DoubleValue.class))
                        ((ForgeConfigSpec.DoubleValue) field.get(Object.class)).set((double) value);
                    else if (field.getType().equals(ForgeConfigSpec.IntValue.class))
                        ((ForgeConfigSpec.IntValue) field.get(Object.class)).set((int) value);
                    else if (field.getGenericType().getTypeName().equals("net.minecraftforge.common.ForgeConfigSpec$ConfigValue<java.lang.String>"))
                        ((ForgeConfigSpec.ConfigValue<String>) field.get(Object.class)).set((String) value);
                    else itlt.LOGGER.debug("Skipping unknown/unsupported variable type "
                                + "\"" + field.getGenericType().getTypeName() + "\" encountered during migration");
                } catch (final Exception e) {
                    itlt.LOGGER.info(String.format("Unable to migrate \"%s\" with value \"%s\" to v%s", key, value, itlt.VERSION));
                    itlt.LOGGER.debug(e);
                    itlt.LOGGER.debug(e.getStackTrace());
                }
            }
        }
    }

    // Copy over the differing values to the new latest spec's equivalents
    public static void migrate(String from, final String to, final UnmodifiableCommentedConfig oldConfig) {
        switch (from) {
            case itlt.VERSION, "2.1.4", "2.1.3", "2.1.2", "2.1.1", "2.1.0" -> {
                // no changes
                migrateSameFormat(oldConfig, Set.of());
                ClientConfig.configVersion.set(itlt.VERSION);
            }
            case "2.0.1", "2.0.0" -> {
                // v2.1.0 fixed a bug where all config options were inside the Java group.
                // e.g. [Java.Display.WindowTitle] in v2.0.0 becomes [Display.WindowTitle] in v2.1.0
                ClientConfig.enableCustomWindowTitle.set(oldConfig.get("Java.Display.WindowTitle.enableCustomWindowTitle"));
                ClientConfig.customWindowTitleText.set(oldConfig.get("Java.Display.WindowTitle.customWindowTitleText"));
                ClientConfig.enableUsingAutodetectedDisplayName.set(oldConfig.get("Java.Display.WindowTitle.enableUsingAutodetectedDisplayName"));
                ClientConfig.autoDetectedDisplayNameFallback.set(oldConfig.get("Java.Display.WindowTitle.autoDetectedDisplayNameFallback"));

                // [Java.Display.Icon] -> [Display.Icon]
                ClientConfig.enableEnhancedVanillaIcon.set(oldConfig.get("Java.Display.Icon.enableEnhancedVanillaIcon"));
                ClientConfig.enableCustomIcon.set(oldConfig.get("Java.Display.Icon.enableCustomIcon"));
                ClientConfig.enableUsingAutodetectedIcon.set(oldConfig.get("Java.Display.Icon.enableUsingAutodetectedIcon"));

                // [Java.ServerList] -> [ServerList]
                ClientConfig.enableCustomServerListEntries.set(oldConfig.get("Java.ServerList.enableCustomServerListEntries"));

                // [Java.Advanced.ExplicitGC] explicitGCOnX -> [Java.Advanced.ExplicitGC] doExplicitGCWhen["x"]
                final List<String> triggersList = new ArrayList<>(3);
                if ((boolean) oldConfig.get("Java.Advanced.ExplicitGC.explicitGCOnPause"))
                    triggersList.add(ClientConfig.explicitGCTriggers.Pause.toString());
                if ((boolean) oldConfig.get("Java.Advanced.ExplicitGC.explicitGCOnSleep"))
                    triggersList.add(ClientConfig.explicitGCTriggers.Sleep.toString());
                if ((boolean) oldConfig.get("Java.Advanced.ExplicitGC.explicitGCOnMenu"))
                    triggersList.add(ClientConfig.explicitGCTriggers.Menu.toString());
                ClientConfig.doExplicitGCWhen.set(triggersList);

                // fix a couple of typos
                ClientConfig.requiredMaxJavaVersion.set(oldConfig.get("Java.Version.Max.Requirement.requiredMaxJavaVerion"));
                ClientConfig.requiredMinJavaVersion.set(oldConfig.get("Java.Version.Min.Requirement.requiredMinJavaVerion"));

                final var ignoreList = new HashSet<>(
                        List.of("enableCustomWindowTitle", "customWindowTitleText", "enableUsingAutodetectedDisplayName",
                                "autoDetectedDisplayNameFallback", "enableEnhancedVanillaIcon", "enableCustomIcon",
                                "enableUsingAutodetectedIcon", "enableCustomServerListEntries", "configVersion"));

                // Copy over the remaining settings that haven't changed format
                migrateSameFormat(oldConfig, ignoreList);

                ClientConfig.configVersion.set(itlt.VERSION);
            }
            case "1.0.3" -> {
                from += " (auto-detected)";

                // [Display] useTechnicIcon -> [Display.Icon] enableUsingAutodetectedIcon
                // [Display] loadCustomIcon -> [Display.Icon] enableCustomIcon
                ClientConfig.enableUsingAutodetectedIcon.set(oldConfig.get("Display.useTechnicIcon"));
                ClientConfig.enableCustomIcon.set(oldConfig.get("Display.loadCustomIcon"));

                // [Display] useTechnicDisplayName -> [Display.WindowTitle] enableUsingAutodetectedDisplayName
                ClientConfig.enableUsingAutodetectedDisplayName.set(oldConfig.get("Display.useTechnicDisplayName"));

                // [Server] AddDedicatedServer -> [ServerList] enableCustomServerListEntries
                final boolean addDedicatedServer = oldConfig.get("Server.AddDedicatedServer");
                if (addDedicatedServer) {
                    ClientConfig.enableCustomServerListEntries.set(true);
                    itlt.LOGGER.warn(String.format("Unable to migrate SERVER_SERVER_NAME option from v%s to v%s", from, to));
                    itlt.LOGGER.warn(String.format("Unable to migrate SERVER_SERVER_IP option from v%s to v%s", from, to));
                }

                ClientConfig.configVersion.set(itlt.VERSION);
            }
            default -> itlt.LOGGER.error(String.format("Migration failed: Unknown config version \"v%s\"", from));
        }
    }
}
