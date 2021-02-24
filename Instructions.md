1. Clone the repo
2. Make sure you have Java 7 and that JAVA_HOME points to it
3. Open the "Configure Java" app, go to the "Advanced" tab and enable TLSv1.1 and v1.2 under the "Advanced Security Settings" section.
4. Run `./gradlew setupDecompWorkspace`
5. Open build/unpacked/dev.json
6. Find and replace `"url" : "http://repo.maven.apache.org/maven2"` with `"url" : "http://insecure.repo1.maven.org/maven2"`
7. Run `./gradlew setupDecompWorkspace` again
8. Watch it fail, this time due to lwjgl-platform and lwjgl-parent 2.9.0
9. Give up, set JAVA_HOME back to Java 8, run `./gradlew setupDevWorkspace` and import it into IntelliJ