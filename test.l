id = \x.x
s0 = \s z.z

false = \t f.f
true = \t f.t

and = \a b.a b false
or  = \a b.a true b
not = \a t f . a f t

succ = \n s z . s (n s z)
s1 = succ s0
s2 = succ s1
s3 = succ s2
s4 = succ s3
s5 = succ s4
s6 = succ s5
s7 = succ s6
s8 = succ s7

fromChurch = \n . n (+ 1) 0

plus = \a b s z . a s (b s z)
mul = \a b . (a (plus b) s0)
pow = \a b . (b (mul a) s1)

pair = \f s b . b f s
fst = \p.p true
snd = \p.p false
