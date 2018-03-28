package pw.jonak.spyfall.frontend.elements

import kotlinx.html.role
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.span

interface AlertProps : RProps {
    var text: String
    //var timeout: Int
    var hidden: Boolean
}

class Alert(props: AlertProps) : RComponent<AlertProps, RState>(props) {

//    override fun RState.init(props: AlertProps) {
//        window.setTimeout({
//            unmountComponentAtNode(findDOMNode(this@Alert).parentElement)
//        }, props.timeout)
//    }

    override fun RBuilder.render() {
        span(classes = if (props.hidden) "accessibilityonly" else "") {
            +props.text
            attrs {
                role = "alert"
            }
            attrs["aria-atomic"] = "true"
            attrs["aria-live"] = "assertive"
        }
    }
}

fun RBuilder.alert(text: String, hidden: Boolean = true) = child(Alert::class) {
    attrs.text = text
    //attrs.timeout = timeout
    attrs.hidden = hidden
}