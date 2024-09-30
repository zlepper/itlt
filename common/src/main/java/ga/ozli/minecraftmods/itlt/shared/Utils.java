package ga.ozli.minecraftmods.itlt.shared;

import oshi.SystemInfo;

import java.lang.management.ManagementFactory;
import java.util.Locale;

public final class Utils {
    public static final SystemInfo SYSTEM_INFO = new SystemInfo();

    public static class Memory {
        /**
         * Converts bytes to gigabytes, rounding to the nearest tenth (e.g.: 1.0, 1.1, 1.2...)
         */
        private static float bytesToGigabytes(long bytes) {
            var megabytes = bytes >> 20;
            return Float.parseFloat(String.format(Locale.ROOT, "%.1f", megabytes / 1024.0f));
        }

        /**
         * Gets the max amount of RAM allocated to the JVM in gigabytes, rounded to the nearest tenth
         */
        public static float getJvmMax() {
            var memBean = ManagementFactory.getMemoryMXBean();
            return bytesToGigabytes(memBean.getHeapMemoryUsage().getMax() + memBean.getNonHeapMemoryUsage().getMax());
        }

        /**
         * Gets the amount of physical RAM this system has, rounded to the nearest tenth
         */
        public static float getOsMax() {
            return bytesToGigabytes(SYSTEM_INFO.getHardware().getMemory().getTotal());
        }

        private Memory() {}
    }

    private Utils() {}
}
