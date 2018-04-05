package pw.jonak.spyfall.frontend.elements

import kotlinx.html.LI
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.RDOMBuilder
import react.dom.li

/**
 * The state of [ListEntry]
 * @property checked true if this [ListEntry] should be crossed out.
 */
interface ListEntryState : RState {
    var checked: Boolean
}

/**
 * The properties of [ListEntry]
 * @property inner What should be inside this [ListEntry]
 * @property extraClasses Other properties this [LI] should have.
 */
interface ListEntryProps : RProps {
    var inner: RDOMBuilder<LI>.() -> Unit
    var extraClasses: String
}

/**
 * Simply an [LI] component that can be crossed off when clicked/tapped.
 */
class ListEntry(props: ListEntryProps) : RComponent<ListEntryProps, ListEntryState>(props) {
    override fun ListEntryState.init(props: ListEntryProps) {
        checked = false
    }

    override fun RBuilder.render() {
            li(classes = "${if (state.checked) "crossedoff" else ""} listentry ${props.extraClasses}") {
                props.inner(this)
                attrs {
                    onClickFunction = {
                        it.preventDefault()
                        it.stopPropagation()
                        state.checked = !state.checked
                    }
                }
            }
    }
}

/** Allows [ListEntry] to be used in [RBuilder] contexts. */
fun RBuilder.listEntry(classes: String = "", inner: RDOMBuilder<LI>.() -> Unit) = child(ListEntry::class) {
    attrs.inner = inner
    attrs.extraClasses = classes
}