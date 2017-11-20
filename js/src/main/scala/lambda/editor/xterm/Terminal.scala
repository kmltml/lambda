package lambda
package editor
package xterm

import org.scalajs.dom._
import scala.scalajs.js.annotation.JSGlobal
import scalajs._

@js.native
@JSGlobal
class Terminal extends js.Object {

  def open(parent: html.Element, focus: Boolean = true): Unit = js.native
  def write(text: String): Unit = js.native
  def writeln(text: String): Unit = js.native


}
