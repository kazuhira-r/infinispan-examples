package org.littlewings.infinispan.atomic

import org.infinispan.Cache
import org.infinispan.atomic.{AtomicHashMap, AtomicMapLookup, AtomicHashMapProxy}
import org.infinispan.manager.DefaultCacheManager

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class AtomicHashMapWithCacheSpec extends FunSpec {
  describe("with Infinispan Cache Spec") {
    it("no transaction cache NG") {
      withCache() { cache =>
        // トランザクションの設定がないCacheでは、AtomicMapの取得ができない
        an [IllegalStateException] should be thrownBy AtomicMapLookup.getAtomicMap[String, String, String](cache, "atomic-map")
      }
    }

    it("need transaction") {
      withCache("repeatableReadCache") { cache =>
        // JTAトランザクションが開始済み、またはバッチモードが利用できないと
        // AtomicHapは取得できない
        an [IllegalArgumentException] should be thrownBy AtomicMapLookup.getAtomicMap[String, String, String](cache, "atomic-map")
      }
    }

    it("repeatable-read cache commit") {
      withCache("repeatableReadCache") { cache =>
        val tm = cache.getAdvancedCache.getTransactionManager
        tm.begin()

        val map = AtomicMapLookup.getAtomicMap[String, String, String](cache, "atomic-map")

        map.put("key1", "value1")
        map.put("key2", "value2")

        tm.commit()

        map.get("key1") should be ("value1")
        map.get("key2") should be ("value2")

        map should have size 2

        // map自身は、AtomicHashMapProxyのインスタンス
        map should be (a [AtomicHashMapProxy[_, _]])

        // Cacheは[String, String]だが、「atomic-map」キーでAtomicHashMapが入っている
        cache.get("atomic-map").getClass should be (classOf[AtomicHashMap[_, _]])
      }
    }

    it("repeatable-read cache rollback") {
      withCache("repeatableReadCache") { cache =>
        val tm = cache.getAdvancedCache.getTransactionManager
        tm.begin()

        val map = AtomicMapLookup.getAtomicMap[String, String, String](cache, "atomic-map")

        map.put("key1", "value1")
        map.put("key2", "value2")

        tm.rollback()

        // 結果として空のAtomicHashMapとなってしまった場合は、getなどもできない
        // map.get("key1")
        // cache.get("atomic-map").getClass should be (classOf[AtomicHashMap[_, _]])
      }
    }

    it("read-committed cache") {
      withCache("readCommittedCache") { cache =>
        val tm = cache.getAdvancedCache.getTransactionManager
        tm.begin()

        val map = AtomicMapLookup.getAtomicMap[String, String, String](cache, "atomic-map")
        map.put("key1", "value1")
        map.put("key2", "value2")

        tm.commit()

        tm.begin()

        map.put("key1", "value1-1")
        map.put("key3", "value3")

        tm.rollback()

        map should have size 2
        map.get("key1") should be ("value1")
        map.get("key2") should be ("value2")
        map.get("key3") should be (null)
      }
    }

    it("remove AtomicMap") {
      withCache("readCommittedCache") { cache =>
        val tm = cache.getAdvancedCache.getTransactionManager
        tm.begin()

        val map = AtomicMapLookup.getAtomicMap[String, String, String](cache, "atomic-map")
        map.put("key1", "value1")
        map.put("key2", "value2")

        tm.commit()

        cache.get("atomic-map").getClass should be (classOf[AtomicHashMap[_, _]])

        tm.begin()
        AtomicMapLookup.removeAtomicMap(cache, "atomic-map")
        tm.commit()

        cache.get("atomic-map") should be (null)
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
