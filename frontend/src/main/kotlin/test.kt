import org.w3c.dom.HTMLDivElement
import kotlin.browser.document

fun main(args: Array<String>) {
    println("hi")
    val div = document.getElementById("app") as HTMLDivElement
}