package pw.jonak.spyfall.frontend.state

import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.HTMLInputElement
import pw.jonak.spyfall.frontend.FrontendMain
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.br
import react.dom.form
import react.dom.input
import react.dom.label
import kotlin.browser.document

interface JoinProps : RProps {
    var name: String
}

class Join : RComponent<JoinProps, RState>() {

    override fun RBuilder.render() {
        form {
            attrs {
                onSubmitFunction = {
                    FrontendMain.gameCode = (document.getElementById("gamecode") as? HTMLInputElement)?.value
                    println("Found gamecode ${FrontendMain.gameCode}")
                    it.preventDefault()
                }
            }

            input(type = InputType.text, classes = "login") {
                attrs {
                    readonly = true
                    value = props.name
                }
            }
            br { }
            input(type = InputType.text, classes = "login") {
                attrs {
                    id = "gamecode"
                }
            }
            label {
                +"Game Code"
                attrs["htmlFor"] = "gamecode"
            }
            input(type = InputType.submit) {
                attrs {
                    value = "Join Game!"
                }
            }
        }
    }
}

fun RBuilder.join(name: String) = child(Join::class) {
    attrs.name = name
}
