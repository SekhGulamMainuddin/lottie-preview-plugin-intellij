# Contributing to Lottie Preview Plugin

First off, thank you for considering contributing to the Lottie Preview plugin! 

## 🛠 Development Environment Setup

This plugin targets the **IntelliJ Platform** and uses the `intellij-platform-gradle-plugin`.

### Prerequisites
1. **JDK 17**: JetBrains requires Java 17 for plugin compilation. Ensure your `JAVA_HOME` is pointing to JDK 17.
2. **IntelliJ IDEA**: Download IntelliJ IDEA Community or Ultimate to use as your development IDE.

### Running the Plugin Locally
1. Clone the repository:
   ```bash
   git clone https://github.com/SekhGulamMainuddin/lottie-preview-plugin-intellij.git
   cd lottie-preview-plugin-intellij
   ```
2. Open the project in IntelliJ IDEA.
3. Open the **Gradle Tool Window** (usually on the right sidebar).
4. Navigate to `Tasks -> intellijPlatform -> runIde` and double-click to run.
   - *Alternatively, run from the terminal:* `./gradlew runIde`
5. A sandbox instance of Android Studio/IntelliJ will launch with the plugin pre-installed so you can test your changes live!

### Architecture Overview
- **Kotlin UI**: Located in `src/main/kotlin`. Handles IDE interaction, Virtual File System (VFS) events, and rendering the Swing UI toolbar.
- **JCEF Browser Manager**: `JcefLottieBrowserManager.kt` bridges the gap between Kotlin and the embedded Chromium browser.
- **Web Player**: Located in `src/main/resources/lottiepreview`. Contains `player.html`, `lottie.min.js` (for `.json`), and `dotlottie.js` & `dotlottie-player.wasm` (for `.lottie` WASM engine). 

> **Note on Web Libraries**: If you need to update `dotlottie.js`, do not import external CDNs. You must bundle it using a local bundler (like `esbuild`) and replace the files in `src/main/resources/lottiepreview/` to ensure offline support.

## 🧪 Testing & Verification
Before submitting a Pull Request, please ensure your code passes the JetBrains marketplace verification checks:

```bash
./gradlew buildPlugin verifyPlugin
```
This task checks for deprecations, experimental API usage warnings, and ensures compatibility with target IDE builds.

## 📥 Pull Request Process
1. Create a feature branch (`git checkout -b feature/my-new-feature`).
2. Commit your changes (`git commit -m "feat: add some feature"`).
3. Push to the branch (`git push origin feature/my-new-feature`).
4. Open a Pull Request!

We actively review all contributions. Thank you for making the plugin better!
