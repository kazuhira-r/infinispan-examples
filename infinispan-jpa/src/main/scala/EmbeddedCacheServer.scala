import org.infinispan.manager.DefaultCacheManager

object EmbeddedCacheServer {
  def main(args: Array[String]): Unit = {
    val manager = new DefaultCacheManager("infinispan.xml")
    val cache = manager.getCache[Int, User]("cacheStoreJpa")

    readLine()

    cache.stop()
    manager.stop()
  }
}
