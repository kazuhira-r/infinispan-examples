import org.infinispan.Cache
import org.infinispan.configuration.cache.ConfigurationBuilder
import org.infinispan.container.entries.CacheEntry
import org.infinispan.eviction.{EvictionStrategy, EvictionThreadPolicy}
import org.infinispan.manager.{DefaultCacheManager, EmbeddedCacheManager}

object InfinispanEvictPassive {
  def main(args: Array[String]): Unit = {
    val manager = new DefaultCacheManager

    val range = (1 to 10)
    val (keys, values) = range.map(i => (s"key$i", s"value$i")).unzip

    println("---------------------------- Passivation -------------------------------------")
    val cachePassivation =
      defineCacheEnablePassivation(manager, "cacheEnablePassivation")
    val advancedCachePassivation = cachePassivation.getAdvancedCache

    keys.foreach { k =>
      printCacheEntryDetail("Cache Passivation Preloaded",
                            advancedCachePassivation.getCacheEntry(k, null, null))
    }

    keys.zip(values).foreach { case (k, v) => cachePassivation.put(k, v) }

    println("------------------------------------------------------------------------------")

    keys.foreach { k =>
      printCacheEntryDetail("Cache Passivation Writed",
                            advancedCachePassivation.getCacheEntry(k, null, null))
    }

    println("---------------------------- Passivation -------------------------------------")

    println("---------------------------- Write-Through -------------------------------------")
    val cacheWriteThrough =
        defineCacheWriteThrough(manager, "cacheWritThrough")
    val advancedCacheWriteThrough = cacheWriteThrough.getAdvancedCache

    keys.foreach { k =>
      printCacheEntryDetail("Cache Write-Through Preloaded",
                            advancedCacheWriteThrough.getCacheEntry(k, null, null))
    }

    keys.zip(values).foreach { case (k, v) => cacheWriteThrough.put(k, v) }

    println("------------------------------------------------------------------------------")

    keys.foreach { k =>
      printCacheEntryDetail("Cache Write-Through Writed",
                            advancedCacheWriteThrough.getCacheEntry(k, null, null))
    }

    println("---------------------------- Write-Through -------------------------------------")

    println("---------------------------- No-Store -------------------------------------")
    val cacheNoStore =
        defineCacheNoStore(manager, "cacheNoStore")
    val advancedCacheNoStore = cacheNoStore.getAdvancedCache

    keys.foreach { k =>
      printCacheEntryDetail("Cache No-Store Preloaded",
                            advancedCacheNoStore.getCacheEntry(k, null, null))
    }

    keys.zip(values).foreach { case (k, v) => cacheNoStore.put(k, v) }

    println("------------------------------------------------------------------------------")

    keys.foreach { k =>
      printCacheEntryDetail("Cache No-Store Writed",
                            advancedCacheNoStore.getCacheEntry(k, null, null))
    }

    println("---------------------------- No-Store -------------------------------------")
  }

  def defineCacheEnablePassivation(manager: EmbeddedCacheManager, name: String): Cache[String, String] = {
    manager.defineConfiguration(
      name,
      new ConfigurationBuilder()
        .eviction
        .strategy(EvictionStrategy.LIRS)
        .threadPolicy(EvictionThreadPolicy.PIGGYBACK)
        .maxEntries(4)
        .loaders
        .passivation(true)
        .preload(true)
        .addFileCacheStore
        .ignoreModifications(false)
        .fetchPersistentState(false)
        // .purgeOnStartup(true)
        .location("cache-store-passivation")
        .build)
    manager.getCache(name)
  }

  def defineCacheWriteThrough(manager: EmbeddedCacheManager, name: String): Cache[String, String] = {
    manager.defineConfiguration(
      name,
      new ConfigurationBuilder()
        .eviction
        .strategy(EvictionStrategy.LIRS)
        .threadPolicy(EvictionThreadPolicy.PIGGYBACK)
        .maxEntries(4)
        .loaders
        //.passivation(false)
        .preload(true)
        .addFileCacheStore
        .ignoreModifications(false)
        .fetchPersistentState(false)
        // .purgeOnStartup(true)
        .location("cache-store-write-through")
        .build)
    manager.getCache(name)
  }

  def defineCacheNoStore(manager: EmbeddedCacheManager, name: String): Cache[String, String] = {
    manager.defineConfiguration(
      name,
      new ConfigurationBuilder()
        .eviction
        .strategy(EvictionStrategy.LIRS)
        .maxEntries(4)
        .build)
    manager.getCache(name)
  }

  def printCacheEntryDetail(preMsg: String, cacheEntry: CacheEntry): Unit =
    cacheEntry match {
      case null =>
      case _ =>
        val msg = s"""$preMsg =>
                      |   Key[${cacheEntry.getKey}],
                      |   Value[${cacheEntry.getValue}]""".stripMargin
        println(msg)
    }
}
