import java.util.concurrent.Callable

@SerialVersionUID(1L)
class MyCallable extends Callable[Integer] with Serializable {
  println("create mycallable instance")

  def call(): Integer = {
    println("called")
    10
  }
}
