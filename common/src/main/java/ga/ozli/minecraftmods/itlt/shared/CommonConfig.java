package ga.ozli.minecraftmods.itlt.shared;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Collections;

public final class CommonConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder() {
        // ITLT uses nested push/pop calls to create a hierarchy of config options rather than using a dot-separated string.
        // This method is overridden to skip the path parsing and directly push the group name as a singleton list for performance reasons.
        @Override
        public ForgeConfigSpec.Builder push(String groupName) {
            return super.push(Collections.singletonList(groupName));
        }
    };

    private static final ForgeConfigSpec.ConfigValue<String> JAVA_VER_GUIDE;
    private static final ForgeConfigSpec.ByteValue MIN_JAVA_WARN;
    private static final ForgeConfigSpec.ByteValue MAX_JAVA_WARN;

    private static final ForgeConfigSpec.ConfigValue<String> MEM_GUIDE;
    private static final ForgeConfigSpec.FloatValue MIN_MEM_WARN;
    private static final ForgeConfigSpec.FloatValue MAX_MEM_WARN;
    private static final ForgeConfigSpec.FloatValue NEAR_MAX_MEM_WARN;

    private static final ForgeConfigSpec.BooleanValue GC_ON_STARTUP;

    static final ForgeConfigSpec.ConfigValue<String> PACK_NAME;
    static final ForgeConfigSpec.ConfigValue<String> PACK_VER;
    static final ForgeConfigSpec.ConfigValue<String> PACK_SUPPORT_URL;

    public static final ForgeConfigSpec SPEC;

    static {
        BUILDER.push("Java"); {

            BUILDER.push("Version"); {
                JAVA_VER_GUIDE = BUILDER.define("guideUrl", "");
                MIN_JAVA_WARN = BUILDER
                        .comment(
                                " ",
                                " The minimum recommended Java version to run the modpack.",
                                " Newer versions may have better performance, but some mods may break.",
                                " Depending on the launcher being used, it may be difficult/annoying for users to change the Java version used for starting your modpack.",
                                " ",
                                " The default is Java 21, which is the minimum version required by Minecraft 1.20.5+."
                        )
                        .defineInRange("min", (byte) 21, (byte) 21, (byte) 127);
                MAX_JAVA_WARN = BUILDER
                        .comment(
                                " ",
                                " The maximum recommended Java version to run the modpack.",
                                " If you know that a mod in your pack is buggy with a certain Java version, you can warn the user against using too new Java here.",
                                " ",
                                " To disable this warning, set the value to 0."
                        )
                        .defineInRange("max", (byte) 0, (byte) 0, (byte) 127);
            } BUILDER.pop();

            BUILDER.push("Memory"); {
                MEM_GUIDE = BUILDER.define("guideUrl", "");
                MIN_MEM_WARN = BUILDER
                        .comment(
                                " ",
                                " If you know that your modpack fails to load into a world if the player allocates too little memory, you can set a minimum memory warning here.",
                                " Do not set the minimum too high, as allocating too much memory can hurt performance and prevent the game from starting on low-end PCs.",
                                " ",
                                " To disable this warning, set the value to 0."
                        )
                        .defineInRange("min", 1f, 0f, 1024f);
                MAX_MEM_WARN = BUILDER
                        .comment(
                                "",
                                " Allocating too much memory can hurt performance and cause lag spikes - you can set a max memory warning to help prevent that.",
                                " ",
                                " To disable this warning, set the value to 0."
                        )
                        .defineInRange("max", 0f, 0f, 1024f);
                NEAR_MAX_MEM_WARN = BUILDER
                        .comment(
                                " ",
                                " Some players may allocate more memory than they have in their PC, or allocate all of it without leaving any spare for the OS, drivers and other apps.",
                                " This can severely hurt performance because it makes the game fight with everything else for resources and spills over from RAM to the much slower swap space.",
                                " You can set a warning when the allocated memory is near the maximum available memory to help prevent that.",
                                " The default is to warn when there's less than 1GB spare memory left.",
                                " ",
                                " To disable this warning, set the value to 0."
                        )
                        .defineInRange("nearMax", 1f, 0f, 3f);
            } BUILDER.pop();

            BUILDER.push("Advanced"); {
                GC_ON_STARTUP = BUILDER
                        .comment(
                                " ",
                                " Run an explicit full GC after the game has finished loading.",
                                " This can help reduce initial memory usage, but causes a one-time brief pause after the game loads.",
                                " ",
                                " Warning: This option has no effect if the -XX:+DisableExplicitGC JVM arg is present."
                        )
                        .define("enableExplicitGC", true);
            } BUILDER.pop();

        } BUILDER.pop();

        BUILDER.push("ModpackInfo"); {
            PACK_NAME = BUILDER
                    .comment(
                            " ",
                            " The name of your modpack"
                    )
                    .define("name", "");
            PACK_VER = BUILDER
                    .comment(
                            " ",
                            " The version of your modpack"
                    )
                    .define("version", "");
            PACK_SUPPORT_URL = BUILDER
                    .comment(
                            " ",
                            " The URL to your modpack's support page for players to get help with crashes"
                    )
                    .define("supportUrl", "https://discord.minecraftforge.net");
        } BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static final class Java {
        static final class Version {
            static final String GUIDE = CommonConfig.JAVA_VER_GUIDE.get();
            static final byte MIN = CommonConfig.MIN_JAVA_WARN.get();
            static final byte MAX = CommonConfig.MAX_JAVA_WARN.get();

            // ensure the class is loaded so that the static fields are initialised at this point
            private static void load() {}

            private Version() {}
        }

        static final class Memory {
            static final String GUIDE = CommonConfig.MEM_GUIDE.get();
            static final float MIN = CommonConfig.MIN_MEM_WARN.get();
            static final float MAX = CommonConfig.MAX_MEM_WARN.get();
            static final float NEAR_MAX = CommonConfig.NEAR_MAX_MEM_WARN.get();

            private static void load() {}

            private Memory() {}
        }

        public static final class Advanced {
            public static final boolean GC_ON_STARTUP = CommonConfig.GC_ON_STARTUP.get();

            private static void load() {}

            private Advanced() {}
        }

        private Java() {}
    }

    public record ModpackInfo(String name, String version, String supportUrl) {
        public static final ModpackInfo INSTANCE = new ModpackInfo(
                CommonConfig.PACK_NAME.get(), CommonConfig.PACK_VER.get(), CommonConfig.PACK_SUPPORT_URL.get()
        );

        public ModpackInfo {
            assert name != null;
            assert version != null;
            assert supportUrl != null;

            if (name.isBlank())
                name = "Unknown";

            if (version.isBlank())
                version = "Unknown";

            if (supportUrl.isBlank())
                throw new IllegalArgumentException("Modpack support URL cannot be blank");
        }

        public String toFriendlyString() {
            return """
                   
                   \t\tName: %s
                   \t\tVersion: %s
                   \t\tSupport URL: %s
                   """.stripTrailing().formatted(name, version, supportUrl);
        }
    }

    public static void load() {
        Java.Version.load();
        Java.Memory.load();
        Java.Advanced.load();
    }

    private CommonConfig() {}
}
