package pw.jonak.spyfall.frontend.elements

import kotlinx.html.LI
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.RDOMBuilder
import react.dom.li

interface ListEntryState : RState {
    var checked: Boolean
}

interface ListEntryProps : RProps {
    var inner: RDOMBuilder<LI>.() -> Unit
    var extraClasses: String
}

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

fun RBuilder.listEntry(classes: String = "", inner: RDOMBuilder<LI>.() -> Unit) = child(ListEntry::class) {
    attrs.inner = inner
    attrs.extraClasses = classes
}