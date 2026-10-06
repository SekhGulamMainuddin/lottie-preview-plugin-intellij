# Lottie Preview Plugin — Architecture & Development Rules

## IntelliJ Platform Architecture

This plugin follows the official [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html) architecture.
All code **must** use the three sanctioned building blocks:

### 1. Extensions
- Registered declaratively in `plugin.xml` via extension points.
- Examples: `ToolWindowFactory`, `AnAction`.
- Prefer declarative registration for lazy instantiation over programmatic registration.

### 2. Services
- Stateful singletons loaded on demand via `getService()`.
- Scoped to **application** (global) or **project** (per-project instance).
- Use `@Service` annotation for **light services** (preferred when the service doesn't need to be overridden or exposed as API).
- Light service classes **must be `final`** (Kotlin classes are final by default).
- Services that need cleanup **must implement `Disposable`** — they are automatically disposed when their scope ends.
- **Never store service references in fields.** Always call `getInstance()` at the point of use.
- **Avoid heavy work in constructors.** Services are lazily created; keep `init` lightweight.

### 3. Listeners
- Stateless event handlers registered declaratively in `plugin.xml` under `<applicationListeners>` or `<projectListeners>`.
- **Listeners must be stateless.** They must NOT hold state or implement `Disposable`.
- All business logic must be delegated to a **Service**.
- Project-level listeners can accept a `Project` parameter in their constructor.

### Deprecated Patterns (DO NOT USE)
- `ApplicationComponent` / `ProjectComponent` — replaced by Services + Listeners.
- Programmatic listener registration — use `plugin.xml` declarative registration instead.
- Constructor injection of dependency services — retrieve services at the point of use.

---

## Plugin Architecture

```
┌─────────────────────────────────────────────────────┐
│  LottiePreviewService (@Service, project-level)     │
│    - Owns LottieBrowserManager lifecycle            │
│    - Single source of truth for preview state       │
│    - loadAnimation(VirtualFile)                     │
├─────────────────────────────────────────────────────┤
│  Extensions (registered in plugin.xml)              │
│    - LottiePreviewWindowFactory → builds UI panel   │
│    - OpenLottieAction → context-menu entry          │
├─────────────────────────────────────────────────────┤
│  Listeners (stateless, registered in plugin.xml)    │
│    - LottieFileListener → auto-preview on tab switch│
│    - LottieVfsListener → reload on change, clear    │
│                           on deletion               │
├─────────────────────────────────────────────────────┤
│  Browser Layer (implementation detail)              │
│    - LottieBrowserManager (interface)               │
│    - JcefLottieBrowserManager (JCEF renderer)       │
│    - NoOpLottieBrowserManager (fallback)            │
│    - JcefAvailability (reflection-based check)      │
├─────────────────────────────────────────────────────┤
│  Utilities                                          │
│    - LottieFileValidator (stateless, object)        │
│    - PlaybackActions (toolbar builder)              │
└─────────────────────────────────────────────────────┘
```

### Key Principles

1. **Service is the single source of truth.** Actions and listeners call the Service — never dig into UI internals (tool window content manager, panel fields, etc.) to find state.
2. **Panel is a thin UI shell.** It gets the browser component from the Service and lays it out. It does NOT own the browser lifecycle.
3. **Browser layer is an implementation detail.** Only the Service creates and holds the `LottieBrowserManager`. UI and listeners access it through the Service.

---

## Package Structure

```
com.lottiepreview.plugin/
├── actions/          # AnAction subclasses and toolbar builders
├── browser/          # LottieBrowserManager interface + implementations
├── file/             # File validation and VFS/editor listeners
├── service/          # Project-level services (LottiePreviewService)
└── toolwindow/       # ToolWindowFactory and UI panels
```

---

## Coding Conventions

- **Language:** Kotlin (JVM target 17).
- **Concurrency:** Use `kotlinx.coroutines` with structured concurrency. Prefer service-scoped `CoroutineScope` injected via constructor.
- **Disposal:** Register child disposables with `Disposer.register(parent, child)`. Never leak disposable resources.
- **Threading:** UI mutations on EDT. File I/O on background threads. `getService()` is safe from any thread.
- **Error handling:** Use `runCatching` for recoverable errors. Log via `Logger.getInstance()`. Never swallow exceptions silently.

## Build & CI

- **Gradle wrapper:** `./gradlew` from the `lottie-preview-plugin/` directory.
- **JDK:** Use JDK 17 (Zulu) for building. Set `JAVA_HOME` if your default JDK differs.
- **Target IDE:** Android Studio Panda 4 Patch 1 (build 253.x) and newer, including Rabbit (2026.2+).
- **CI runs on:** macOS (GitHub Actions) with Android Studio installed for `verifyPlugin`.

---

## JCEF Compatibility (Rabbit 2026.2+ / IntelliJ 2026.2+)

Starting with Android Studio Rabbit (2026.2) and IntelliJ 2026.2, JCEF was extracted from
the core platform into a separate **"Web Browser (JCEF)"** plugin. If a user does not have
that plugin installed, any direct reference to `com.intellij.ui.jcef.JBCefApp` will trigger
a `NoClassDefFoundError` at class-load time, crashing the plugin.

### How This Plugin Handles It

1. **Optional `<depends>` in `plugin.xml`:**
   ```xml
   <depends optional="true" config-file="jcef-optional.xml">com.intellij.modules.jcef</depends>
   ```
   This tells the platform to wire JCEF classes into this plugin's classloader **when
   available**, without making it a hard requirement.

2. **`jcef-optional.xml`** — An intentionally minimal config file (`<idea-plugin/>`) that
   satisfies the `config-file` contract. No conditional extensions are registered there.

3. **`JcefAvailability.isAvailable()`** — A reflection-based helper that probes for
   `JBCefApp` via `Class.forName()` before calling `isSupported()`. All JCEF-dependent
   code paths gate on this method, so no hard class reference to JCEF exists outside the
   `JcefLottieBrowserManager` implementation.

4. **Fallback path** — When JCEF is unavailable, `NoOpLottieBrowserManager` is used instead,
   which renders the informative `JcefUnsupportedPanel` with troubleshooting steps.

### Rules for Future Changes

- **NEVER** import or reference `com.intellij.ui.jcef.*` classes outside the `browser/`
  package's JCEF-specific implementations (`JcefLottieBrowserManager`).
- **ALWAYS** use `JcefAvailability.isAvailable()` to check JCEF support. Do NOT use
  `JBCefApp.isSupported()` directly — it will crash on Rabbit+ without the JCEF plugin.
- If adding new JCEF-dependent extensions or services, register them in `jcef-optional.xml`
  instead of the main `plugin.xml`.
- Keep `pluginUntilBuild` unset (open-ended) to support future IDE versions.
