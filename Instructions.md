1. Clone the repo
2. Make sure you have Java 7 and that JAVA_HOME points to it. Uninstall newer versions of Java or the scripts will keep using them regardless of JAVA_HOME and PATH and fail as a result.
3. Open the "Configure Java" app, go to the "Advanced" tab and enable TLSv1.1 and v1.2 under the "Advanced Security Settings" section.
4. Extract mcp, copy over mcp.cfg from this repo to the conf folder, overwriting the existing one
5. Get a copy of the Forge 1.2.5 source zip from https://files.minecraftforge.net
6. Extract it to the root of where you extracted mcp. Once extracted there should be a folder called "forge" alongside the other mcp folders
7. Run install.cmd or install.sh in the Forge folder
8. Move the contents of the modsrc folder to the src folder
9. Install newer Java again so that you can use Eclipse. Make sure the JAVA_HOME and PATH still point to Java 7.
10. Import into eclipse, latest version of eclipse surprisingly works fine so no need to worry about getting Eclipse Juno or whatever
11. The mod's source code is under the dk.zlepper.itlt package in eclipse

## Testing and building
1. When you want to test, startclient.bat/startclient.sh should work, if not you'll have to manually build for production and test externally with a Minecraft launcher
2. To build, save your changes in eclipse then run recompile.bat/.sh. Once that's done, run reobfuscate.bat/.sh.
3. Now manually create the jar either through the JDK's utility or through WinRAR.
4. With the WinRAR method, you open the existing jar provided in the modSrc or src folders and just drag over the new class stuff inside the minecraft folder inside the reobf folder into the root of the jar. Don't forget the mcmod.info file also!
5. Use the getchangedsrc.bat/.sh to update the modSrc folder for contributing to this repo

## Notes
- You will probably need to refer to the instructions for MC 1.4 and 1.5 if you get stuck as the steps are similar and I may have forgotten to mention something.
- .minecraft/versions/1.2.5/1.2.5.jar -> itlt/jars/bin/minecraft.jar
- https://assets.minecraft.net/1_2_5/minecraft_server.jar -> itlt/jars/minecraft_server.jar
- itlt/jars/bin needs jinput, lwjgl, etc...
- itlt/jars/bin/natives needs to be present and populated with the right stuff too
- itlt/runtime/bin/fernflower.jar may need to be manually copied over from a 1.3.2 mod dev instance
- You'll probably need to manually change the run configurations in Eclipse to use Java 7 if it doesn't already work