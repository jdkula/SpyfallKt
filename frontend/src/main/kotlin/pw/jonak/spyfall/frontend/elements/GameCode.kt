package pw.jonak.spyfall.frontend.elements

import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.*
import org.w3c.dom.HTMLInputElement
import pw.jonak.spyfall.frontend.LocalizationInformation.getLocalization
import react.*
import react.dom.input
import react.dom.span
import kotlin.browser.document
import kotlin.browser.window

/**
 * The state of [GameCode]
 * @property isField Determines if this [GameCode] is in field mode (for copying)
 */
interface GameCodeState : RState {
    var isField: Boolean
}

/**
 * The properties of [GameCode]
 * @property gameCode The game code to be displayed
 */
interface GameCodeProps : RProps {
    var gameCode: String
}

/**
 * Displays a game code, allowing the user to tap-to-copy.
 */
class GameCode(props: GameCodeProps) : RComponent<GameCodeProps, GameCodeState>(props) {

    override fun GameCodeState.init(props: GameCodeProps) {
        isField = false
    }

    override fun RBuilder.render() {
        span(classes = "col s12 center-align") {
            +"${getLocalization("ui", "code")}: "
            if(state.isField) {
                span(classes = "teletype copybox") {
                    input(classes = "copybox", type = InputType.text) {
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

/** Allows you to use [GameCode] from [RBuilder] contexts */
fun RBuilder.gameCode(code: String) = child(GameCode::class) {
    attrs.gameCode = code
}