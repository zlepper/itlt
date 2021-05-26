package dk.zlepper.itlt.client.launchers;

import javax.annotation.Nullable;
import java.io.File;

public class ForgeDevEnv implements DetectedLauncher {

    @Nullable
    @Override
    public String getModpackDisplayName() {
        return null;
    }

    @Nullable
    @Override
    public File getModpackIcon() {
        return null;
    }
}
