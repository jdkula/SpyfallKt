package pw.jonak.spyfall.frontend

import kotlinext.js.getOwnPropertyNames
import org.w3c.xhr.JSON
import org.w3c.xhr.XMLHttpRequest
import org.w3c.xhr.XMLHttpRequestResponseType
import kotlin.browser.window
import kotlin.js.Json
import kotlin.properties.Delegates

object LocalizationInformation {
    /**
     * Keeps track of the [XMLHttpRequest]s that have been sent out for
     * localizations -- this way we don't get the "Whoops the site crashed
     * because it sent out 20 XHRs at once" problem.
     */
    private val xhrsOut = HashSet<String>()

    private var localizationName: String by Delegates.observable("en_US") { _, _, _ ->
        localizations = HashMap()
    }
    private var localizations: HashMap<String, Json> by Delegates.observable(HashMap()) { _, _, _ ->
        updatePage()
    }
    internal var localizationOptions: Map<String, String> = mapOf()
        private set

    /**
     * Fills up [localizationOptions] from the server.
     */
    internal fun getLocalizationList() {
        val xhr = XMLHttpRequest()
        xhr.onload = {
            val status = xhr.status
            if (status == 200.toShort()) {
                val mapJson = xhr.response.unsafeCast<Json>()
                val props = mapJson.getOwnPropertyNames()
                localizationOptions = props.map { it to mapJson[it].unsafeCast<String>() }.toMap()
                updatePage()
            }
        }
        xhr.open(
            "GET",
            "${window.location.protocol}//${window.location.host}${window.location.pathname}/localization/localizations.json"
        )
        xhr.responseType = XMLHttpRequestResponseType.JSON
        xhr.send()
    }

    /**
     *
     */
    internal fun getLocalization(localizationGroup: String, localizationElement: String): String {
        if (localizationGroup !in localizations && localizationGroup !in xhrsOut) {
            xhrsOut += localizationGroup
            val xhr = XMLHttpRequest()
            xhr.onload = {
                val status = xhr.status
                xhrsOut -= localizationGroup
                if (status == 200.toShort() && localizationGroup !in localizations) {
                    localizations[localizationGroup] = xhr.response.unsafeCast<Json>()
                    updatePage()
                } else {
                }
            }
            xhr.open(
                "GET",
                "${window.location.protocol}//${window.location.host}${window.location.pathname}/localization/$localizationName/$localizationGroup.json"
            )
            xhr.responseType = XMLHttpRequestResponseType.JSON
            xhr.send()
        }
        val x = localizations[localizationGroup]
        val y = x?.get(localizationElement)
        val z = y as? String
        return z ?: localizationElement
    }

    internal fun changeLocalization(newLocalization: String): Boolean {
        return if(newLocalization in localizationOptions) {
            clearLocalizations()
            localizationName = newLocalization
            true
        } else false
    }

    private fun clearLocalizations() {
        localizations = HashMap()
    }
}