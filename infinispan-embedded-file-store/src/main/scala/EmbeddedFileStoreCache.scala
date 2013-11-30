import org.infinispan.configuration.cache.ConfigurationBuilder
import org.infinispan.eviction.EvictionStrategy
import org.infinispan.manager.DefaultCacheManager

object EmbeddedFileStoreCache {
  def main(args: Array[String]): Unit = {
    val manager = new DefaultCacheManager
    manager.defineConfiguration(
      "fileStoreCache",
      new ConfigurationBuilder()
        .eviction
        .strategy(EvictionStrategy.LIRS)
        .maxEntries(5)
        .expiration
      // .lifespan(2, java.util.concurrent.TimeUnit.SECONDS)
        .loaders
        .passivation(true)
        .addFileCacheStore
        .ignoreModifications(false)
        .fetchPersistentState(false)
      // .purgeOnStartup(true)  // => これを付けると、次回起動時に空になる
        .location("cache-store")
        .build
    )

    val cache = manager.getCache[String, String]("fileStoreCache")

    val range = 1 to 10
    range foreach { v =>
      val key = "key" + v
      println(s"$key => ${cache.get(key)}")
    }

    Thread.sleep(3000L)

    range foreach { v =>
      val key = "key" + v
      cache.put(key, new java.util.Date().toString)
    }

    range foreach { v =>
      val key = "key" + v
      println(s"$key => ${cache.get(key)}")
    }
  }
}
