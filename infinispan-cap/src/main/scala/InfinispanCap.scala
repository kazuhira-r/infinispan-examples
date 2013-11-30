import scala.collection.mutable.ListBuffer

import java.util.Date

import org.infinispan.manager.DefaultCacheManager
import org.infinispan.client.hotrod.RemoteCacheManager

object InfinispanCap {
  def main(args: Array[String]): Unit = {
    val manager = new RemoteCacheManager("localhost")
    val cache = manager.getCache[String, String]()

    try {
      val keys = (1 to 1000) map (i => s"key${i}")
      val data =
            (0 to 10)
              .foldLeft(new StringBuilder) { (b, i) =>
              b ++= i.toString
            }.toString

      val log = (msg: String) => println(s"[${new Date}] $msg")

      log("Current Cache Values")
      var start = System.currentTimeMillis()
      log("Missing Keys Count => " + keys.filter(key => !cache.containsKey(key)).size)
      log(s"elapsed time: ${System.currentTimeMillis() - start}")

      log("Created Data Size[%1$,3d]".format(data.getBytes("UTF-8").size))

      log("Put Cache Entries Start")
      start = System.currentTimeMillis()
      for (key <- keys) cache.put(key, data)
      log(s"elapsed time: ${System.currentTimeMillis() - start}")

      start = System.currentTimeMillis()
      log("Current Cache Pushed Values")
      for (key <- keys) cache.get(key)
      log(s"elapsed time: ${System.currentTimeMillis() - start}")

      log("Program End")
    } finally {
      cache.stop()
      manager.stop()
    }
  }
}

/*
class Listener[T](val name: String) extends FutureListener[T] {
  def futureDone(future: Future[T]): Unit =
    try {
      future.get
      println(s"Future Done[$name]")
    } catch {
      case NonFatal(e) => e.printStackTrace()
    }
}
*/
