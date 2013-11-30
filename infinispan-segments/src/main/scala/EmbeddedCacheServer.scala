import scala.collection.JavaConverters._

import org.infinispan.manager.DefaultCacheManager
import org.infinispan.notifications.Listener
import org.infinispan.notifications.cachelistener.annotation._
import org.infinispan.notifications.cachelistener.event._

object EmbeddedCacheServer {
  def main(args: Array[String]): Unit = {
    val manager = new DefaultCacheManager("infinispan.xml")
    val cache = manager.getCache[String, String]("cacheWithSegments")
    cache.addListener(new CacheListener)
  }
}

@Listener
class CacheListener {
  @TopologyChanged
  def topologyChanged(event: TopologyChangedEvent[_, _]): Unit =
    if (!event.isPre) {
      val cache = event.getCache
      val keys = cache.keySet.asScala
      val dm = cache.getAdvancedCache.getDistributionManager

      keys.foreach(k => println(s"key[$k] locate => ${dm.locate(k)}"))
    }
}
