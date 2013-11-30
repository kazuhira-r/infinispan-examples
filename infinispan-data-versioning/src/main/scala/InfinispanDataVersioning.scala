import scala.collection.JavaConverters._

import org.infinispan.manager.DefaultCacheManager
import org.infinispan.interceptors.VersionedEntryWrappingInterceptor

object InfinispanDataVersioning {
  def main(args: Array[String]): Unit = {
    val manager = new DefaultCacheManager("infinispan.xml")
    val cache = manager.getCache[String, String]()
    val advancedCache = cache.getAdvancedCache

    println(cache.getCacheConfiguration)

    for (
      i <- advancedCache.getInterceptorChain.asScala
      if i.isInstanceOf[VersionedEntryWrappingInterceptor]
    ) println(s"Versioned Interceptor => $i")

    try {
      cache.put("key1", "value1")
      println(s"key1 => ${cache.get("key1")}")
      cache.put("key1", "value2")
      println(s"key1 => ${cache.get("key1")}")
      cache.put("key1", "value3")
      println(s"key1 => ${cache.get("key1")}")

      println("-----------------------------------------")

      val cacheEntry = advancedCache.getCacheEntry("key1", null, null)
      println(s"entry => $cacheEntry")
      println(s"entry key: ${cacheEntry.getKey}, entry value: ${cacheEntry.getValue}")
      val versionEntry = cacheEntry.getVersion
      println(versionEntry)
    } finally {
      cache.stop()
      manager.stop()
    }
  }
}
