package pw.jonak.spyfall.frontend.state

import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.HTMLInputElement
import pw.jonak.spyfall.common.UserRegistrationInformation
import pw.jonak.spyfall.common.UserRegistrationRequest
import pw.jonak.spyfall.common.deserialize
import pw.jonak.spyfall.common.serialize
import pw.jonak.spyfall.frontend.ApplicationState
import pw.jonak.spyfall.frontend.FrontendMain
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.*
import kotlin.browser.document

interface StartProps : RProps {
    var name: String
}

interface StartState : RState {
    var currentName: String
}

class Start(props: StartProps) : RComponent<StartProps, StartState>(props) {
    override fun StartState.init(props: StartProps) {
        currentName = props.name
    }

    override fun RBuilder.render() {
        p {
            +"Hello!"
        }
        form {
            attrs {
                onSubmitFunction = {
                    val name = (document.getElementById("username") as HTMLInputElement).value
                    FrontendMain.userName = name
                    FrontendMain.socketClient.sendMessage(UserRegistrationRequest(name).serialize()) {
                        val info = it.data?.toString()?.deserialize()
                        if(info is UserRegistrationInformation) {
                            FrontendMain.userId = info.userId
                            println("Found User ID ${FrontendMain.userId}!")
                            FrontendMain.state = ApplicationState.JOIN
                        }
                    }
                    println("Found $name!")
                    it.preventDefault()
                }
            }
            input(type = InputType.text, classes = "login") {
                attrs {
                    id = "username"
                }
            }
            label {
                +"Username"
                attrs["htmlFor"] = "username"
            }
            br {}
            input(type = InputType.submit, classes = "login") {
                attrs {
                    id = "joinButton"
                    value = "Join"
                }
            }
            label {
                +"Join"
                attrs["htmlFor"] = "login"
            }
        }
    }
}



fun RBuilder.start(name: String = "World") = child(Start::class) {
    attrs.name = "World"
}