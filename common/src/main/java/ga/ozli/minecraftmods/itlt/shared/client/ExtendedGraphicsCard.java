package ga.ozli.minecraftmods.itlt.shared.client;

import com.mojang.blaze3d.systems.RenderSystem;
import ga.ozli.minecraftmods.itlt.shared.Utils;
import org.jetbrains.annotations.Nullable;
import oshi.hardware.GraphicsCard;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static ga.ozli.minecraftmods.itlt.shared.client.ExtendedGraphicsCard.GraphicsBrand.*;

record ExtendedGraphicsCard(GraphicsCard delegate) implements GraphicsCard {
    public enum GraphicsBrand {
        AMD,
        NVIDIA,
        INTEL,
        UNKNOWN;

        private static final String[] AMD_API_STRINGS = { "AMD ", "Radeon", "ATI Technologies", "0x1002" };

        public static GraphicsBrand getCurrent() {
            var apiDescription = RenderSystem.getApiDescription();

            if (apiDescription.contains("Intel "))
                return INTEL;

            if (isAMD(apiDescription))
                return AMD;

            if (apiDescription.toLowerCase(Locale.ROOT).contains("nvidia"))
                return NVIDIA;

            return UNKNOWN;
        }

        private static boolean isAMD(String apiDescription) {
            for (String apiStr : AMD_API_STRINGS) {
                if (apiDescription.contains(apiStr)) {
                    return true;
                }
            }

            return false;
        }
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public String getDeviceId() {
        return delegate.getDeviceId();
    }

    @Override
    public String getVendor() {
        return delegate.getVendor();
    }

    @Override
    public String getVersionInfo() {
        return delegate.getVersionInfo();
    }

    @Override
    public long getVRam() {
        return delegate.getVRam();
    }

    public GraphicsBrand getBrand() {
        if (getVendor().contains("Intel") || getVendor().contains("8086"))
            return INTEL;

        if (getName().contains("AMD") || getVendor().contains("0x1002"))
            return AMD;

        if (getVendor().toLowerCase(Locale.ROOT).contains("nvidia"))
            return NVIDIA;

        return UNKNOWN;
    }

    public boolean isIntegrated() {
        return switch (getBrand()) {
            case INTEL -> KnownGPUs.Intel.isIntegrated(getDeviceId());
            case AMD -> KnownGPUs.AMD.isIntegrated(getDeviceId());
            default -> false; // if NVIDIA or unknown, assume it's not integrated
        };
    }

    @Nullable
    public static ExtendedGraphicsCard getCurrent(Map<GraphicsBrand, List<ExtendedGraphicsCard>> candidates) {
        return getCurrent(candidates, GraphicsBrand.getCurrent());
    }

    @Nullable
    public static ExtendedGraphicsCard getCurrent(Map<GraphicsBrand, List<ExtendedGraphicsCard>> candidates, GraphicsBrand currentBrand) {
        var filteredCandidates = candidates.get(currentBrand);
        if (filteredCandidates == null || filteredCandidates.isEmpty())
            return null; // the current brand isn't in the candidates somehow!?

        if (filteredCandidates.size() > 1)
            return null; // can't tell which of the filtered candidates are running as they're all the same brand

        return filteredCandidates.getFirst();
    }

    public static Map<GraphicsBrand, List<ExtendedGraphicsCard>> getAll() {
        return Utils.SYSTEM_INFO.getHardware().getGraphicsCards().stream()
                .map(ExtendedGraphicsCard::new)
                .collect(Collectors.groupingBy(ExtendedGraphicsCard::getBrand));
    }

    private static final class KnownGPUs {
        private static final class Intel {
            /**
             * @link <a href="https://en.wikipedia.org/wiki/List_of_Intel_graphics_processing_units">Wikipedia list of Intel graphics processing units</a>
             * @link <a href="https://dgpu-docs.intel.com/devices/hardware-table.html">Intel graphics hardware devices table</a>
             * @link <a href="https://admin.pci-ids.ucw.cz/read/PC/8086">PCI ID repository</a>
             */
            private static final Set<String> KNOWN_INTEL_IGPUS = Set.of(
                    "A780", // Desktop Raptor Lake UHD Graphics 770

                    // Mobile Alder Lake
                    "46A6", "46A8", "46AA", // Intel Xe Graphics
                    "46A3", "46B3", "46C3", // UHD Graphics

                    // Desktop Alder Lake
                    "4680", "4690", // UHD Graphics 770
                    "4692", // UHD Graphics 730
                    "4693", // UHD Graphics 710

                    // Mobile Tiger Lake
                    "9A49", "9A40", // Intel Xe Graphics
                    "9A78", // UHD Graphics

                    // Desktop Rocket Lake
                    "4C8A", // UHD Graphics 750
                    "4C8B", // UHD Graphics 730

                    // Mobile Ice Lake
                    "8A53", "8A52", "8A51", "8A5A", "8A5C", // Iris Plus Graphics
                    "8A56", "8A58", // UHD Graphics

                    // Mobile Coffee Lake
                    "3EA5", // Iris Plus Graphics 655
                    "3EA6", // Iris Plus Graphics 645
                    "3E9B", // UHD Graphics 630

                    // Desktop Coffee Lake
                    "3E92", "3E91", // UHD Graphics 630
                    "3E93", // UHD Graphics 610

                    // Mobile Kaby Lake
                    "5927", // Iris Plus Graphics 650
                    "5926", // Iris Plus Graphics 640
                    "591B", // HD Graphics 630
                    "5917", // UHD Graphics 620
                    "5916", // HD Graphics 620
                    "591E", // HD Graphics 615
                    "5906", // HD Graphics 610

                    // Desktop Kaby Lake
                    "5912", // HD Graphics 630
                    "5902", // HD Graphics 610

                    // Mobile and Desktop, Gemini Lake and Gemini Lake Refresh
                    "3184", // UHD Graphics 605
                    "3185", // UHD Graphics 600

                    // Mobile and Desktop Skylake
                    "193B",  // Iris Pro Graphics 580
                    "1927", // Iris Graphics 550
                    "1926", // Iris Graphics 540
                    "1912", "191B", // HD Graphics 530
                    "1916", // HD Graphics 520
                    "191E", // HD Graphics 515
                    "1902", "1906", // HD Graphics 510

                    // Mobile and Desktop Apollo Lake
                    "5A84", // HD Graphics 505
                    "5A85", // HD Graphics 500

                    // Other UHD Graphics
                    "A78B", "A78A", "A789", "A788", "A783", "A782", "A781", // Raptor Lake-S
                    "4682", "4688", "468A", "468B", // Alder Lake-S
                    "46D0", "46D1", "46D2", // Alder Lake-N
                    "4626", "4628", "462A", "46A0", "46A1", "46A2", "46B0", "46B1", "46B2", "46C0", "46C1", "46C2", // Alder Lake
                    "4C90", "4C9A", "4C8C", "4C80", // Rocket Lake
                    "4E71", "4E61", "4E57", "4E55", "4E51", // Jasper Lake
                    "9A59", "9A60", "9A68", "9A70", "9AC0", "9AC9", "9AD9", "9AF8" // Tiger Lake
            );

            private static boolean isIntegrated(String deviceId) {
                return KNOWN_INTEL_IGPUS.contains(deviceId);
            }

            private Intel() {}
        }

        private static final class AMD {
            /**
             * In no particular order.
             * @link <a href="https://admin.pci-ids.ucw.cz/read/PC/1002">PCI ID repository</a>
             * @link <a href="https://devicehunt.com/search/type/pci/vendor/1002/device/any">DeviceHunt listing</a>
             */
            private static final Set<String> KNOWN_AMD_IGPUS = Set.of(
                    "1638", // Cezanne
                    "15d8", // Picasso
                    "15dd", // Raven Ridge
                    "1636", // Renoir
                    "164c", // Lucienne
                    "164d", "1681", // Rembrandt
                    "164e", // Raphael
                    "15bf", "164f", // Phoenix
                    "15c8", // Phoenix 2
                    "15e7", // Barcelo
                    "1506", // Mendocino
                    "150e" // Strix
            );

            private static boolean isIntegrated(String deviceID) {
                return KNOWN_AMD_IGPUS.contains(deviceID);
            }

            private AMD() {}
        }

        private KnownGPUs() {}
    }
}
