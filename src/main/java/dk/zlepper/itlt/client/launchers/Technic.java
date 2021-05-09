package dk.zlepper.itlt.client.launchers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dk.zlepper.itlt.itlt;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class Technic implements DetectedLauncher {

    // if running from the Technic Launcher, use the pack slug's displayName
    @Nullable
    @Override
    public String getModpackDisplayName() throws IOException {
        final Path itltJarPath = LauncherUtils.getItltJarPath();

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

    @Nullable
    @Override
    public File getModpackIcon() {
        final Path itltJarPath = LauncherUtils.getItltJarPath();

        // get the pack slug
        final String packSlug = itltJarPath.getParent().getParent().getFileName().toString();

        // get the icon from the associated pack's slug
        final Path iconPath = itltJarPath.resolve("../../../../assets/packs/" + packSlug + "/icon.png");

        if (iconPath.toFile().exists()) return iconPath.toFile();
        else return null;
    }
}
