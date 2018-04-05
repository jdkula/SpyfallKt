package pw.jonak.spyfall.frontend.elements

import kotlinx.html.SPAN
import pw.jonak.spyfall.frontend.LocalizationInformation.getLocalization
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.RDOMBuilder
import react.dom.div
import react.dom.span

/**
 * Properties for the [IntroHeader]
 * @property before What should be above the header
 * @property after What should be below the header.
 */
interface IntroProps : RProps {
    var before: RDOMBuilder<SPAN>.() -> Unit
    var after: RDOMBuilder<SPAN>.() -> Unit
}

/** The application's welcome header. */
class IntroHeader(props: IntroProps) : RComponent<IntroProps, RState>(props) {
    override fun RBuilder.render() {
        div(classes = "row") {
            span(classes = "col s12 center-align") {
                props.before(this)
            }
            span(classes = "accessibilityonly") { +" " }
            span(classes = "col maxwidth s12 intro-header center-align") {
                +getLocalization("ui", "welcome to spyfall")
            }
            span(classes = "col s12 center-align") {
                props.after(this)
            }
        }
    }
}

/** Allows [IntroHeader] to be used from [RBuilder] contexts */
fun RBuilder.introHeader(before: RDOMBuilder<SPAN>.() -> Unit, after: RDOMBuilder<SPAN>.() -> Unit) = child(IntroHeader::class) {
    attrs.before = before
    attrs.after = after
}