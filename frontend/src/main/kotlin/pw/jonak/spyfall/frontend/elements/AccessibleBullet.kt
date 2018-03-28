package pw.jonak.spyfall.frontend.elements

import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.span

class AccessibleBullet : RComponent<RProps, RState>() {
    override fun RBuilder.render() {
        span(classes = "accessibilityonly") {
            +"•"
        }
    }
}

fun RBuilder.accessibleBullet() = child(AccessibleBullet::class) {}