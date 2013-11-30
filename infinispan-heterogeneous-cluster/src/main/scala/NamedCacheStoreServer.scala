import org.infinispan.manager.DefaultCacheManager

object NamedCacheStoreServer {
  def main(args: Array[String]): Unit = {
    System.setProperty("capacity.factor", "1.0")

    val manager = new DefaultCacheManager("infinispan.xml")

    try {
      val cache = manager.getCache[String, String]("heterogeneous-cache")

      readLine("NamedCacheStoreManager Startup.")

      cache.stop()
    } finally {
      manager.stop()
    }
  }
}
