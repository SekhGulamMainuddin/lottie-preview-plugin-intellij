package com.lottiepreview.plugin.browser

import com.intellij.openapi.diagnostic.Logger

/**
 * Safe JCEF availability check that works across all IntelliJ Platform versions.
 *
 * In Android Studio Rabbit (2026.2+) and IntelliJ 2026.2+, JCEF was moved from
 * a built-in module into a separate "Web Browser (JCEF)" plugin. If that plugin
 * is not installed, directly referencing `com.intellij.ui.jcef.JBCefApp` triggers
 * a `NoClassDefFoundError` at class-load time.
 *
 * This helper uses reflection to probe for the class first, so the calling code
 * never hard-links against JCEF symbols at the point of the check. The rest of
 * the JCEF-specific code (e.g. `JcefLottieBrowserManager`) lives behind this
 * gate and is only loaded when JCEF is genuinely available.
 */
object JcefAvailability {

    private val log = Logger.getInstance(JcefAvailability::class.java)

    /**
     * Returns `true` only when **both** conditions hold:
     *  1. The `JBCefApp` class is on the runtime classpath (i.e. the JCEF plugin
     *     is installed on Rabbit+, or it's a pre-Rabbit IDE that bundles JCEF).
     *  2. `JBCefApp.isSupported()` returns `true` (the current JVM boot runtime
     *     actually ships the native CEF binaries).
     *
     * Any reflective or linkage failure is caught and treated as "unsupported".
     */
    @JvmStatic
    fun isAvailable(): Boolean {
        return try {
            val clazz = Class.forName("com.intellij.ui.jcef.JBCefApp")
            val method = clazz.getMethod("isSupported")
            method.invoke(null) as Boolean
        } catch (e: LinkageError) {
            log.info("JCEF not available: linkage failure (${e.message})")
            false
        } catch (e: ClassNotFoundException) {
            log.info("JCEF not available: JBCefApp class not found")
            false
        } catch (e: Throwable) {
            log.warn("JCEF availability check failed unexpectedly", e)
            false
        }
    }
}
