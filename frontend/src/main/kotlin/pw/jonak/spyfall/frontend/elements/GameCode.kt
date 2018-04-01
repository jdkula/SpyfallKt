package pw.jonak.spyfall.frontend.elements

import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.*
import org.w3c.dom.HTMLInputElement
import pw.jonak.spyfall.frontend.getLocalization
import react.*
import react.dom.input
import react.dom.span
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.json

interface GameCodeState : RState {
    var isField: Boolean
}

interface GameCodeProps : RProps {
    var gameCode: String
}

class GameCode(props: GameCodeProps) : RComponent<GameCodeProps, GameCodeState>(props) {

    override fun GameCodeState.init(props: GameCodeProps) {
        isField = false
    }

    override fun RBuilder.render() {
        span(classes = "col s12 center-align") {
            +"${getLocalization("ui", "code")}: "
            if(state.isField) {
                span(classes = "teletype") {
                    attrs["style"] = json("width" to "7ex", "max-width" to "7ex")
                    input(type = InputType.text) {
                        attrs["style"] = json("width" to "7ex", "max-width" to "7ex")
                        attrs {
                            value = "${window.location.protocol}//${window.location.host}${window.location.pathname}/g/${props.gameCode}"
                            id = "gamecodeentry"
                            onMouseOutFunction = {
                                setState {
                                    isField = false
                                }
                            }
                            onBlurFunction = {
                                setState {
                                    isField = false
                                }
                            }
                            onClickFunction = {
                                val elem = document.getElementById("gamecodeentry") as HTMLInputElement
                                elem.select()
                                document.execCommand("copy")
                                elem.value = getLocalization("ui", "copied")
                                elem.blur()
                            }
                        }
                    }
                }
            } else {
                span(classes = "teletype") {
                    attrs {
                        onFocusFunction = {
                            setState {
                                isField = true
                            }
                        }
                        onMouseOverFunction = {
                            setState {
                                isField = true
                            }
                        }
                    }
                    +props.gameCode
                }
            }
        }
    }
}

fun RBuilder.gameCode(code: String) = child(GameCode::class) {
    attrs.gameCode = code
}