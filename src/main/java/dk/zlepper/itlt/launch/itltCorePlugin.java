package dk.zlepper.itlt.launch;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

public class itltCorePlugin implements IFMLLoadingPlugin {
	
	// This FML Loading Plugin allows Gson and Apache Commons Lang libraries to be used in this mod.
	// FML will grab these libraries before starting the game and it should all just work nicely in production.
	
	// Add the line below to the mod's built jar's MANIFEST.MF to enable this. Once enabled, the mod needs to go in coremods folder for this to have an effect.
	// FMLCorePlugin: dk.zlepper.itlt.launch.itltCorePlugin

	@Override
	public String[] getLibraryRequestClass() {
		return new String[] { "dk.zlepper.itlt.launch.itltGsonLibrarySet", "dk.zlepper.itlt.launch.itltCommonsLangLibrarySet" };
	}

	
	@Override
	public String[] getASMTransformerClass() {
		return null;
	}


	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		
	}

}
