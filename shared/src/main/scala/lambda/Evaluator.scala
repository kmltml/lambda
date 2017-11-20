package lambda

import cats._
import cats.data._
import cats.implicits._

object Evaluator {

  import Expr._

  def eval(term: Expr, ctxt: Map[String, Expr]): Eval[Expr] = term match {
    case v: Var => ctxt.get(v.name).map(eval(_, ctxt)).getOrElse(Eval.now(v))
    case App(Abs(v, b), a) => eval(b.subst(v, a), ctxt)
    case app @ App(BuiltinFun(apply), a) =>
      for {
        a_ <- Eval.defer(eval(a, ctxt))
        v <- apply.lift(a_)
          .map(eval(_, ctxt))
          .getOrElse(Eval.now(app))
      } yield v
    case App(f, a) =>
      for {
        f_ <- Eval.defer(eval(f, ctxt))
        a_ <- Eval.defer(eval(a, ctxt))
        v <- if(f != f_ || a != a_) eval(App(f_, a_), ctxt) else Eval.now(App(f, a))
      } yield v
    case Abs(v, b) =>
      for {
        b_ <- Eval.defer(eval(b, ctxt - v))
        v <- if(b_ != b) eval(Abs(v, b_), ctxt) else Eval.now(Abs(v, b))
      } yield v
    case t => Eval.now(t)
  }

}
