package ga.ozli.minecraftmods.itlt.shared;

import ga.ozli.minecraftmods.itlt.platform.Services;

public final class CommonClass {
    public static void checkJavaVersion() {
        byte currentJavaVer = (byte) Runtime.version().feature();
        Constants.LOG.debug("currentJavaVer: {}", currentJavaVer);

        if (currentJavaVer < CommonConfig.Java.Version.MIN) {
            Services.PLATFORM.addWarning("Outdated Java");
        } else if (CommonConfig.Java.Version.MAX != 0 && currentJavaVer > CommonConfig.Java.Version.MAX) {
            Services.PLATFORM.addWarning("Java too new");
        }
    }

    public static void checkJavaMemory() {
        float currentJavaMaxMemGiB = Utils.Memory.getJvmMax();
        Constants.LOG.debug("currentJavaMaxMemGiB: {}", currentJavaMaxMemGiB);

        if (currentJavaMaxMemGiB < CommonConfig.Java.Memory.MIN) {
            Services.PLATFORM.addWarning("Not enough memory");
        } else if (CommonConfig.Java.Memory.MAX != 0 && currentJavaMaxMemGiB > CommonConfig.Java.Memory.MAX) {
            Services.PLATFORM.addWarning("Too much memory");
        } else if (CommonConfig.Java.Memory.NEAR_MAX != 0 && currentJavaMaxMemGiB > (Utils.Memory.getOsMax() - CommonConfig.Java.Memory.NEAR_MAX)) {
            Services.PLATFORM.addWarning("Too much memory allocated for this machine");
        }
    }

    private CommonClass() {}
}
