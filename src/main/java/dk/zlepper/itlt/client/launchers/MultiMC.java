package dk.zlepper.itlt.client.launchers;

import dk.zlepper.itlt.itlt;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public class MultiMC implements DetectedLauncher {

    // if running from the MultiMC launcher, use the instance's user-friendly name
    @Nullable
    @Override
    public String getModpackDisplayName() throws IOException {
        final Path itltJarPath = LauncherUtils.getItltJarPath();

        final String instanceCfg = itltJarPath.resolve("../../../instance.cfg").toString();

        // attempt to load the instance.cfg file and parse it
        final Properties parsedInstanceCfg = new Properties();
        parsedInstanceCfg.load(new FileInputStream(instanceCfg));

        final String instanceName = parsedInstanceCfg.getProperty("name");
        itlt.LOGGER.debug("instanceName: " + instanceName);

        return instanceName;
    }

    @Nullable
    @Override
    public File getModpackIcon() {
        final Path itltJarPath = LauncherUtils.getItltJarPath();

        final Path iconPath = itltJarPath.getParent().getParent().resolve("icon.png");

        if (iconPath.toFile().exists()) return iconPath.toFile();
        else return null;
    }
}
