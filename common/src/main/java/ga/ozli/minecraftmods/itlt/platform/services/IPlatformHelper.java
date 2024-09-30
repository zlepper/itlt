package ga.ozli.minecraftmods.itlt.platform.services;

import java.nio.file.Path;

public interface IPlatformHelper {
    void addWarning(String message);

    Path getConfigDir();

    enum PhysicalSide {
        CLIENT,
        SERVER;

        public boolean isClient() {
            return this == CLIENT;
        }
    }

    PhysicalSide getPhysicalSide();
}
