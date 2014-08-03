package org.littlewings.infinispan.persistence

import org.infinispan.Cache
import org.infinispan.configuration.cache.{CacheMode, ConfigurationBuilder}
import org.infinispan.manager.DefaultCacheManager

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class SimpleMapCacheStoreSpec extends FunSpec {
  describe("simple map cache store spec") {
    it("persistence") {
      val numInstances = 2

      SimpleMapCacheStore.instances(numInstances)
      withStoreCache[String, String]("storeCache", numInstances) { cache =>
        println(s"Cache size => ${cache.size}")

        val range = 1 to 10

        range.foreach(i => println(s"key[${s"key$i"}] => ${cache.get(s"key$i")}"))

        (1 to 10) foreach (i => cache.put(s"key$i", s"value$i"))

        /*
        println("Wait...")
        Thread.sleep(5000L)

        range.foreach(i => println(s"key[${s"key$i"}] => ${cache.get(s"key$i")}"))
        */
      }
    }
  }

  def withStoreCache[K, V](cacheName: String, numInstances: Int = 1)(fun: Cache[K, V] => Unit): Unit = {
    val managers =
    (1 to numInstances).map { _ =>
      val manager = new DefaultCacheManager("infinispan.xml")

      val persistenceBuilder = new ConfigurationBuilder()
        .clustering
        .cacheMode(CacheMode.DIST_SYNC)
        .expiration
        // .maxIdle(3000L)
        // .wakeUpInterval(500L)
        .persistence

      manager.defineConfiguration(cacheName,
                                  persistenceBuilder
                                    .addStore(new SimpleMapCacheStoreConfigurationBuilder(persistenceBuilder))
                                    .fetchPersistentState(false)
                                    .preload(false)
                                    .shared(false)
                                    .purgeOnStartup(false)
                                    .ignoreModifications(false)
                                    .build)

      manager
    }

    try {
      managers.foreach(_.getCache[K, V](cacheName))

      fun(managers.head.getCache[K, V](cacheName))
    } finally {
      managers.foreach(_.stop())
    }
  }
}
