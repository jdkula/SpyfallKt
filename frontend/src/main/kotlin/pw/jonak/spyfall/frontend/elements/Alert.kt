package pw.jonak.spyfall.frontend.elements

import kotlinx.html.role
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.span

/**
 * Properties for [Alert]
 * @property text The text to alert
 * @property accessibilityOnly true if the alert should only be shown to accessible technology.
 */
interface AlertProps : RProps {
    var text: String
    var accessibilityOnly: Boolean
}

/**
 * Displays an alert that interrupts screen readers.
 */
class Alert(props: AlertProps) : RComponent<AlertProps, RState>(props) {
    override fun RBuilder.render() {
        span(classes = if (props.accessibilityOnly) "accessibilityonly" else "") {
            +props.text
            attrs {
                role = "alert"
            }
            attrs["aria-atomic"] = "true"
            attrs["aria-live"] = "assertive"
        }
    }
}

/** Allows [Alert] to be used from [RBuilder] contexts */
fun RBuilder.alert(text: String, hidden: Boolean = true) = child(Alert::class) {
    attrs.text = text
    attrs.accessibilityOnly = hidden
}