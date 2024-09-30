package ga.ozli.minecraftmods.itlt.shared.client;

import ga.ozli.minecraftmods.itlt.shared.CommonConfig;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Collections;

public final class ClientConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder() {
        @Override
        public ForgeConfigSpec.Builder push(String groupName) {
            return super.push(Collections.singletonList(groupName));
        }
    };

    private static final ForgeConfigSpec.BooleanValue WRONG_GPU;

    private static final ForgeConfigSpec.ConfigValue<String> WINDOW_TITLE_FORMAT;

    public static final ForgeConfigSpec SPEC;

    static {
        BUILDER.push("Graphics"); {
            WRONG_GPU = BUILDER
                    .comment(
                            "",
                            " Sometimes players accidentally run the game on integrated graphics instead of their dedicated GPU, which severely hurts performance.",
                            " Turn this on to warn the player when they are using integrated graphics but have a dedicated GPU installed.",
                            " Don't worry - they won't be warned if they only have integrated graphics, because there is no dedicated GPU to switch to."
                    )
                    .define("wrongGPU", true);
        } BUILDER.pop();

        BUILDER.push("Display"); {
            WINDOW_TITLE_FORMAT = BUILDER
                    .define("windowTitleFormat", "%mc - %modpackName %modpackVersion");
        } BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static final class Graphics {
        public static final boolean WRONG_GPU = ClientConfig.WRONG_GPU.get();

        private static void load() {}

        private Graphics() {}
    }

    public static final class Display {
        public static final String WINDOW_TITLE;

        static {
            var windowTitleFormat = ClientConfig.WINDOW_TITLE_FORMAT.get();
            if (!windowTitleFormat.contains("%mc"))
                windowTitleFormat = "%mc - " + windowTitleFormat;

            WINDOW_TITLE = windowTitleFormat
                    .replaceFirst("%modpackName", CommonConfig.ModpackInfo.INSTANCE.name())
                    .replaceFirst("%modpackVersion", CommonConfig.ModpackInfo.INSTANCE.version())
                    .trim();
        }

        private static void load() {}

        private Display() {}
    }

    public static void load() {
        Graphics.load();
        Display.load();
    }
}
