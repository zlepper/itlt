package dk.zlepper.itlt.launch;

import cpw.mods.fml.relauncher.ILibrarySet;

public class itltCommonsLangLibrarySet implements ILibrarySet {
	
	private static String[] libraries = { "commons-lang3-3.1.jar" };
	private static String[] checksums = { "905075e6c80f206bbe6cf1e809d2caa69f420c76" };

	@Override
	public String[] getLibraries() {
		return libraries;
	}

	@Override
	public String[] getHashes() {
		return checksums;
	}

	@Override
	public String getRootURL() {
		return "http://insecure.repo1.maven.org/maven2/org/apache/commons/commons-lang3/3.1/%s";
	}
}