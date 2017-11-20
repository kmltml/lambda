package lambda
package editor

class Event[A] {

  private var listeners: Vector[A => Unit] = Vector.empty

  def apply(a: A): Unit = listeners.foreach(_(a))

  def +=(f: A => Unit): Unit = listeners :+= f

}
