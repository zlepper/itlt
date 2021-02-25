package dk.zlepper.itlt.launch;

import cpw.mods.fml.relauncher.ILibrarySet;

public class itltGsonLibrarySet implements ILibrarySet {
	
	private static String[] libraries = { "gson-2.2.4.jar" };
	private static String[] checksums = { "a60a5e993c98c864010053cb901b7eab25306568" };

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
		//return "https://repo1.maven.org/maven2/org/apache/commons/commons-lang3/3.1/commons-lang3-3.1.jar
		return "http://insecure.repo1.maven.org/maven2/com/google/code/gson/gson/2.2.4/%s";
	}
}