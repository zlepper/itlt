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

public class CurseClient implements DetectedLauncher {

    // if running from the Curse Client launcher, use the profile's name
    @Nullable
    @Override
    public String getModpackDisplayName() throws IOException {
        final Path itltJarPath = LauncherUtils.getItltJarPath();

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
    @Override
    public File getModpackIcon() {
        return null;
    }

    @Override
    public boolean supportsChangingJavaVersion() {
        return false;
    }
}
