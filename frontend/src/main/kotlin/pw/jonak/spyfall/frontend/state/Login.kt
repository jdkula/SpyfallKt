package pw.jonak.spyfall.frontend.state

import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.HTMLInputElement
import pw.jonak.spyfall.common.EnsureUserRegistration
import pw.jonak.spyfall.common.UserRegistrationRequest
import pw.jonak.spyfall.common.serialize
import pw.jonak.spyfall.frontend.ApplicationState
import pw.jonak.spyfall.frontend.appState
import pw.jonak.spyfall.frontend.dummyUser
import pw.jonak.spyfall.frontend.elements.introHeader
import pw.jonak.spyfall.frontend.socketClient
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.*
import kotlin.browser.document

const val defaultPlaceholder = ""

interface LoginProps : RProps {
    var name: String
}

class LoginState : RState {
    lateinit var currentName: String
}

class Login(props: LoginProps) : RComponent<LoginProps, LoginState>(props) {
    override fun LoginState.init(props: LoginProps) {
        currentName = props.name
    }

    override fun RBuilder.render() {
        introHeader({}, {})
        span(classes = "accessibilityonly") {
            +"Login Menu"
        }
        form {
            attrs {
                id = "loginForm"
                onSubmitFunction = {
                    it.preventDefault()
                    val name = (document.getElementById("username") as HTMLInputElement).value
                    state.currentName = if (name.isNotEmpty()) name else if (props.name != defaultPlaceholder) props.name else ""
                    if (state.currentName.isNotEmpty()) {
                        register(state.currentName)
                    }
                }
            }
            div(classes = "row") {
                div(classes = "s12 input-field") {
                    input(type = InputType.text, classes = "login") {
                        attrs {
                            id = "username"
                            placeholder = props.name
                        }
                    }
                    label {
                        +"Name"
                        attrs["htmlFor"] = "username"
                    }
                }

                input(type = InputType.submit, classes = "waves-effect waves-light btn col s12") {
                    attrs {
                        value = "Login"
                        id = "loginButton"
                    }
                }
            }
        }
        br { }
        button {
            +"Admin Actions"
            attrs {
                onClickFunction = {
                    toAdminMenu()
                }
            }
        }
    }
}


fun RBuilder.login(placeholderName: String? = null) = child(Login::class) {
    attrs.name = placeholderName ?: defaultPlaceholder
}

fun register(name: String) {
    socketClient.run {
        val message = if (appState.userInfo != dummyUser) {
            EnsureUserRegistration(appState.userInfo.userId, name)
        } else {
            UserRegistrationRequest(name)
        }
        sendMessage(message.serialize())
        println("Found $name!")
    }
}

fun toLoginState() {
    appState = appState.changeUserInfo(dummyUser).changeState(ApplicationState.LOGIN).changeLobby(null).changeLocations(null)
}