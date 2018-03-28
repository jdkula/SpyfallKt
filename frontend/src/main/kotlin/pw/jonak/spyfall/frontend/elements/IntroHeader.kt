package pw.jonak.spyfall.frontend.elements

import kotlinx.html.SPAN
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.RDOMBuilder
import react.dom.div
import react.dom.span
import kotlin.js.json

interface IntroProps : RProps {
    var before: RDOMBuilder<SPAN>.() -> Unit
    var after: RDOMBuilder<SPAN>.() -> Unit
}

class IntroHeader(props: IntroProps) : RComponent<IntroProps, RState>(props) {
    override fun RBuilder.render() {
        div(classes = "row") {
            span(classes = "col s12 center-align") {
                props.before(this)
            }
            span(classes = "accessibilityonly") { +" " }
            span(classes = "col s12 intro-header center-align") {
                attrs["style"] = json("width" to "100%")
                +"Welcome to Spyfall!"
            }
            span(classes = "col s12 center-align") {
                props.after(this)
            }
        }
    }
}

fun RBuilder.introHeader(before: RDOMBuilder<SPAN>.() -> Unit, after: RDOMBuilder<SPAN>.() -> Unit) = child(IntroHeader::class) {
    attrs.before = before
    attrs.after = after
}