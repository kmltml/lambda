package lambda

import fastparse.WhitespaceApi
import fastparse.noApi._

object Parser {

  abstract class Context(whiteChars: String) {

    val White = WhitespaceApi.Wrapper {
      import fastparse.all._
      NoTrace(CharsWhileIn(whiteChars, 0))
    }
    import White._

    val SymbolChars = "-+*=&^%$#@!~/?><"
    val IdStart = CharPred(c => c.isLetter || SymbolChars.contains(c))
    val IdRest = CharsWhile(c => c.isLetterOrDigit || SymbolChars.contains(c), min = 0)
    val Id = (IdStart ~~ IdRest ~~ "'".rep(min = 0)).!

    val Term: P[Expr] = P(App)
    val Term0: P[Expr] = P(Abs | Var | Par | Num)

    val Var: P[Expr] = P(Id.map(Expr.Var))


    val Lambda = CharIn("\\λ")
    val Dot = "." | "->"
    val Abs: P[Expr] = P((Lambda ~/ Id.rep(min = 1) ~ Dot ~ Term)
      .map {
        case (ids, t) => ids.foldRight(t)(Expr.Abs)
      })

    val App: P[Expr] = P(Term0.rep(min = 1)
      .map(ts => ts.reduceLeft(Expr.App)))

    val Par: P[Expr] = P("(" ~/ ExprContext.Term ~ ")")

    val Num: P[Expr] = P(CharsWhileIn("0123456789").!.map(s => Expr.Num(s.toInt)))

  }

  object ExprContext extends Context(" \r\n\t")
  object DefContext extends Context(" \t") {
    import White._

    val Newline = StringIn("\r\n", "\n")

    val Bind: P[Def] = P((Id ~ "=" ~/ DefContext.Term).map(Def.tupled))

    val Binds: P[Seq[Def]] = P(Bind.rep(sep = Newline.rep(min = 1)) ~ Newline.rep(min = 0) ~ End)

    val ReplLine: P[Either[Def, Expr]] = P(Bind.map(Left(_)) | Term.map(Right(_)) ~ End)
  }
  
}
