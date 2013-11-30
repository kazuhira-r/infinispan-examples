import org.infinispan.CacheImpl
import org.infinispan.manager.DefaultCacheManager

object InfinispanPrintConfigurationScala {
  def _main(args: Array[String]): Unit = {
    val manager = new DefaultCacheManager("infinispan.xml")
    val defaultCache = manager.getCache[Any, Any]
    val defaultNamedCache = manager.getCache[Any, Any]("defaultNamedCache")
    val namedCache = manager.getCache[Any, Any]("namedCache")

    val caches = List(defaultCache, defaultNamedCache, namedCache)

    println(defaultCache.getCacheConfiguration)
    println(defaultCache.asInstanceOf[CacheImpl[_, _]].getConfigurationAsXmlString)

    caches.foreach(_.stop())
    manager.stop()
  }
}
