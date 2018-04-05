package pw.jonak.spyfall.frontend.elements

import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.span

/** A bullet that's nice for users using accessible technologies. */
class AccessibleBullet : RComponent<RProps, RState>() {
    override fun RBuilder.render() {
        span(classes = "accessibilityonly") {
            +"•"
        }
    }
}

/** Allows [AccessibleBullet] to be used from [RBuilder] contexts */
fun RBuilder.accessibleBullet() = child(AccessibleBullet::class) {}