import scala.collection.JavaConverters._

import org.infinispan.Cache
import org.infinispan.manager.DefaultCacheManager

trait CacheSupport {
  def usingCache[A](body: Cache[KeyEntry, ValueEntry] => A): A = {
    val manager = new DefaultCacheManager("infinispan.xml")
    val cache = manager.getCache[KeyEntry, ValueEntry]()

    try {
      body(cache)
    } finally {
      cache.stop()
      manager.stop()
    }
  }
}

object InfinispanStoreAs extends CacheSupport {
  def main(args: Array[String]): Unit = usingCache { cache =>
    // storeAsBinary=StoreAsBinaryConfiguration{enabled=false, storeKeysAsBinary=true, storeValuesAsBinary=true, defensive=false}

    val key = new KeyEntry("1")
    val value = new ValueEntry("firstValue")

    cache.put(key, value)

    println("first => " + cache.get(key))

    cache.getAdvancedCache.getInterceptorChain.asScala.foreach(i => println(i.getClass.getName))
  }
}

object QueryAndUpdate extends CacheSupport {
  def main(args: Array[String]): Unit = usingCache { cache =>
    val key = new KeyEntry("1")
    val value = cache.get(key)

    println("Get Value => " + value)

    value.value = "update"

    println("Local Updated, In Cache => " + cache.get(key))

    println("Reference Equals => " + (cache.get(key) eq cache.get(key)))
  }
}
