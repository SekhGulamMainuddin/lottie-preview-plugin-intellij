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
- **Target IDE:** Android Studio Panda 4 Patch 1 (build 253.x).
- **CI runs on:** macOS (GitHub Actions) with Android Studio installed for `verifyPlugin`.
