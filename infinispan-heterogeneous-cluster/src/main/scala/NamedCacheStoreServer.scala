import org.infinispan.manager.DefaultCacheManager

import scala.io.StdIn

object NamedCacheStoreServer {
  def main(args: Array[String]): Unit = {
    System.setProperty("capacity.factor", "1.0")

    val manager = new DefaultCacheManager("infinispan.xml")

    try {
      val cache = manager.getCache[String, String]("heterogeneous-cache")

      StdIn.readLine("NamedCacheStoreManager Startup.")

      cache.stop()
    } finally {
      manager.stop()
    }
  }
}
