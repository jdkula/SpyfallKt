package pw.jonak.spyfall.frontend

import kotlin.browser.document
import kotlin.js.Date

/**
 * Manages cookies in the browser idiomatically.
 */
@Suppress("RedundantVisibilityModifier")
public object CookieManager {

    /**
     * Adds a key-value [pair] to cookies with some [expiry].
     */
    public fun add(pair: Pair<String, String>, expiry: Date) {
        document.cookie = pair.toCookie() + "; " + ("expires" to expiry.toUTCString()).toCookie() + "; path=/"
    }

    /**
     * Deletes a [key] from the browser's cookie store.
     */
    public fun delete(key: String) {
        document.cookie = (key to "").toCookie() + "; " + ("expires" to Date().toUTCString()).toCookie() + "; path=/"
    }

    /**
     * Returns true if the [key] is in the cookie store as returned by [getCookies]
     */
    public operator fun contains(key: String): Boolean {
        return key in getCookies()
    }

    /**
     * Provides indexed access to cookies.
     */
    public operator fun get(key: String): String? {
        return getCookies()[key]
    }

    /**
     * Turns a [Pair] into a cookie key-value pair as recognized by JavaScript
     */
    private fun Pair<String, String>.toCookie(): String = "$first=$second"

    /**
     * Deletes all cookies from the browser.
     */
    public fun deleteAll() {
        getCookies().forEach { (key, _) ->
            delete(key)
        }
    }

    /**
     * Gets all cookies as a map.
     * Excludes unset but present cookies.
     */
    private fun getCookies(): Map<String, String> =
        document
            .cookie
            .split(";")
            .map { it.trim() }
            .filter { it.split("=").size == 2 }
            .filter { it.split("=")[1] != "" }
            .map { it.split("=")[0] to it.split("=")[1] }
            .toMap()
}