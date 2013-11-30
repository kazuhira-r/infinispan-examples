import org.infinispan.client.hotrod.RemoteCacheManager
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder

object InfinispanRemote {
  def main(args: Array[String]): Unit = {
    val manager =
      new RemoteCacheManager(
        new ConfigurationBuilder()
          .addServers("localhost:11222")
          .build)
    val cache = manager.getCache[String, String]("namedCache")

    try {
      val range = 1 to 5

      if (cache.size > 0) {
        for (i <- range) {
          require(cache.get(s"key$i") == s"value$i")
        }
      }

      for (i <- range) {
        cache.put(s"key$i", s"value$i")
      }

      for (i <- range) {
        require(cache.get(s"key$i") == s"value$i")
      }
    } finally {
      manager.stop()
    }
  }
}
