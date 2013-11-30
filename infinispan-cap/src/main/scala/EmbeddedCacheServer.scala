import org.infinispan.manager.DefaultCacheManager

object EmbeddedCacheServer {
  def _main(args: Array[String]): Unit =
    new DefaultCacheManager("infinispan.xml").getCache[Any, Any]()
}
