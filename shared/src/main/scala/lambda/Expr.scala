package lambda

import cats._
import cats.data._
import cats.implicits._
import scala.annotation.tailrec

sealed trait Expr {

  import Expr._

  def free: Set[String] = this match {
    case Var(n) => Set(n)
    case Abs(v, b) => b.free - v
    case App(a, b) => a.free ++ b.free
    case Num(_) | BuiltinFun(_) => Set.empty
  }

  def subst(from: String, to: Expr): Expr = {
    def go(expr: Expr): Eval[Expr] = expr match {
      case Var(`from`) => Eval.now(to)
      case Var(v) => Eval.now(expr)
      case App(a, b) =>
        for {
          a_ <- Eval.defer(go(a))
          b_ <- Eval.defer(go(b))
        } yield App(a_, b_)
      case Abs(`from`, _) => Eval.now(expr)
      case a: Abs if to.free(a.name) =>
        val renamed = a.renameFresh
        go(renamed.body)
          .map(b => renamed.copy(body = b))
      case Abs(v, b) => go(b).map(Abs(v, _))
      case Num(_) | BuiltinFun(_) => Eval.now(expr)
    }
    go(this).value
  }

}

object Expr {

  implicit val show: Show[Expr] = prettyprint(_, false)

  def prettyprint(e: Expr, lhs: Boolean): String = e match {
    case Abs(v, b) =>
      @tailrec
      def go(e: Expr, acc: List[String]): (Expr, List[String]) = e match {
        case Abs(v, b) => go(b, v :: acc)
        case _ => (e, acc.reverse)
      }
      val (body, vars) = go(b, Nil)
      val s = show"\\${(v::vars).mkString(" ")}.$body"
      if(lhs) show"($s)" else s
    case App(a, b) =>
      val l = prettyprint(a, true)
      val r = b match {
        case _: App => show"($b)"
        case _ => prettyprint(b, lhs)
      }
      s"$l $r"
    case Var(v) => v
    case Num(n) => n.toString
    case BuiltinFun(_) => "[builtin]"
  }

  final case class Var(name: String) extends Expr
  final case class Abs(name: String, body: Expr) extends Expr {
    def renameFresh: Abs = {
      def namesToTry: Stream[String] = {
        val baseName = name.replaceAll("\\d*'*$", "")
        (baseName + "'") #:: (baseName + "''") #:: Stream.iterate(0)(_ + 1).map(baseName + _)
      }
      val bodyFree = body.free
      val newName = namesToTry.dropWhile(bodyFree).head
      Console.err.println(show"Renaming $name to $newName in $this")
      Abs(newName, body.subst(name, Var(newName)))
    }
  }
  final case class App(fun: Expr, param: Expr) extends Expr

  final case class Num(i: Int) extends Expr
  final case class BuiltinFun(apply: PartialFunction[Expr, Expr]) extends Expr

}
