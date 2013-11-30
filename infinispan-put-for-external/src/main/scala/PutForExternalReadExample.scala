import java.util.concurrent.TimeUnit

import org.infinispan.configuration.cache.ConfigurationBuilder
import org.infinispan.eviction.EvictionStrategy
import org.infinispan.manager.{DefaultCacheManager, EmbeddedCacheManager}
import org.infinispan.transaction.TransactionMode

object PutForExternalReadExample {
  def main(args: Array[String]): Unit = {
    val manager = new DefaultCacheManager
    try {
      simply(manager)
      withJTA(manager)
    } finally {
      manager.stop
    }
  }

  def simply(manager: EmbeddedCacheManager): Unit = {
    manager.defineConfiguration("simply",
                                new ConfigurationBuilder()
                                  .eviction
                                  .strategy(EvictionStrategy.LIRS)
                                  .maxEntries(10)
                                  .expiration
                                  .lifespan(3, TimeUnit.SECONDS)
                                  .maxIdle(1, TimeUnit.SECONDS)
                                  .build)
    val cache = manager.getCache[String, String]("simply")

    cache.putForExternalRead("key-ext", "value-ext")
    cache.put("key1", "value1")

    println(s"putForExternalRead: key-ext => ${cache.get("key-ext")}")

    cache.putForExternalRead("key-ext", "value updated updated")
    println(s"putForExternalRead Change: key-ext => ${cache.get("key-ext")}")

    cache.put("key-ext", "value updated")
    println(s"put: key-ext => ${cache.get("key-ext")}")

    cache.putForExternalRead("key-ext", "value updated updated")
    println(s"putForExternalRead Change: key-ext => ${cache.get("key-ext")}")

    cache.evict("key-ext")
    println(s"evict: key-ext => ${cache.get("key-ext")}")

    cache.putForExternalRead("key-ext", "value-ext")
    println(s"putForExternalRead: key-ext => ${cache.get("key-ext")}")

    cache.remove("key-ext")
    println(s"remove: key-ext => ${cache.get("key-ext")}")
  }

  def withJTA(manager: EmbeddedCacheManager): Unit = {
    manager.defineConfiguration("jta-cache",
                                new ConfigurationBuilder()
                                  .transaction
                                  .transactionMode(TransactionMode.TRANSACTIONAL)
                                  .build)
    val cache = manager.getCache[String, String]("jta-cache")

    val tm = cache.getAdvancedCache.getTransactionManager

    tm.begin()

    (1 to 3) foreach (i => cache.put(s"key${i}", s"value${i}"))
    cache.putForExternalRead("ext-key", "ext-value")

    tm.rollback()

    (1 to 3) foreach (i => println(s"key${i} => ${cache.get(s"key$i")}")) 
    println(cache.get("ext-key"))
  }
}
