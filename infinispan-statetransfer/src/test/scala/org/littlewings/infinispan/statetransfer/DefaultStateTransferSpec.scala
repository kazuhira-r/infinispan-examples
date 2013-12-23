package org.littlewings.infinispan.statetranfer

import org.infinispan.manager.DefaultCacheManager

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class DefaultStateTransferSpec extends FunSpec with EmbeddedCacheSupport {
  describe("state transfer configuration spec") {
    it("default state transfer settings") {
      withCache[String, String]("infinispan-simple.xml") { (cache, _) =>
        val configuration = cache.getCacheConfiguration
        
        val stateTransferConfiguration =
          configuration.clustering.stateTransfer

        stateTransferConfiguration.awaitInitialTransfer should be (true)
        stateTransferConfiguration.chunkSize should be (10000)
        stateTransferConfiguration.fetchInMemoryState should be (true)
        stateTransferConfiguration.timeout should be (240000)
      }
    }
  }

  describe("data inserted spec") {
    it("no data") {
      withCache[String, String]("infinispan-simple.xml") { (cache1, _) =>
        withCache[String, String]("infinispan-simple.xml") { (cache2, elapsedTime) =>
          elapsedTime should be < 1500L
        }
      }
    }

    it("many data") {
      withCache[String, String]("infinispan-simple.xml") { (cache1, _) =>
        (1 to 100000).foreach { i =>
          cache1.put(s"key$i", s"value$i")
        }

        withCache[String, String]("infinispan-simple.xml") { (cache2, elapsedTime) =>
          elapsedTime should be < 6000L
          cache2.get("key100000") should be ("value100000")
        }
      }
    }
  }
}
