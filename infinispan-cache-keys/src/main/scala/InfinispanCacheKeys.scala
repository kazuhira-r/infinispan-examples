import scala.collection.JavaConverters._

import org.infinispan.manager.DefaultCacheManager

object InfinispanCacheKeys {
  def main(args: Array[String]): Unit = {
    val manager = new DefaultCacheManager("infinispan.xml")

    try {
      val cache = manager.getCache[String, String]()

      (1 to 10) foreach { i => cache.put(s"key$i", s"value$i") }

      println(s"Size => ${cache.size}")
      cache
        .keySet
        .asScala
        .toSeq
        .sortWith(_.drop(3).toInt < _.drop(3).toInt)
        .foreach(println)

      val dm = cache.getAdvancedCache.getDistributionManager
      val selfNode = manager.getAddress

      println(s"Self Node = $selfNode")

      (1 to 10) foreach { i =>
        val key = s"key$i"
        val primary = dm.getPrimaryLocation(key)
        val locate = dm.locate(key)

        val notSelf = (primary != selfNode) && !locate.contains(selfNode)
        val prefix = if (notSelf) "***** " else ""

        println(s"${prefix}Key = $key, PL[$primary], Locate$locate")
      }
    } finally {
      manager.stop()
    }
  }
}
