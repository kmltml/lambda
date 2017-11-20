package lambda

object BuiltinDefs {

  val defs = Map[String, Expr] (
    "+" -> Expr.BuiltinFun {
      case Expr.Num(a) => Expr.BuiltinFun {
        case Expr.Num(b) => Expr.Num(a + b)
      }
    }
  )

}
