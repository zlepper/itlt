![It's the little things logo](docs/logo/inverted-transparent/logo-long-inverted-transparent.png)

# [![](http://cf.way2muchnoise.eu/full_its-the-little-things_downloads.svg)](http://www.curseforge.com/minecraft/mc-mods/its-the-little-things) [![](https://cf.way2muchnoise.eu/versions/Available%20for%20MC_its-the-little-things_all.svg)](http://www.curseforge.com/minecraft/mc-mods/its-the-little-things/files)

## About itlt

It's the little things, the little finishing touches to your modpack. :)

### Download

Available for download at CurseForge: https://www.curseforge.com/minecraft/mc-mods/its-the-little-things

## Players and modpack authors

### Help

If you need help using or troubleshooting itlt, you can open a support ticket by [clicking here](https://github.com/zlepper/itlt/issues/new?labels=help&template=3_user-help.md)

### Problems and bugs

If you're having a problem with itlt crashing or being buggy, please [file a new bug report](https://github.com/zlepper/itlt/issues/new?labels=bug&template=1_bug_report.md) and I'll look into it.

### Suggestions

If you have an idea or suggestion for itlt, I'd love to hear it!

Check out [this issues page](https://github.com/zlepper/itlt/issues?q=is%3Aissue+is%3Aopen+label%3Asuggestion) to see if it's already been suggested and leave a thumbs up on things you would like to see implemented. To make a new suggestion, [click here](https://github.com/zlepper/itlt/issues/new?labels=suggestion&template=2_suggestion.md).

## Contributors and developers

Please refer to the [Technical Documentation](TechnicalDocumentation.md) - it provides a technical overview, troubleshooting, to-do lists and various details about how stuff works and how to add or change stuff.

### Help

If you need a hand working with itlt, ping me (@Paint_Ninja) in **#squirrels** on [Forgecord](https://discord.gg/UvedJ9m). Remember to bare their rules in mind. Alternatively, [open a dev support ticket](https://github.com/PaintNinja/issue-template-test/issues/new?labels=dev+help&template=4_dev_help.md) and I'll get back to you when I can.

### Notes

-   All branches are actively maintained, but don't worry! For contributing, you only need to submit a PR to one branch (of your choice) and I'll handle porting to the rest after merging your PR.
-   If you want to add support for a Minecraft version that doesn't already have a branch, open a dev support ticket and I'll make the branch for you so you can make your PR.

### Branches

#### Current

| Branch                                                          | Minecraft version | Mod version | ForgeGradle version              | Gradle version |
| --------------------------------------------------------------- | ----------------- | ----------- | -------------------------------- | -------------- |
| 1.16-2.0-rewrite (you're here)                                  | 1.16.x            | **v2**      | ForgeGradle 4.1                  | Gradle 6.9     |
| [master](https://github.com/zlepper/itlt/tree/1.16-2.0-rewrite) | 1.16.x            | v1          | ForgeGradle 3                    | Gradle 4.9     |
| [1.15](https://github.com/zlepper/itlt/tree/1.15)               | 1.15.2            | v1          | ForgeGradle 3                    | Gradle 4.9     |
| [1.14](https://github.com/zlepper/itlt/tree/1.14)               | 1.14.x            | v1          | ForgeGradle 3                    | Gradle 4.10.3  |
| [1.12](https://github.com/zlepper/itlt/tree/1.12)               | 1.12.2            | v1          | ForgeGradle 4.1                  | Gradle 6.9     |
| [1.11](https://github.com/zlepper/itlt/tree/1.11)               | 1.11.x            | v1          | ForgeGradle 2.2.1                | Gradle 4.10.3  |
| [1.10](https://github.com/zlepper/itlt/tree/1.10)               | 1.10.x            | v1          | ForgeGradle 2.2.1                | Gradle 4.10.3  |
| [1.9.4](https://github.com/zlepper/itlt/tree/1.9.4)             | 1.9.4             | v1          | ForgeGradle 2.2.1                | Gradle 4.10.3  |
| [1.9](https://github.com/zlepper/itlt/tree/1.9)                 | 1.9.0             | v1          | ForgeGradle 2.1.1                | Gradle 4.10.3  |
| [1.8.9](https://github.com/zlepper/itlt/tree/1.8.9)             | 1.8.9             | v1          | ForgeGradle 2.1.1                | Gradle 4.10.3  |
| [1.8](https://github.com/zlepper/itlt/tree/1.8)                 | 1.8.0             | v1          | ForgeGradle 2.0-SNAPSHOT-aa67375 | Gradle 4.7     |
| [1.7](https://github.com/zlepper/itlt/tree/1.7)                 | 1.7.10            | v1          | ForgeGradle 1.2.1                | Gradle 4.4.1   |
| [1.6](https://github.com/zlepper/itlt/tree/1.6)                 | 1.6.x             | v1          | ForgeGradle 1.0                  | Gradle 3.0     |

#### Legacy

These are still maintained, but are worked on much less often due to being harder to setup and work with in general.

I've provided `Instructions.md` and some additional files in each of these branches to help. I strongly recommend you read the instructions if you're not already familiar with the process, otherwise you're going end up spending a while troubleshooting. These Python scripts no longer work out of the box like they used to - they need a bit of manual setup and a specific environment to work in. RetroGradle aims to eventually port these MC versions to use ForgeGradle 4 but it's a long way off - possibly years from now.

| Branch                                                          | Minecraft version | Mod version | Dev toolchain                                              | Python version |
| --------------------------------------------------------------- | ----------------- | ----------- | ---------------------------------------------------------- | -------------- |
| [1.5](https://github.com/zlepper/itlt/tree/1.5)                 | 1.5.x             | v1          | Forge Src 7.8.1.738(?) + MCP 7.51 scripts + Manual labour  | Python 2.7.3   |
| [1.4.6/1.4.7](https://github.com/zlepper/itlt/tree/1.4.6/1.4.7) | 1.4.6 and 1.4.7   | v1          | Forge Src 6.6.2.534(?) + MCP 7.26a scripts + Manual labour | Python 2.7.3   |
| [1.4.4/1.4.5](https://github.com/zlepper/itlt/tree/1.4.4/1.4.5) | 1.4.4 and 1.4.5   | v1          | Forge Src 6.4.2.448(?) + MCP 7.23 scripts + Manual labour  | Python 2.7.3   |
| [1.3.2](https://github.com/zlepper/itlt/tree/1.3.2)             | 1.3.2             | v1          | Forge Src 4.3.5.318(?) + MCP 7.2 scripts + Manual labour   | Python 2.7.2   |
| [1.2.5](https://github.com/zlepper/itlt/tree/1.2.5)             | 1.2.5             | v1          | Forge Src 3.4.9.171(?) + MCP 6.2 scripts + Manual labour   | Python 2.7.2   |