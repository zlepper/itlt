package dk.zlepper.itlt.client.helpers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dk.zlepper.itlt.itlt;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.userdev.FMLDevClientLaunchProvider;
import net.minecraftforge.userdev.FMLDevServerLaunchProvider;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

public class LauncherUtils {

    private static Path getItltJarPath() {
        final Path itltJarPath = ModList.get().getModFileById("itlt").getFile().getFilePath();
        itlt.LOGGER.debug("itltJarPath: " + itltJarPath); // should be something like ???\mcRoot\mods\itlt.jar
        return itltJarPath;
    }
    public static LauncherName detectLauncher() {
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
        if (isTechnicLauncher) return LauncherName.Technic;

        // if the .minecraft folder has mmc-pack.json and instance.cfg files beside it then we know we're running
        // within a MultiMC modpack.
        final Path theoreticalMultiMCPath = itltJarPath.getParent().getParent().getParent();
        final boolean isMultiMCLauncher =
                Files.exists(theoreticalMultiMCPath.resolve("mmc-pack.json")) &&
                        Files.exists(theoreticalMultiMCPath.resolve("instance.cfg"));
        itlt.LOGGER.debug("theoreticalMultiMCPath: " + theoreticalMultiMCPath);
        itlt.LOGGER.debug("isMultiMCLauncher: " + isMultiMCLauncher);
        if (isMultiMCLauncher) return LauncherName.MultiMC;

        // if the .minecraft folder has .curseclient and minecraftinstance.json files inside it then we know we're
        // running within the CurseClient
        final Path theoreticalCurseClientPath = itltJarPath.getParent().getParent();
        final boolean isCurseClientLauncher =
                Files.exists(theoreticalCurseClientPath.resolve(".curseclient")) &&
                        Files.exists(theoreticalCurseClientPath.resolve("minecraftinstance.json"));
        itlt.LOGGER.debug("theoreticalCurseClientPath: " + theoreticalCurseClientPath);
        itlt.LOGGER.debug("isCurseClientLauncher: " + isCurseClientLauncher);
        if (isCurseClientLauncher) return LauncherName.CurseClient;

        final Path theoreticalFTBAppPath = itltJarPath.getParent().getParent().getParent().getParent();
        final boolean isFTBAppLauncher =
                Files.exists(theoreticalFTBAppPath.resolve("ftbapp.log"));
        itlt.LOGGER.debug("theoreticalFTBAppPath: " + theoreticalFTBAppPath);
        itlt.LOGGER.debug("isFTBAppLauncher: " + isFTBAppLauncher);
        if (isFTBAppLauncher) return LauncherName.FTBApp; // todo: determine if there's any benefit to supporting this launcher

        final boolean isForgeDevEnv = isForgeDevEnv();
        itlt.LOGGER.debug("isForgeDevEnv: " + isForgeDevEnv);
        if (isForgeDevEnv) return LauncherName.ForgeDevEnv;

        return LauncherName.Unknown;
    }

    public static boolean isForgeDevEnv() {
        try {
            if (new FMLDevClientLaunchProvider().name().equals("fmldevclient")) return true;
            if (new FMLDevServerLaunchProvider().name().equals("fmldevserver")) return true;
        } catch (Exception ignored) {}
        return false;
    }

    public enum LauncherName {
        Unknown,
        Technic,
        MultiMC,
        CurseClient,
        FTBApp,
        ForgeDevEnv
    }

    public static String getTechnicPackName() throws IOException {
        final Path itltJarPath = getItltJarPath();

        // get the pack slug
        final String packSlug = itltJarPath.getParent().getParent().getFileName().toString();

        // open the cache.json for the associated slug to get the pack's displayName
        final Path cacheJsonPath = itltJarPath.resolve("../../../../assets/packs" + packSlug + "/cache.json");
        final Reader reader = Files.newBufferedReader(cacheJsonPath);

        // convert the cacheJson String to a Map
        final Type type = new TypeToken<Map<String, Object>>(){}.getType();
        final Map<String, Object> definitionsMap = new Gson().fromJson(reader, type);

        final String packDisplayName = definitionsMap.get("displayName").toString();

        reader.close();

        itlt.LOGGER.debug("packDisplayName: " + packDisplayName);
        return packDisplayName;
    }

    public static String getMultiMCInstanceName() throws IOException {
        final Path itltJarPath = getItltJarPath();

        final String instanceCfg = itltJarPath.resolve("../../../instance.cfg").toString();

        // attempt to load the instance.cfg file and parse it
        final Properties parsedInstanceCfg = new Properties();
        parsedInstanceCfg.load(new FileInputStream(instanceCfg));

        final String instanceName = parsedInstanceCfg.getProperty("name");
        itlt.LOGGER.debug("instanceName: " + instanceName);

        return instanceName;
    }

    public static String getCurseClientProfileName() throws IOException {
        final Path itltJarPath = getItltJarPath();

        // open the minecraftinstance.json file
        final Reader reader = Files.newBufferedReader(itltJarPath.resolve("../../minecraftinstance.json"));

        // parse the json file to a Map with keys of type String
        final Type type = new TypeToken<Map<String, Object>>(){}.getType();
        final Map<String, Object> map = new Gson().fromJson(reader, type);

        // get the "name" key from the Map
        final String profileName = map.get("name").toString();

        // close the json file
        reader.close();

        itlt.LOGGER.debug("profileName: " + profileName);
        return profileName;
    }

    @Nullable
    public static File getTechnicPackIcon() {
        final Path itltJarPath = getItltJarPath();

        // get the pack slug
        final String packSlug = itltJarPath.getParent().getParent().getFileName().toString();

        // get the icon from the associated pack's slug
        final Path iconPath = itltJarPath.resolve("../../../../assets/packs/" + packSlug + "/icon.png");

        if (iconPath.toFile().exists()) return iconPath.toFile();
        else return null;
    }

    @Nullable
    public static File getMultiMCInstanceIcon() {
        final Path itltJarPath = getItltJarPath();

        final Path iconPath = itltJarPath.getParent().getParent().resolve("icon.png");

        if (iconPath.toFile().exists()) return iconPath.toFile();
        else return null;
    }
}
