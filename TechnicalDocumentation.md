# Technical documentation

## Terminology

| Term        | Definition                                           |
| ----------- | ---------------------------------------------------- |
| itlt        | It's the little things (the name of this mod)        |
| FML         | Forge Mod Loader                                     |
| ForgeSPI    | Forge Service Provider Interfaces                    |
| Mixin(s)    | A coremodding utility by Mumfrey                     |
| Coremodding | The act of modifying core parts of the game directly |
| Req(s)      | Requirement(s)                                       |
| Warn(s)     | Warning(s)                                           |
| MC          | Minecraft                                            |

## Contents

-   [Introduction](https://github.com/zlepper/itlt/blob/1.16-2.0-rewrite/TechnicalDocumentation.md#introduction)
-   [Main classes](https://github.com/zlepper/itlt/blob/1.16-2.0-rewrite/TechnicalDocumentation.md#main-classes)
-   [Features]
    -   [Java environment warnings and requirements system](https://github.com/zlepper/itlt/blob/1.16-2.0-rewrite/TechnicalDocumentation.md#java-environment-warnings-and-requirements-system)
        -   [What?](https://github.com/zlepper/itlt/blob/1.16-2.0-rewrite/TechnicalDocumentation.md#what)
        -   [Where?](https://github.com/zlepper/itlt/blob/1.16-2.0-rewrite/TechnicalDocumentation.md#where)
        -   [Q&A](https://github.com/zlepper/itlt/blob/1.16-2.0-rewrite/TechnicalDocumentation.md#qa)
        -   [How do I...](https://github.com/zlepper/itlt/blob/1.16-2.0-rewrite/TechnicalDocumentation.md#how-do-i)
    -   [Branding customisation](https://github.com/zlepper/itlt/blob/1.16-2.0-rewrite/TechnicalDocumentation.md#branding-customisation)
        -   [What?](https://github.com/zlepper/itlt/blob/1.16-2.0-rewrite/TechnicalDocumentation.md#what-1)
        -   [Where?](https://github.com/zlepper/itlt/blob/1.16-2.0-rewrite/TechnicalDocumentation.md#where-1)
        -   [Q&A](https://github.com/zlepper/itlt/blob/1.16-2.0-rewrite/TechnicalDocumentation.md#qa-1)
        -   [How do I...](https://github.com/zlepper/itlt/blob/1.16-2.0-rewrite/TechnicalDocumentation.md#how-do-i-1)
-   [To-do lists](https://github.com/zlepper/itlt/blob/1.16-2.0-rewrite/TechnicalDocumentation.md#to-do-lists)
    -   [Things I still need to document here](https://github.com/zlepper/itlt/blob/1.16-2.0-rewrite/TechnicalDocumentation.md#things-i-still-need-to-document-here)
    -   [Things left to-do with the existing code](https://github.com/zlepper/itlt/blob/1.16-2.0-rewrite/TechnicalDocumentation.md#things-left-to-do-with-the-existing-code)
    -   [New features to consider adding later](https://github.com/zlepper/itlt/blob/1.16-2.0-rewrite/TechnicalDocumentation.md#new-features-to-consider-adding-later)

## Introduction

The "It's the little things" mod implements various miscellaneous features to the game, most of which are minor and targeted at modpacks. In other words, "It's the little things that add the finishing touches to your modpack".

This document is a technical overview and walkthrough for developers interested in contributing or simply for advanced users that are interested in how the mod works behind the scenes. I've wrote this because too often I find myself wanting to contribute something to an existing mod but get end up first needing to spend a while figuring out how it works due to differences in coding practices and program design.

**Don't worry, you're not expected to read all of this before contributing. It's just a "cheat sheet" that is intended for you to skim read the specific parts you care about if you want, to help you save time figuring out how the mod works.**

## Main classes

There are two main classes in this mod, one that is used when launching the mod inside a Forge environment (itlt.java), and one in a standard Java environment (Main.java).

This is because some of the features itlt uses involves making pop-up windows which cannot be done directly within a Minecraft game environment, the alternative is heavier coremodding which would be harder to maintain across Minecraft versions and more likely to break compatibility with other mods.

See the warnings feature brief for details on which to use and when. If you're not touching that feature, the itlt.java is probably the starting point you're looking for.

| Environment | Description                                                            | Main class |
| ----------- | ---------------------------------------------------------------------- | ---------- |
| Ingame      | Constructed when inside a Forge environment in the game                | itlt.java  |
| Plain Java  | Constructed when the jar's launched directly by `java -jar` or similar | Main.java  |

## Features

### Java environment warnings and requirements system

#### What?

-   Modpack authors can set requirements and warnings relating to the Java environment their pack is started in
    -   Requirements prevent the game from starting if not met and lets the user know why
    -   Warnings tell the player if something isn't ideal but still lets them play
        -   Warnings are not shown if the launcher the player's using doesn't allow them to change what the warning is asking for

#### Where?

-   Handled in `client.ClientModEvents#commonInit` (`FMLCommonSetupEvent`)
-   Has calls to:
    -   `ClientUtils#getJavaVersion()` to get normalised Java version (e.g. 1.8 -> 8, but 11 is still -> 11)
    -   `LauncherUtils#detectLauncher()` to get the launcher used to start the game
    -   `ClientConfig` to grab config values related to the requirements and warnings system
    -   `ClientUtils#startUIProcess()` to show pop-ups as needed
-   Uses enums from:
    -   `client.helpers.Message.Content` (e.g. NeedsNewerJava, WantsNewerJava, NeedsLessMemory, etc...)
    -   `LauncherUtils.LauncherName` (e.g. CurseClient, Technic, MultiMC, etc...)

#### Q&A:

-   Why not `FMLClientSetupEvent`?
    -   When a requirement isn't met the game is intentionally stopped. As such, using the FMLCommonSetupEvent which runs earlier helps avoid wasting people's time by letting them know if a requirement isn't met sooner than if I used the FMLClientSetupEvent here
-   What does `ClientUtils#startUIProcess()` do?
    -   It parses the `Message.Content` into a set of arguments and starts a new Java process which then executes the `Main.java` main class mentioned in the Main classes section
-   How does `ClientUtils#startUIProcess()` work?
    -   The `Message.Content` enum has almost all the details needed for the UI process, with the remaining filled in by context and some `ClientConfig` values
    1.  It generates translation keys based on the `Message.Content` and grabs some config values to fill in the gaps (non-translatable stuff such as guideURLs)
    2.  As resourcepacks aren't available during the `FMLCommonSetupEvent` stage, the language files for itlt are manually parsed and the translation keys are translated before being sent off to `Main.java` in another process
    3.  To keep things simple and to reduce the number of dependencies, `Main.java` always expects the same number and position of arguments being fed to it for the requirements and warnings system. Empty arguments have a "." inside.
    4.  A `ProcessBuilder` makes and starts the itlt mod jar itself which Java then resolves the `Main-Class` based on the MANIFEST.MF generated by the `jar { manifest { ... } }` section of the `build.gradle`
-   What does `Main.java` do?
    -   It uses the arguments fed to it to determine which pop-up window GUI to make with Swing
    -   If it detects it's on Windows, it tries some reflection to make it use the correct Windows iconography rather than an outdated one embedded inside the JDK in order to make the GUI look nicer and more native, with a graceful fallback
    -   Warning preferences (such as remembering "Don't warn about this again") are handled by `client.helpers.WarningPreferences` which `Main.java` `ClientUtils#startUIProcess()`
-   What does `WarningPreferences.java` do?
    -   It's a wrapper around `java.util.Properties` with support for booleans and creating the file if not already present
-   Why are the `isJava64bit` warning and requirements handled in `client.ClientModEvents#clientInit` (`FMLClientSetupEvent`) separate from all the rest?
    -   It calls `Minecraft#isJava64bit()` which isn't available until this stage in the mod loading process

#### How do I...

-   Add a new requirement?
    1. Add to the Subject and/or Desire enums in `client.helpers.Message`
    2. Add in the possible combinations in the Content enum. Use `Type.Needs`
    3. If your new requirement is based on a new environment type, add the detection/getter(s) for that in `ClientUtils`
    4. Add relevant config options to `ClientConfig`
    5. Handle it in `client.ClientModEvents#commonInit` or `client.ClientModEvents#clientInit` depending on how your environment detection is done
-   Add a new warning?
    -   Same as the requirement thing above but use `Type.Wants` instead of `Type.Needs` and consider any "selectivelyIgnore(yourWarningNameHere)Warning` config options if relevant
-   Change the GUI?
    -   Look at `Main.java` and adjust the Swing code as desired
    -   You can change args sent to the GUI through `ClientUtils#startUIProcess()`

### Branding customisation

#### What?

-   Modpack authors can add to the existing game's branding to help distinguish it from other modpacks on the same Minecraft version
    -   Window title can either be customised manually or set to auto-detect based on how it's named in the launcher the game was started from. Or no custom window title at all if prefered (this'll keep the Vanilla one).
    -   Same with the custom icon

#### Where?

-   Handled in `client.ClientModEvents#clientInit` (`FMLClientSetupEvent`)
-   Has calls to:
    -   `ClientConfig` to grab config values related to branding customisation
    -   `LauncherUtils#detectLauncher()` to get the launcher used to start the game
    -   `LauncherUtils#getTechnicPackName()` to get the Technic pack slug's displayName
    -   `LauncherUtils#getMultiMCInstanceName()` to get the MultiMC instance's user-friendly name
    -   `LauncherUtils#getCurseClientProfileName()` to get the Curse Client profile's name
    -   `LauncherUtils#getTechnicPackIcon()` to get the Technic pack slug's icon
    -   `LauncherUtils#getMultiMCInstanceIcon()` to get the MultiMC instance's icon
    -   `ClientUtils#setWindowIcon()` to set the custom window icon
-   Uses enums from:
    -   `LauncherUtils.LauncherName` (e.g. CurseClient, Technic, MultiMC, etc...)

#### Q&A:

-   How does `LauncherUtils#detectLauncher()` work?
    -   Each launcher lays out the directory structure differently and manages isolating separate Minecraft installs in their own way.
    -   itlt uses this to detect what launcher started the game. For example, the Technic Launcher stores modpacks in a dedicated "modpacks" folder, MultiMC puts `mmc-pack.json` and `instance.cfg` files inside the install's `.minecraft` folder, and so on...
    -   If there's a better way of doing this I'd love to know. Checking the existance of stuff on file system isn't ideal.
-   What does `LauncherUtils#getTechnicPackName()` do?
    -   It parses the Technic Launcher's `cache.json` file for our packSlug and grabs the value associated to the `displayName` key
-   What does `LauncherUtils#getMultiMCInstanceName()` do?
    -   It parses the MultiMC `instance.cfg` file in the root `.minecraft` folder we're running from and grabs the value associated to the `name` key
-   What does `LauncherUtils#getCurseClientProfileName()` do?
    -   It parses the `.minecraftinstance.json` and grabs the value associated to the `name` key

#### How do I...

-   Add detection for another/new launcher?
    1. Determine what's unique about how the launcher organises modpacks
    2. Add a new enum to `LauncherUtils.LauncherName`
    3. Add detection code to `LauncherUtils#detectLauncher()` and return your LauncherName enum added in step 2 if detected
-   Support custom window title auto-detection on an existing launcher?
    1. Determine how the launcher stores the friendly display name for the currently running modpack
    2. Implement it as a new method in `LauncherUtils` with the name `get(launcherName)(whateverTheyCallMCInstalls)Name()` that returns a `String`
        - For example, MultiMC calls different installs of the game "instances", so we name the method `getMultiMCInstanceName()`
        - For example, Technic Launcher calls different installs of the game "packs", so we name the method `getTechnicPackName()`
    3. Add a new case to the `switch (detectedLauncher)` statement inside the `ClientConfig.enableUsingAutodetectedDisplayName.get()` if block and call your method, assigning its result to the `customWindowTitle` variable
-   Support custom window title auto-detection on a new launcher?
    -   First add detection for the new launcher, then follow the instructions for doing it on an existing launcher
-   Support custom icon auto-detection on an existing launcher?
    1. Determine how the launcher stores the icon for the currently running modpack
    2. Implement it as a new method in `LauncherUtils` with the name `get(launcherName)(whateverTheyCallMCInstalls)Icon()` that returns a `File`
        - For example, MultiMC calls different installs of the game "instances", so we name the method `getMultiMCInstanceIcon()`
    3. Add a new case to the `switch (detectedLauncher)` statement inside the `ClientConfig.enableUsingAutodetectedIcon.get()` if block and call your method, assigning its result to the `autoDetectedIcon` variable
-   Support custom icon auto-detection on a new launcher?
    -   First add detection for the new launcher, then follow the instructions for doing it on an existing launcher

## To-do lists

### Things I still need to document here

-   Custom server list entries (found in `client.ClientModEvents#clientInit`)
-   Explicit GC (found in `client.ClientForgeEvents#onGuiOpen`)
-   Anti-cheat
    -   Server-side ModID blacklisting
    -   Client<->server negotiation
    -   Class checksumming
    -   Definition updates and caching
-   Mixins used
    -   FMLHandshakeHandlerMixin
    -   WindowTitleMixin
-   Icon resizing mechanism (found in `client.ClientUtils#setWindowIcon`)
-   Config declaration style (how config comments are laid out, the naming scheme for config keys, key structure such as `Java.Advanced.ExplicitGC`...)

### Things left to-do with the existing code

-   HIGH PRIORITY: Change format of enableAppendingToCustomTitle from "packName (Minecraft* version)" to "Minecraft* version - packName"
-   Investigate organising the LauncherUtils better
    -   `new DetectedLauncher().getFriendlyName()`?
    -   `LauncherUtils.MultiMC.getFriendlyName()` and have `LauncherUtils.MultiMC` implement `LauncherUtils.DetectedLauncher`?
-   Redo the anti-cheat stuff
    -   It works, but there's lots of signs of me trying to get it all to work and trying new concepts in-place rather than in isolation. The whole thing could do with a cleanup really
    -   Measure if there's actually a performance benefit for using BLAKE3-JNI instead of the pure Java implementation or if the overhead of JNI and the benefits of JVM's JIT narrows the gap. If there isn't much perf benefit then removing it from being shadowed would be good.
-   HIGH PRIORITY: Cleanup the icon resizing stuff
    -   Look into using `client.helpers.IconLoader` instead of shadowing in the Scalr library
-   Revisit the custom server list entries feature

### New features to consider adding later

-   Support for GDLauncher and SKLauncher
-   Account for modpack authors setting the max memory or java requirement lower than the min and enabling both, making it impossible to satisfy
-   Config option to launch the game in fullscreen by default (while still respecting the player's choice in video settings menu if they change it)
-   Being able to IMC to the itlt mod and get a crash report back if it was your fault (useful for other mods)
-   Optional mods
    -   Show a GUI before the game has finished loading if jars have a file name ending in ".optional" (i.e. "modid.optional.jar" or "modid.optional.jar.disabled") and toggle the ".disabled" file extension before mod loading depending on the user's preference on first launch
    -   Add a client-side command, menu button or at least a config option for users to toggle so they can get the optional mods prompt again if they change their mind about any of their selection
    -   Be smart enough to also enable dependencies for optional mods when needed
