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
        local(androidStudioPath)
        bundledPlugin("org.jetbrains.android")
    }

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
}

kotlin {
    jvmToolchain(17)
}

intellijPlatform {
    pluginConfiguration {
        id = "com.lottiepreview.plugin"
        name = "Lottie Preview"
        version = providers.gradleProperty("pluginVersion").get()

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild").get()
            untilBuild = providers.gradleProperty("pluginUntilBuild").get()
        }

        vendor {
            name = "Lottie Preview"
        }

        description = """
            Preview Lottie animations directly inside Android Studio using the embedded JCEF browser.
        """.trimIndent()
    }

    pluginVerification {
        ides {
            local(androidStudioPath)
        }
    }
}
