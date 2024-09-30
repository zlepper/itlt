package ga.ozli.minecraftmods.itlt.platform;

import ga.ozli.minecraftmods.itlt.platform.services.IPlatformHelper;
import ga.ozli.minecraftmods.itlt.shared.Constants;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.ModLoadingStage;
import net.minecraftforge.fml.ModLoadingWarning;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.IModInfo;

import java.nio.file.Path;

public final class ForgePlatformHelper implements IPlatformHelper {
    private static final ModLoader MOD_LOADER = ModLoader.get();
    private static final ModList MOD_LIST = ModList.get();
    private static final IModInfo ITLT_MOD_INFO = MOD_LIST.getModContainerById(Constants.MOD_ID).orElseThrow().getModInfo();

    @Override
    public void addWarning(String message) {
        MOD_LOADER.addWarning(new ModLoadingWarning(ITLT_MOD_INFO, ModLoadingStage.COMMON_SETUP, message));
    }

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public PhysicalSide getPhysicalSide() {
        return FMLEnvironment.dist.isClient() ? PhysicalSide.CLIENT : PhysicalSide.SERVER;
    }
}
