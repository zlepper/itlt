package dk.zlepper.itlt.client.launchers;

import javax.annotation.Nullable;
import java.io.File;

// todo: determine if there's any benefit to supporting this launcher
public class FTBApp implements DetectedLauncher {

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
