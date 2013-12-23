package org.littlewings.infinispan.statetranfer

import org.infinispan.manager.DefaultCacheManager

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class AwaitInitialTransferOffSpec extends FunSpec with EmbeddedCacheSupport {
  describe("awaitInitialTransfer Off state transfer configuration spec") {
    it("state transfer settings") {
      withCache[String, String]("infinispan-awaitInitialTransferOff.xml") { (cache, _) =>
        val configuration = cache.getCacheConfiguration
        
        val stateTransferConfiguration =
          configuration.clustering.stateTransfer

        stateTransferConfiguration.awaitInitialTransfer should be (false)
        stateTransferConfiguration.chunkSize should be (10000)
        stateTransferConfiguration.fetchInMemoryState should be (true)
        stateTransferConfiguration.timeout should be (240000)
      }
    }
  }

  describe("data inserted spec") {
    it("no data") {
      withCache[String, String]("infinispan-awaitInitialTransferOff.xml") { (cache1, _) =>
        withCache[String, String]("infinispan-awaitInitialTransferOff.xml") { (cache2, elapsedTime) =>
          elapsedTime should be < 1000L
        }
      }
    }

    it("many data") {
      withCache[String, String]("infinispan-awaitInitialTransferOff.xml") { (cache1, _) =>
        (1 to 100000).foreach { i =>
          cache1.put(s"key$i", s"value$i")
        }

        withCache[String, String]("infinispan-awaitInitialTransferOff.xml") { (cache2, elapsedTime) =>
          elapsedTime should be < 1000L
          cache2.get("key100000") should be ("value100000")
        }
      }
    }
  }
}
