import scala.collection.JavaConverters._

import java.util.Set

import org.infinispan.Cache
import org.infinispan.manager.DefaultCacheManager
import org.infinispan.distexec.DefaultExecutorService

object DistributedExecutionFrameworkExample {
  def main(args: Array[String]): Unit = {
    val manager = new DefaultCacheManager("infinispan.xml")
    val cache = manager.getCache[String, Integer]()
    cache.addListener(new LoggingListener)

    val limit = 50
    val keys = new Array[String](limit)
    var total = 0
    for (i <- 1 to limit) {
      val key =  "key" + i
      keys(i - 1) = key
      cache.put(key, i)
      total += i
    }

    val des = new DefaultExecutorService(cache)
    try {
      /*
      val future = des.submit(new MyCallable)
      println(s"ExecutorService#submit Result => ${future.get}")
      */
      /*
      val futures = des.submitEverywhere(new MyCallable)
      futures.asScala.foreach(f => println(f.get))
      */

      /*
      val future = des.submit(new MyDistributedCallable, keys: _*)
      println(s"ExecutorService#submit Result => ${future.get}, Local Sum = ${total}")
      */

      val futures = des.submitEverywhere(new MyDistributedCallable, keys: _*)
      val callableTotal = futures.asScala.foldLeft(0) { (acc, f) => acc + f.get }
      println(s"ExecutorService#submitEverywhere Result => ${callableTotal}, Local Sum = ${total}")
    } finally {
      des.shutdown()

      cache.stop()
      manager.stop()
    }
  }
}
