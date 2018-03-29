package pw.jonak.spyfall.frontend.elements

import kotlinx.html.LI
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.RDOMBuilder
import react.dom.li
import kotlin.js.json

interface ListEntryState : RState {
    var checked: Boolean
}

interface ListEntryProps : RProps {
    var inner: RDOMBuilder<LI>.() -> Unit
    var extraClasses: Set<String>
}

class ListEntry(props: ListEntryProps) : RComponent<ListEntryProps, ListEntryState>(props) {
    override fun ListEntryState.init(props: ListEntryProps) {
        checked = false
    }

    override fun RBuilder.render() {
        li(classes = "${props.extraClasses.joinToString(" ")} ${if(state.checked) "crossedoff" else ""}") {
            props.inner(this)
            attrs["style"] = json("cursor" to "pointer", "user-select" to "none")
            attrs {
                onClickFunction = {
                    state.checked = !state.checked
                }
            }
        }
    }
}

fun RBuilder.listEntry(classes: Set<String> = setOf(), inner: RDOMBuilder<LI>.() -> Unit) = child(ListEntry::class) {
    attrs.inner = inner
    attrs.extraClasses = classes
}