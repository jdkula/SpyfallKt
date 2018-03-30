package pw.jonak.spyfall.frontend

import kotlin.browser.document
import kotlin.js.Date

object CookieManager {
    fun add(pair: Pair<String, String>, expiry: Date) {
        document.cookie = pair + ("expires" to expiry.toUTCString()) + "; path=/"
    }

    fun delete(key: String) {
        document.cookie = (key to "") + ("expires" to Date().toUTCString()) + "; path=/"
    }

    operator fun contains(key: String): Boolean {
        return (key in getCookies()).inlinePrint("$key in cookies? ")
    }

    operator fun get(key: String): String? {
        return getCookies()[key]
    }

    fun Pair<String, String>.toCookie(): String {
        return "$first=$second"
    }

    operator fun Pair<String, String>.plus(other: Pair<String, String>): String {
        return toCookie() + "; " + other.toCookie()
    }

    fun getCookies(): Map<String, String> {
        return document
                .cookie
                .split(";")
                .map { it.trim() }
                .filter { it.split("=").size == 2 }
                .filter { it.split("=")[1] != "" }
                .map { it.split("=")[0] to it.split("=")[1] }
                .toMap()
    }

    private fun <T> T.inlinePrint(prefix: String = ""): T {
        println("$prefix$this")
        return this
    }
}