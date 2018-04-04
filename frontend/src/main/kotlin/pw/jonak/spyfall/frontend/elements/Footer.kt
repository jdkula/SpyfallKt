package pw.jonak.spyfall.frontend.elements

import kotlinx.html.id
import kotlinx.html.js.onClickFunction
import pw.jonak.spyfall.frontend.LocalizationInformation.changeLocalization
import pw.jonak.spyfall.frontend.LocalizationInformation.getLocalization
import pw.jonak.spyfall.frontend.LocalizationInformation.localizationOptions
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.*
import kotlin.browser.document
import kotlin.js.json

class Footer : RComponent<RProps, RState>() {
    override fun RBuilder.render() {
        div(classes = "footer center-align") {
            p(classes = "attribution") {
                attrs["dangerouslySetInnerHTML"] = json("__html" to getLocalization("ui", "footer"))
            }
            div(classes = "stacked") {
                a(classes = "btn-small waves-effect waves-light grey darken-2") {
                    attrs {
                        href = getLocalization("ui", "rules url")
                    }
                    i(classes = "material-icons left") { +"help_outline" }
                    +" ${getLocalization("ui", "rules")}"
                }
            }
            div {
                button(classes = "dropdown-trigger btn-small waves-effect waves-light grey darken-3") {
                    +getLocalization("ui", "choose language")
                    attrs["data-target"] = "language-dropdown"
                }

                ul(classes = "dropdown-content") {
                    attrs {
                        id = "language-dropdown"
                    }
                    localizationOptions.keys.map { language ->
                        li {
                            a {
                                accessibleBullet()
                                +language
                                attrs {
                                    href = ""
                                    onClickFunction = {
                                        it.preventDefault()
                                        changeLocalization(localizationOptions[language]!!)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        @Suppress("UNUSED_VARIABLE")
        val elem = document.querySelector(".dropdown-trigger")
        js("M.Dropdown.init(elem, {});")
    }
}

fun RBuilder.footer() = child(Footer::class) {}