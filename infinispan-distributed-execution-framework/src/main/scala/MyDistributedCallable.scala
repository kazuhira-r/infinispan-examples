import scala.collection.JavaConverters._

import org.infinispan.Cache
import org.infinispan.distexec.DistributedCallable

@SerialVersionUID(1L)
class MyDistributedCallable extends DistributedCallable[String, Integer, Integer] with Serializable {
  var inputKeys: Set[String] = _
  var cache: Cache[String, Integer] = _

  println("create distributedcallable instance")

  def setEnvironment(cache: Cache[String, Integer], inputKeys: java.util.Set[String]): Unit = {
    println(s"Input Keys => Size:${inputKeys.size}, Values:${inputKeys}")
    this.inputKeys = Set.empty ++ inputKeys.asScala
    this.cache = cache
  }

  def call(): Integer = {
    println("called")
    inputKeys.foldLeft(0) { (acc, key) =>
      cache.get(key) match {
        case null => acc
        case v => acc + v
      }
    }
  }
}
