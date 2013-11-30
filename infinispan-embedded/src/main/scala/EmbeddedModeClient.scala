import java.util.concurrent.TimeUnit

import org.infinispan.Cache
import org.infinispan.manager.{DefaultCacheManager, EmbeddedCacheManager}

object EmbeddedModeClient {
  def main(args: Array[String]): Unit = {
    val manager: EmbeddedCacheManager = new DefaultCacheManager
    val cache: Cache[String, String] = manager.getCache()

    cache.put("key1", "value1")
    cache.put("key2", "value2")

    println(s"key1 => ${cache.get("key1")}")  // => key1 => value1
    println(s"key2 => ${cache.get("key2")}")  // => key2 => value2

    cache.remove("key1")
    cache.remove("key2")

    println(s"key1 => ${cache.get("key1")}")  // => key1 => null
    println(s"key2 => ${cache.get("key2")}")  // => key2 => null

    cache.put("key1", "value1")
    cache.put("key2", "value2", 1, TimeUnit.SECONDS)

    println("Sleep...")
    Thread.sleep(3000L)

    println(s"key1 => ${cache.get("key1")}")  // => key1 => value1
    println(s"key2 => ${cache.get("key2")}")  // => key2 => null

    val namedCache1: Cache[String, String] = manager.getCache("namedCache1")
    println(s"namedCache1: key1 => ${namedCache1.get("key1")}")  // => namedCache1: key1 => null
    println(s"namedCache1: key2 => ${namedCache1.get("key2")}")  // => namedCache1: key2 => null

    import org.infinispan.configuration.cache.ConfigurationBuilder
    import org.infinispan.eviction.EvictionStrategy
    manager.defineConfiguration(
      "namedCache2",
      new ConfigurationBuilder()
        .eviction
        .strategy(EvictionStrategy.LIRS)
        .maxEntries(10)
        .build
    )

    val namedCache2: Cache[String, String] = manager.getCache("namedCache2")
    println(s"namedCache2: key1 => ${namedCache2.get("key1")}")  // => namedCache2: key1 => null
    println(s"namedCache2: key2 => ${namedCache2.get("key2")}")  // => namedCache2: key2 => null
  }
}
