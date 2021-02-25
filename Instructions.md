1. Clone the repo
2. Make sure you have Java 7 and that JAVA_HOME points to it
3. Open the "Configure Java" app, go to the "Advanced" tab and enable TLSv1.1 and v1.2 under the "Advanced Security Settings" section.
4. Extract mcp
5. Get a copy of the Forge 1.5.2 source zip from https://files.minecraftforge.net
6. Extract it to the root of where you extracted mcp. Once extracted there should be a folder called "forge" alongside the other mcp folders
7. Start the Minecraft Launcher and launch Minecraft 1.5.2 at least once, copy the 1.5.2.jar from .minecraft/versions/1.5.2/, paste it into the jars folder of where you extracted mcp.
8. Rename it from 1.5.2.jar to minecraft.jar. Grab the minecraft_server.jar for 1.5.2 and put it in here also
9. Run install.cmd or install.sh in the Forge folder
10. Use the provided fml_libs_dev15.zip to manually resolve any failed downloads when installing
11. Use the provided fml_libs15.zip to manually resolve any failed downloads when doing startclient.cmd/startclient.sh for the first time
12. Move the contents of the modsrc folder to the src folder
13. Import into eclipse, latest version of eclipse surprisingly works fine so no need to worry about getting Eclipse Juno or whatever
14. The mod's source code is under the dk.zlepper.itlt package in eclipse

## Testing and building
1. When you want to test, startclient.bat/startclient.sh should work, if not you'll have to manually build for production and test externally with a Minecraft launcher
2. To build, save your changes in eclipse then run recompile.bat/.sh. Once that's done, run reobfuscate.bat/.sh.
3. Now manually create the jar either through the JDK's utility or through WinRAR.
4. With the WinRAR method, you open the existing jar provided in the modSrc or src folders and just drag over the new class stuff inside the minecraft folder inside the reobf folder into the root of the jar. Don't forget the mcmod.info file also!
5. If you want to use Gson and Apache Commons Lang libs, make a new folder in the jar called "META-INF" and copy over the provided MANIFEST.MF into that folder so that FML can grab the required libs at runtime and it should all just work in production. The mod then needs to be put in the coremods folder instead of the mods folder.