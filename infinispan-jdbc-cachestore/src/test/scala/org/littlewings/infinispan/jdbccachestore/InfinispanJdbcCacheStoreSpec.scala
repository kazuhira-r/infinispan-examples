package org.littlewings.infinispan.jdbccachestore

import scala.collection.JavaConverters._

import org.infinispan.Cache
import org.infinispan.manager.DefaultCacheManager

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class InfinispanJdbcCacheStoreSpec extends FunSpec {
  describe("jdbc-cache-store stringKeyedJdbcStore Spec") {
    it("save data, with string-key") {
      val clusterSize = 3
      val keysValues = (1 to 5).map(i => (s"key$i", new ValueClass(s"value$i")))

      withCache[String, ValueClass](clusterSize, "jdbcStringBasedStoreCache") { cache =>
        keysValues.foreach { case (k, v) => cache.put(k, v) }
      }

      withCache[String, ValueClass](clusterSize, "jdbcStringBasedStoreCache") { cache =>
        keysValues.foreach { case (k, v) =>
          cache.get(k) should be (new ValueClass(v.value))
        }
      }

      val keysValues2 = (1 to 3).map(i => (s"key$i", new ValueClass(s"value${i}-v2")))
      withCache[String, ValueClass](clusterSize, "jdbcStringBasedStoreCache") { cache =>
        keysValues2.foreach { case (k, v) => cache.put(k, v) }
      }
    }

    it("save data, with no-string-key") {
      val clusterSize = 3
      val keysValues = (10 to 15).map(i => (new KeyClass(s"key$i"), new ValueClass(s"value$i")))

      withCache[KeyClass, ValueClass](clusterSize, "jdbcStringBasedStoreCacheNoStringKey") { cache =>
        keysValues.foreach { case (k, v) => cache.put(k, v) }
      }

      withCache[KeyClass, ValueClass](clusterSize, "jdbcStringBasedStoreCacheNoStringKey") { cache =>
        keysValues.foreach { case (k, v) =>
          cache.get(k) should be (new ValueClass(v.value))
        }
      }
    }
  }

  describe("jdbc-cache-store binaryJdbcStore Spec") {
    it("save data, with string-key") {
      val clusterSize = 3
      val keysValues = (1 to 5).map(i => (s"key$i", new ValueClass(s"value$i")))

      withCache[String, ValueClass](clusterSize, "jdbcBinaryStoreCache") { cache =>
        keysValues.foreach { case (k, v) => cache.put(k, v) }
      }


      withCache[String, ValueClass](clusterSize, "jdbcBinaryStoreCache") { cache =>
        keysValues.foreach { case (k, v) =>
          cache.get(k) should be (new ValueClass(v.value))
        }
      }
    }

    it("save data, with no-string-key") {
      val clusterSize = 3
      val keysValues = (10 to 15).map(i => (new KeyClass(s"key$i"), new ValueClass(s"value$i")))

      withCache[KeyClass, ValueClass](clusterSize, "jdbcBinaryStoreCacheNoStringKey") { cache =>
        keysValues.foreach { case (k, v) => cache.put(k, v) }
      }

      withCache[KeyClass, ValueClass](clusterSize, "jdbcBinaryStoreCacheNoStringKey") { cache =>
        keysValues.foreach { case (k, v) =>
          cache.get(k) should be (new ValueClass(v.value))
        }
      }
    }
  }

  describe("jdbc-cache-store mixedJdbcStore Spec") {
    it("save data, with string-key") {
      val clusterSize = 3
      val keysValues = (1 to 5).map(i => (s"key$i", new ValueClass(s"value$i")))

      withCache[String, ValueClass](clusterSize, "jdbcMixedStoreCache") { cache =>
        keysValues.foreach { case (k, v) => cache.put(k, v) }
      }

      withCache[String, ValueClass](clusterSize, "jdbcMixedStoreCache") { cache =>
        keysValues.foreach { case (k, v) =>
          cache.get(k) should be (new ValueClass(v.value))
        }
      }
    }

    it("save data, with no-string-key") {
      val clusterSize = 3
      val keysValues = (10 to 15).map(i => (new KeyClass(s"key$i"), new ValueClass(s"value$i")))

      withCache[KeyClass, ValueClass](clusterSize, "jdbcMixedStoreCache") { cache =>
        keysValues.foreach { case (k, v) => cache.put(k, v) }
      }

      withCache[KeyClass, ValueClass](clusterSize, "jdbcMixedStoreCache") { cache =>
        keysValues.foreach { case (k, v) =>
          cache.get(k) should be (new ValueClass(v.value))
        }
      }
    }
  }

  def withCache[A, B](numInstances: Int, cacheName: String)(fun: Cache[A, B] => Unit): Unit = {
    val managers = (1 to numInstances).map(_ => new DefaultCacheManager("infinispan.xml"))

    try {
      val cache = managers.head.getCache[A, B](cacheName)
      fun(cache)
    } finally {
      for {
        manager <- managers
        cacheName <- manager.getCacheNames.asScala
      } {
        manager.getCache[Any, Any](cacheName).stop()
      }

      managers.foreach(_.stop())
    }
  }
}
