package com.lottiepreview.plugin.browser

import com.intellij.openapi.diagnostic.Logger
import java.lang.reflect.InvocationTargetException

/**
 * Safe JCEF availability check that works across all IntelliJ Platform versions.
 *
 * In Android Studio Rabbit (2026.2+) and IntelliJ 2026.2+, JCEF was moved from
 * a built-in module into a separate "Web Browser (JCEF)" plugin. If that plugin
 * is not installed, directly referencing `com.intellij.ui.jcef.JBCefApp` triggers
 * a `NoClassDefFoundError` at class-load time.
 *
 * Availability requires `JBCefApp` on **this plugin's classloader** (via optional
 * `<depends>` entries in plugin.xml) so [JcefLottieBrowserManager] can use JCEF types.
 */
object JcefAvailability {

    private const val JB_CEF_APP = "com.intellij.ui.jcef.JBCefApp"

    private val log = Logger.getInstance(JcefAvailability::class.java)

    /**
     * The plugin classloader is fixed for the IDE session, so once [JB_CEF_APP] is missing
     * it stays missing until restart. Cache that to avoid re-probing and re-logging.
     */
    @Volatile
    private var jbCefAppMissing = false

    /**
     * Returns `true` only when **both** conditions hold:
     *  1. The `JBCefApp` class is on this plugin's classloader.
     *  2. `JBCefApp.isSupported()` returns `true` (JBR + natives + registry allow JCEF).
     */
    @JvmStatic
    fun isAvailable(): Boolean {
        val jbCefAppClass = loadJBCefAppOnPluginClasspath() ?: return false
        return invokeIsSupported(jbCefAppClass)
    }

    private fun loadJBCefAppOnPluginClasspath(): Class<*>? {
        if (jbCefAppMissing) return null
        val pluginClassLoader = JcefAvailability::class.java.classLoader
        return try {
            Class.forName(JB_CEF_APP, true, pluginClassLoader)
        } catch (_: ClassNotFoundException) {
            jbCefAppMissing = true
            log.info(
                "JCEF not available: $JB_CEF_APP class not found. On 2026.2+ install or enable the " +
                    "Web Browser (JCEF) plugin and restart the IDE."
            )
            null
        } catch (e: LinkageError) {
            jbCefAppMissing = true
            log.info("JCEF not available: linkage failure (${e.message})")
            null
        }
    }

    private fun invokeIsSupported(jbCefAppClass: Class<*>): Boolean {
        return try {
            val method = jbCefAppClass.getMethod("isSupported")
            (method.invoke(null) as? Boolean) == true
        } catch (e: InvocationTargetException) {
            log.warn("JCEF isSupported() failed", e.targetException ?: e)
            false
        } catch (e: LinkageError) {
            log.info("JCEF not available: linkage failure invoking isSupported (${e.message})")
            false
        } catch (e: ReflectiveOperationException) {
            log.warn("JCEF availability check failed unexpectedly", e)
            false
        }
    }
}
