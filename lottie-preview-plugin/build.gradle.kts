plugins {
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
    id("org.jetbrains.intellij.platform") version "2.10.4"
}

group = "com.lottiepreview"
version = providers.gradleProperty("pluginVersion").get()

val androidStudioPath = providers.environmentVariable("ANDROID_STUDIO_PATH")
    .orElse("/Applications/Android Studio.app")
    .get()

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // Compile against an IDE that still ships JCEF in the core platform. Android Studio Rabbit
        // (2026.2+) moved JCEF to the optional "Web Browser (JCEF)" plugin, so its jars lack
        // JBCefBrowser. At runtime JCEF is wired via the optional <depends> in plugin.xml.
        intellijIdeaCommunity("2025.2.4")
    }

}

kotlin {
    jvmToolchain(21)
}

tasks.withType<JavaCompile> {
    options.release.set(17)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

intellijPlatform {
    pluginConfiguration {
        id = "com.lottiepreview.plugin"
        name = "Lottie Preview"
        version = providers.gradleProperty("pluginVersion").get()

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            val until = providers.gradleProperty("pluginUntilBuild")
            if (until.isPresent && until.get().trim().isNotEmpty()) {
                untilBuild = until
            }
        }

        vendor {
            name = "Lottie Preview"
        }

        description = """
            Preview Lottie and dotLottie animations directly inside IntelliJ-based IDEs using the embedded JCEF browser.
        """.trimIndent()
    }

    pluginVerification {
        ides {
            local(androidStudioPath)
        }
    }
}
