import org.infinispan.manager.DefaultCacheManager

object EmbeddedCacheServer {
  def main(args: Array[String]): Unit = {
    new DefaultCacheManager("infinispan.xml").getCache[KeyEntry, ValueEntry]()
  }
}
