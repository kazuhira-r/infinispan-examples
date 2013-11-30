import org.infinispan.manager.DefaultCacheManager

object EmbeddedCacheServer {
  def main(args: Array[String]): Unit = {
    val manager = new DefaultCacheManager("infinispan.xml")
    val cache = manager.getCache[AnyRef, AnyRef]
  }
}
