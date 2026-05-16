# Lottie Preview Plugin for IntelliJ

A powerful, lightweight, and offline-first Lottie animation previewer for IntelliJ IDEA and Android Studio. Preview both `.json` and `.lottie` animations instantly within your IDE.

## ✨ Features

- **Instant Preview**: Right-click any `.json` or `.lottie` file and select "Open in Lottie Preview".
- **Dual-Engine Support**: 
  - Standard `.json` rendering via `lottie-web`.
  - Native `.lottie` support via `@lottiefiles/dotlottie-web` WebAssembly engine.
- **Offline First**: No internet connection required. All engines are bundled locally.
- **Responsive Toolbar**: Wrapping toolbar that adapts to your tool window width.
- **Playback Controls**: Play, Pause, Stop, Speed control (0.5x to 2.0x), and Loop toggling.
- **Visual Debugging**: Toggle animation boundaries to see the exact layout dimensions.
- **Overlay Info**: See the playing filename directly on top of the preview.

## 🚀 Installation

*Currently available via manual build. Coming soon to JetBrains Marketplace.*

1. Download the latest release `.zip` from the [Releases](https://github.com/SekhGulamMainuddin/lottie-preview-plugin-intellij/releases) page.
2. In IntelliJ/Android Studio, go to `Settings -> Plugins -> ⚙️ -> Install Plugin from Disk...`.
3. Select the downloaded `.zip` and restart your IDE.

## 🛠 Usage

1. Open any project containing Lottie animations.
2. Select a `.json` or `.lottie` file in the Project view.
3. The **Lottie Preview** tool window will open (usually on the right sidebar).
4. Use the toolbar to control playback, adjust speed, or toggle boundaries.

## 🤝 Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for setup instructions and development guidelines.

## 📜 Credits & Acknowledgments

This plugin is made possible by the incredible work of the following open-source projects:

- **[lottie-web](https://github.com/airbnb/lottie-web)** by Airbnb - The industry standard for rendering Lottie JSON animations.
- **[dotlottie-web](https://github.com/LottieFiles/dotlottie-web)** by LottieFiles - Providing the high-performance WebAssembly engine for `.lottie` archives.

## ⚖️ License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
