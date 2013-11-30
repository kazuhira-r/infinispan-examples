import org.infinispan.manager.DefaultCacheManager

object EmbeddedCacheServer extends SimpleClassNameLogSupport {
  def main(args: Array[String]): Unit = {
    System.setProperty("nodeId", args(0))

    val manager = new DefaultCacheManager("infinispan.xml")
    val cache = manager.getCache[Any, Any]("listenersCache")

    manager.addListener(new CacheManagerLevelListener)
    cache.addListener(new CacheLevelListener)

    val keysValues = (10 to 15) map (i => (s"key$i", s"value$i"))
    log(s"エントリを ${keysValues.size} 個登録します")
    keysValues.foreach { case (k, v) => cache.put(k, v) }
  }
}
