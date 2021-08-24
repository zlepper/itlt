package dk.zlepper.itlt.client.launchers;

import dk.zlepper.itlt.itlt;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.targets.FMLClientUserdevLaunchHandler;
import net.minecraftforge.fml.loading.targets.FMLServerUserdevLaunchHandler;

import java.nio.file.Files;
import java.nio.file.Path;

public class LauncherUtils {

    static Path getItltJarPath() {
        final Path itltJarPath = ModList.get().getModFileById("itlt").getFile().getFilePath();
        itlt.LOGGER.debug("itltJarPath: " + itltJarPath); // should be something like ???\mcRoot\mods\itlt.jar
        return itltJarPath;
    }

    public static DetectedLauncher getDetectedLauncher() {
        final Path itltJarPath = getItltJarPath();

        // jumping up a few directories should theoretically take us out of the mods folder and into the root folder of
        // the Twitch launcher. If the file "installedPacks" exists in this folder and there's also a folder called
        // "modpacks" in here, we know we're running within a Technic Launcher modpack
        final Path theoreticalTechnicPath = itltJarPath.getParent().getParent().getParent().getParent();
        final boolean isTechnicLauncher =
                Files.exists(theoreticalTechnicPath.resolve("installedPacks")) &&
                        Files.exists(theoreticalTechnicPath.resolve("modpacks"));
        itlt.LOGGER.debug("theoreticalTechnicPath: " + theoreticalTechnicPath);
        itlt.LOGGER.debug("isTechnicLauncher: " + isTechnicLauncher);
        if (isTechnicLauncher) return new Technic();

        // if the .minecraft folder has mmc-pack.json and instance.cfg files beside it then we know we're running
        // within a MultiMC modpack.
        final Path theoreticalMultiMCPath = itltJarPath.getParent().getParent().getParent();
        final boolean isMultiMCLauncher =
                Files.exists(theoreticalMultiMCPath.resolve("mmc-pack.json")) &&
                        Files.exists(theoreticalMultiMCPath.resolve("instance.cfg"));
        itlt.LOGGER.debug("theoreticalMultiMCPath: " + theoreticalMultiMCPath);
        itlt.LOGGER.debug("isMultiMCLauncher: " + isMultiMCLauncher);
        if (isMultiMCLauncher) return new MultiMC();

        // if the .minecraft folder has .curseclient and minecraftinstance.json files inside it then we know we're
        // running within the CurseClient
        final Path theoreticalCurseClientPath = itltJarPath.getParent().getParent();
        final boolean isCurseClientLauncher =
                Files.exists(theoreticalCurseClientPath.resolve(".curseclient")) &&
                        Files.exists(theoreticalCurseClientPath.resolve("minecraftinstance.json"));
        itlt.LOGGER.debug("theoreticalCurseClientPath: " + theoreticalCurseClientPath);
        itlt.LOGGER.debug("isCurseClientLauncher: " + isCurseClientLauncher);
        if (isCurseClientLauncher) return new CurseClient();

        final Path theoreticalFTBAppPath = itltJarPath.getParent().getParent().getParent().getParent();
        final boolean isFTBAppLauncher =
                Files.exists(theoreticalFTBAppPath.resolve("ftbapp.log"));
        itlt.LOGGER.debug("theoreticalFTBAppPath: " + theoreticalFTBAppPath);
        itlt.LOGGER.debug("isFTBAppLauncher: " + isFTBAppLauncher);
        if (isFTBAppLauncher) return new FTBApp();

        // todo: test this
        /*final Path theoreticalGDLauncherPath = itltJarPath.getParent().getParent().getParent().getParent();
        final boolean isGDLauncher =
                Files.exists(theoreticalGDLauncherPath.resolve("datastore").resolve("forgeinstallers")) &&
                        Files.exists(itltJarPath.getParent().resolve("config.json"));
        itlt.LOGGER.info("theoreticalGDLauncherPath.resolve(\"datastore\").resolve(\"forgeinstallers\"): " + theoreticalGDLauncherPath.resolve("datastore").resolve("forgeInstallers"));
        itlt.LOGGER.info("itltJarPath.getParent().resolve(\"config.json\"): " + itltJarPath.getParent().getParent().resolve("config.json"));
        itlt.LOGGER.debug("theoreticalGDLauncherPath: " + theoreticalGDLauncherPath);
        itlt.LOGGER.debug("isGDLauncher: " + isGDLauncher);
        if (isGDLauncher) return LauncherName.GDLauncher;

        // todo: support SKLauncher
        */

        final boolean isForgeDevEnv = isForgeDevEnv();
        itlt.LOGGER.debug("isForgeDevEnv: " + isForgeDevEnv);
        if (isForgeDevEnv) return new ForgeDevEnv();

        return new Unknown();
    }

    public static boolean isForgeDevEnv() {
        try {
            Class.forName("net.minecraftforge.fml.loading.targets.FMLClientUserdevLaunchHandler");
            if (new FMLClientUserdevLaunchHandler().name().equals("fmlclientuserdev")) return true;
            if (new FMLServerUserdevLaunchHandler().name().equals("fmlserveruserdev")) return true;
        } catch (final ClassNotFoundException | NoClassDefFoundError e) {
            return false;
        }
        return false;
    }
}
