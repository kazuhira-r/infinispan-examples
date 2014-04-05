package org.littlewings.infinispan.hashalgorithm

import scala.collection.JavaConverters._

import org.infinispan.Cache
import org.infinispan.manager.DefaultCacheManager

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class InfinispanSegmentsSpec extends FunSpec {
  describe("infinispan segment spec") {
    it("singleSegmentCache") {
      println("========== Start singleSegmentCache ===========")

      withCache[String, String](4, "singleSegmentCache") { cache =>
        val range = 1 to 10

        range.foreach(i => cache.put(s"key$i", s"value$i"))

        val dm = cache.getAdvancedCache.getDistributionManager
        val consistentHash = dm.getConsistentHash

        consistentHash.getNumSegments should be (1)
      }

      println("========== End singleSegmentCache ===========")
    }

    it("defaultCache") {
      println("========== Start defaultCache ===========")

      withCache[String, String](4, "defaultCache") { cache =>
        val range = 1 to 10

        range.foreach(i => cache.put(s"key$i", s"value$i"))

        val dm = cache.getAdvancedCache.getDistributionManager
        val consistentHash = dm.getConsistentHash

        consistentHash.getNumSegments should be (60)

        dm.getPrimaryLocation("key1") should be (consistentHash.locatePrimaryOwner("key1"))
        dm.locate("key10") should be (consistentHash.locateOwners("key10"))

        consistentHash.locatePrimaryOwnerForSegment(0) should not be (null)
        consistentHash.locatePrimaryOwnerForSegment(59) should not be (null)
        an [ArrayIndexOutOfBoundsException] should be thrownBy consistentHash.locatePrimaryOwnerForSegment(60)
      }

      println("========== End defaultCache ===========")
    }
  }


  def withCache[K, V](numInstances: Int, cacheName: String)(fun: Cache[K, V] => Unit): Unit = {
    val managers = (1 to numInstances).map(_ => new DefaultCacheManager("infinispan.xml"))

    managers.foreach(_.getCache[K, V](cacheName))

    val cache = managers.head.getCache[K, V](cacheName)
    try {
      fun(cache)
    } finally {
      cache.stop()
      managers.foreach(_.stop())
    }
  }
}
