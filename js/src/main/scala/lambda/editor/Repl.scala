package lambda
package editor

import org.scalajs.dom._
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.raw.{ KeyboardEvent, MouseEvent }
import scalajs.js

class Repl(val parent: html.Element, val prompt: String) {

  val MaxHistoryItems = 100

  var promptLine: html.Span = createPromptLine()
  var history: Vector[String] = Vector.empty
  var historyPointer = -1

  val onLine = new Event[String]

  parent.addEventListener("click", (e: MouseEvent) => promptLine.focus(), true)

  def createPromptLine(): html.Span = {
    val l = document.createElement("div").asInstanceOf[html.Div]
    l.style.display = "flex"
    val p = document.createElement("span").asInstanceOf[html.Span]
    p.innerHTML = prompt.replace(" ", "&nbsp;")
    l.appendChild(p)
    val e = document.createElement("span").asInstanceOf[html.Span]
    e.contentEditable = "true"
    e.style.asInstanceOf[js.Dynamic].flexGrow = "1"
    l.appendChild(e)
    parent.appendChild(l)
    e.onkeydown = e => {
      e.keyCode match {
        case KeyCode.Enter =>
          e.preventDefault()
          promptLine.contentEditable = "false"
          promptLine.onkeydown = null
          val line = promptLine.textContent
          onLine(line)
          history = (line +: history).take(MaxHistoryItems)
          historyPointer = -1
          promptLine = createPromptLine()
          promptLine.focus()
        case KeyCode.Up =>
          e.preventDefault()
          historyPointer += 1
          history.lift(historyPointer) match {
            case Some(l) =>
              promptLine.textContent = l
              // Set selection to end :/
              val range = document.createRange()
              range.selectNodeContents(promptLine)
              range.collapse(false)
              val selection = window.getSelection
              selection.removeAllRanges()
              selection.addRange(range)
              // See? Simple.
            case None =>
              historyPointer -= 1
          }

        case _ =>
      }
    }
    e
  }

  def writeLn(line: String): Unit = {
    val l = document.createElement("div")
    l.innerHTML = line.replace(" ", "&nbsp;")
    parent.appendChild(l)
  }

}
