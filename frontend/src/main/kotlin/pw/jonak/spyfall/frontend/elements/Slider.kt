package pw.jonak.spyfall.frontend.elements

import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import react.dom.style

interface SliderProps : RProps {
    var color: String
}

class Slider : RComponent<SliderProps, RState>() {
    override fun RBuilder.render() {
        div(classes = "sslider") {
            attrs["aria-label"] = "Server Connection Slider Animation"
            style {
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

fun RBuilder.slider(color: String = "#4a8df8") = child(Slider::class) {
    attrs.color = color
}