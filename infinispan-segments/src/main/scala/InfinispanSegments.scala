import org.infinispan.manager.DefaultCacheManager

object InfinispanSegments {
  def main(args: Array[String]): Unit = {
    val manager = new DefaultCacheManager("infinispan.xml")
    val cache = manager.getCache[String, String]("cacheWithSegments")

    try {
      val range = 1 to 60

      range.foreach(i => cache.put(s"key$i", s"value$i"))

      val dm = cache.getAdvancedCache.getDistributionManager
      range.foreach(i =>
        println(s"locate key[key$i] => ${dm.locate(s"key$i")}"))
    } finally {
      manager.stop()
    }
  }
}
