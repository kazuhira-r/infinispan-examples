import org.infinispan.manager.DefaultCacheManager

object EmbeddedCacheServer {
  def main(args: Array[String]): Unit = {
    val manager = new DefaultCacheManager("infinispan.xml")

    try {
      val cache = manager.getCache[String, String]()

      readLine("Server Startup.")

    } finally {
      manager.stop()
    }
  }
}
