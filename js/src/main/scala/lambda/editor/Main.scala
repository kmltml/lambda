package lambda
package editor

import cats._
import cats.data._
import cats.implicits._

import com.scalawarrior.scalajs.ace._
import fastparse.core.Parsed
import scalajs.js
import org.scalajs.dom._

object Main {

  val DefsKey = "defs"

  def main(args: Array[String]): Unit = {
    val editor = ace.edit("editor")
    editor.setOptions(js.Dynamic.literal(mode = "ace/mode/lambda"))
    editor.setTheme("ace/theme/tomorrow_night_eighties")
    editor.commands.addCommand(new EditorCommand {
      var name = "lambda"
      var bindKey = "\\"
      var exec: js.Function = (editor: Editor) => {
        editor.insert("λ")
      }
      var readOnly = false
    })
    val storedDefs = Option(window.localStorage.getItem(DefsKey))
    storedDefs.foreach(d => editor.setValue(d, 0))

    def saveDefs(): Unit = 
      window.localStorage.setItem(DefsKey, editor.getValue)

    window.onbeforeunload = e => saveDefs()
    var ctxt = BuiltinDefs.defs
    val repl: Repl = new Repl(document.querySelector("#repl").asInstanceOf[html.Element], "λ> ")
    repl.onLine += { line =>
      val parsed = Parser.DefContext.ReplLine.parse(line)
      parsed match {
        case f: Parsed.Failure[_, _] =>
          repl.writeLn(s"Parsing error: ${f.toString}")
        case Parsed.Success(Left(Def(v, e)), _) =>
          ctxt += (v -> e)
          repl.writeLn(s"Defined $v")
        case Parsed.Success(Right(e), _) =>
          repl.writeLn(show" = ${Evaluator.eval(e, ctxt).value}")
      }
    }
    document.getElementById("editor-load").asInstanceOf[html.Button].onclick = e => {
      val parsed = Parser.DefContext.Binds.parse(editor.getValue)
      parsed match {
        case f: Parsed.Failure[_, _] =>
          repl.writeLn(s"Parsing error: ${f.toString}")
        case Parsed.Success(defs, _) =>
          ctxt ++= defs.map(d => d.name -> d.body).toMap
          repl.writeLn(s"Defined: ${defs.map(_.name).mkString(", ")}")
      }
      saveDefs()
    }
  }

}
