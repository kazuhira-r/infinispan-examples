import org.infinispan.manager.DefaultCacheManager

object Invalidator extends SimpleClassNameLogSupport {
  def main(args: Array[String]): Unit = {
    System.setProperty("nodeId", "invalidator")

    val manager = new DefaultCacheManager("infinispan.xml")
    val cache = manager.getCache[String, String]("listenersCache")

    manager.addListener(new CacheManagerLevelListener)
    cache.addListener(new CacheLevelListener)

    val entry = ("key1", "value1-invalidate")
    log(s"エントリ $entry を登録します")
    cache.put(entry._1, entry._2)

    val keysValues = (10 to 15) map (i => (s"key$i", s"value$i"))
    log(s"エントリを ${keysValues.size} 個登録します")
    keysValues.foreach { case (k, v) => cache.put(k, v) }

    cache.stop()
    manager.stop()
  }
}
