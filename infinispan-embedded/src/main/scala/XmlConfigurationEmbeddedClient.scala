import java.util.concurrent.TimeUnit

import org.infinispan.Cache
import org.infinispan.manager.{DefaultCacheManager, EmbeddedCacheManager}

object XmlConfigurationEmbeddedClient {
  def main(args: Array[String]): Unit = {
    val manager: EmbeddedCacheManager = new DefaultCacheManager("infinispan.xml")
    val cache: Cache[String, String] = manager.getCache("xml-configured-cache")

    cache.put("key1", "value1")
    cache.get("key1")
    cache.get("key1")
    cache.put("key2", "value2")
    cache.put("key3", "value3")

    println(s"key1 => ${cache.get("key1")}")  // => key1 => value1
    println(s"key2 => ${cache.get("key2")}")  // => key2 => null
    println(s"key3 => ${cache.get("key3")}")  // => key3 => value3

    println("Sleep...")
    Thread.sleep(3000L)

    println(s"key1 => ${cache.get("key1")}")  // => key1 => null
    println(s"key2 => ${cache.get("key2")}")  // => key2 => null
    println(s"key3 => ${cache.get("key3")}")  // => key3 => null
  }
}
