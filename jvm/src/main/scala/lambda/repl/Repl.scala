package lambda
package repl

import fastparse.core.Parsed
import org.jline.reader.{ EndOfFileException, LineReader, LineReaderBuilder, MaskingCallback, UserInterruptException }
import org.jline.terminal.TerminalBuilder
import scala.io.Source

import cats._
import cats.data._
import cats.implicits._

object Repl {

  val commands = Set("exit", "quit", "load", "ctxt")
  val Command = """:(\w+)(?: (.+))?""".r

  def main(args: Array[String]): Unit = {
    val terminal = TerminalBuilder.terminal()
    val reader = LineReaderBuilder.builder()
      .terminal(terminal).build()
    reader.setOpt(LineReader.Option.DISABLE_EVENT_EXPANSION)

    var ctxt = Map[String, Expr] (
      "+" -> Expr.BuiltinFun {
        case Expr.Num(a) => Expr.BuiltinFun {
          case Expr.Num(b) => Expr.Num(a + b)
        }
      }
    )

    while(true) {
      val line = try {
        reader.readLine("\\> ", null, null: MaskingCallback, null)
      } catch {
        case _: UserInterruptException | _: EndOfFileException => return
      }
      terminal.writer.println()
      line match {
        case Command(cmd, rest) =>
          val candidates = commands.filter(_.startsWith(cmd))
          candidates.size match {
            case 0 => terminal.writer.println("Unknown command")
            case 1 => candidates.head match {
              case "exit" | "quit" =>
                terminal.writer.println("Bye!")
                terminal.writer.flush()
                return
              case "load" =>
                val src = Source.fromFile(rest)
                val text = src.mkString
                src.close()
                val parsed = Parser.DefContext.Binds.parse(text).get.value
                terminal.writer.println(s"Defined: ${parsed.map(_.name).mkString(", ")}")
                ctxt = ctxt ++ parsed.map(d => d.name -> d.body).toMap
              case "ctxt" =>
                ctxt.foreach {
                  case (k, v) => terminal.writer.println(show"$k = $v")
                }
            }
            case _ =>
              terminal.writer.println("Ambigous command, did you mean one of these?")
              terminal.writer.println(candidates mkString ", ")
          }
        case _ =>
          val parsed = Parser.DefContext.ReplLine.parse(line)
          parsed match {
            case Parsed.Success(Left(d), _) =>
              terminal.writer.println(s"Defined ${d.name}")
              ctxt += (d.name -> d.body)
            case Parsed.Success(Right(e), _) =>
              terminal.writer.println(show"= ${Evaluator.eval(e, ctxt).value}")
            case f: Parsed.Failure[_, _] =>
              terminal.writer.println(s"Parsing error: $f")
          }
      }      
    }
  }

}
