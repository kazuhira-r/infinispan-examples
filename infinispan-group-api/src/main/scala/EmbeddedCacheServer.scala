import org.infinispan.manager.DefaultCacheManager

object EmbeddedCacheServer {
  def main(args: Array[String]): Unit = {
    val manager = new DefaultCacheManager("infinispan.xml")
    val cacheAsCustomizedKeyClass = manager.getCache[Any, Any]("cacheAsCustomizedKeyClass")
    val cacheAsGrouper = manager.getCache[NoGroupKeyClass, NoGroupKeyClass]("cacheAsGrouper")
    val cacheAsGrouperSimple = manager.getCache[String, String]("cacheAsGrouperSimple")
  }
}
