package pw.jonak.spyfall.frontend.elements

import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import react.dom.style

/**
 * The properties for [Slider]
 * @property color The color of the slider in hex format.
 */
interface SliderProps : RProps {
    var color: String
}

/**
 * A material design slider
 */
class Slider : RComponent<SliderProps, RState>() {
    override fun RBuilder.render() {
        div(classes = "sslider") {
            attrs["aria-label"] = "Server Connection Slider Animation"
            style {  // Required to override the color
                +"""
                    .sline {
                        background: ${props.color} !important;
                    }

                    .ssubline {
                        background: ${props.color} !important;
                    }
                """.trimIndent()
            }
            div(classes = "sline") {}
            div(classes = "ssubline sinc") {}
            div(classes = "ssubline sdec") {}
        }
    }
}

/** Allows the use of [Slider] in [RBuilder] contexts */
fun RBuilder.slider(color: String = "#4a8df8") = child(Slider::class) {
    attrs.color = color
}