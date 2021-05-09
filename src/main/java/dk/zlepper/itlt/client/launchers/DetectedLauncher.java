package dk.zlepper.itlt.client.launchers;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

public interface DetectedLauncher {

    @Nullable
    String getModpackDisplayName() throws IOException;

    @Nullable
    File getModpackIcon();

    // todo: consider nullable boolean suppliers for better handling of unknown launchers
    default boolean supportsChangingJavaVersion() { return true; }
    default boolean supportsChangingJVMArgs() { return true; }
}
