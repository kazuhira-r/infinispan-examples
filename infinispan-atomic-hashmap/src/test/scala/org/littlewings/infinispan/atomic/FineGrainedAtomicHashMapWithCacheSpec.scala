package org.littlewings.infinispan.atomic

import org.infinispan.Cache
import org.infinispan.atomic.{AtomicHashMap, AtomicMapLookup, FineGrainedAtomicHashMapProxy}
import org.infinispan.manager.DefaultCacheManager

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class FineGrainedAtomicHashMapWithCacheSpec extends FunSpec {
  describe("FineGrainedAtomicHashMap Spec") {
    it("no transaction cache NG") {
      withCache() { cache =>
        a [IllegalStateException] should be thrownBy AtomicMapLookup.getFineGrainedAtomicMap[String, String, String](cache, "fine-grained-atomic-map")
      }
    }

    it("repeatable-read cache") {
      withCache("repeatableReadCache") { cache =>
        val tm = cache.getAdvancedCache.getTransactionManager
        tm.begin()

        val map = AtomicMapLookup.getFineGrainedAtomicMap[String, String, String](cache, "fine-grained-atomic-map")

        map.put("key1", "value1")
        map.put("key2", "value2")

        tm.commit()

        // Cacheに入っているのは、AtomicHashMap
        cache.get("fine-grained-atomic-map").getClass should be (classOf[AtomicHashMap[_, _]])
      }
    }
  }

  def withCache[T](cacheName: String = "")(fun: Cache[String, String] => T): T = {
    val manager = new DefaultCacheManager("infinispan.xml")

    try {
      val cache =
        if (cacheName.isEmpty) manager.getCache[String, String]
        else manager.getCache[String, String](cacheName)

      try {
        fun(cache)
      } finally {
        cache.stop()
      }
    } finally {
      manager.stop()
    }
  }
}
