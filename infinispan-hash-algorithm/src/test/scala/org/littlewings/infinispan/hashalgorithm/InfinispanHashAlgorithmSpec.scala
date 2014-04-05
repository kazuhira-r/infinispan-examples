package org.littlewings.infinispan.hashalgorithm

import scala.collection._
import scala.collection.JavaConverters._
import scala.util.Random

import org.infinispan.Cache
import org.infinispan.manager.{EmbeddedCacheManager, DefaultCacheManager}

import org.scalatest.{BeforeAndAfter, FunSpec}
import org.scalatest.Matchers._

class InfinispanHashAlgorithmSpec extends FunSpec with BeforeAndAfter {
  private var otherManagers: mutable.ArrayBuffer[EmbeddedCacheManager] = _

  before {
    otherManagers = mutable.ArrayBuffer.empty
  }

  describe("infinispan hash-algorithm spec") {
    it("defaultConsistentHashCache") {
      println("========== Start defaultConsistentCache ===========")

      withCache[String, String](4, "defaultConsistentHashCache") { cache =>
        val range = 1 to 10

        range.foreach(i => cache.put(s"key$i", s"value$i"))

        val advancedCache = cache.getAdvancedCache
        val dm = advancedCache.getDistributionManager
        val consistentHash = dm.getConsistentHash

        consistentHash.getNumSegments should be (60)

        println(s"""|Cluster Members:
                    |${advancedCache
                        .getRpcManager
                        .getMembers
                        .asScala
                        .mkString("  ", System.lineSeparator + "  ", "")}""".stripMargin)

        println("===")

        println("Cluster Initial State[DefaultConsistentHash]:")
        range.foreach(i => println(s"  Key[key$i]: Segment[${dm.getConsistentHash.getSegment(s"key$i")}] Primary[${dm.getPrimaryLocation(s"key$i")}]"))

        downMember()
        newMember(cache)

        // downMember()
        // newMember(cache)

        println("===")

        println(s"""|Cluster Members:
                    |${advancedCache
                        .getRpcManager
                        .getMembers
                        .asScala
                        .mkString("  ", System.lineSeparator + "  ", "")}""".stripMargin)

        println("===")

        println("Cluster Member Changed State[DefaultConsistentHash]:")
        range.foreach(i => cache.put(s"key$i", s"value$i"))
        range.foreach(i => println(s"  Key[key$i]: Segment[${dm.getConsistentHash.getSegment(s"key$i")}] Primary[${dm.getPrimaryLocation(s"key$i")}]"))
      }

      println("========== End defaultConsistentCache ===========")
    }

    it("syncConsistentHashCache") {
      println("========== Start syncConsistentCache ===========")

      withCache[String, String](4, "syncConsistentHashCache") { cache =>
        val range = 1 to 10

        range.foreach(i => cache.put(s"key$i", s"value$i"))

        val advancedCache = cache.getAdvancedCache
        val dm = advancedCache.getDistributionManager
        val consistentHash = dm.getConsistentHash

        consistentHash.getNumSegments should be (60)

        println(s"""|Cluster Members:
                    |${advancedCache
                        .getRpcManager
                        .getMembers
                        .asScala
                        .mkString("  ", System.lineSeparator + "  ", "")}""".stripMargin)

        println("===")

        println("Cluster Initial State[SyncConsistentHash]:")
        range.foreach(i => println(s"  Key[key$i]: Segment[${dm.getConsistentHash.getSegment(s"key$i")}] Primary[${dm.getPrimaryLocation(s"key$i")}]"))

        downMember()
        newMember(cache)

        // downMember()
        // newMember(cache)

        println("===")

        println(s"""|Cluster Members:
                    |${advancedCache
                        .getRpcManager
                        .getMembers
                        .asScala
                        .mkString("  ", System.lineSeparator + "  ", "")}""".stripMargin)

        println("===")

        println("Cluster Member Changed State[SyncConsistentHash]:")
        range.foreach(i => cache.put(s"key$i", s"value$i"))
        range.foreach(i => println(s"  Key[key$i]: Segment[${dm.getConsistentHash.getSegment(s"key$i")}] Primary[${dm.getPrimaryLocation(s"key$i")}]"))
      }        

      println("========== End syncConsistentCache ===========")
    }
  }

  def createCacheManager: EmbeddedCacheManager =
    new DefaultCacheManager("infinispan.xml")

  def withCache[K, V](numInstances: Int, cacheName: String)(fun: Cache[K, V] => Unit): Unit = {
    val managers = (1 to numInstances).map(_ => createCacheManager)

    val manager = managers.head
    otherManagers = mutable.ArrayBuffer.empty ++ managers.slice(1, numInstances)

    managers.foreach(_.getCache[K, V](cacheName))

    val cache = manager.getCache[K, V](cacheName)
    try {
      fun(cache)
    } finally {
      cache.stop()
      (manager +: otherManagers).foreach(_.stop())
    }
  }

  def downMember(): Unit = {
    val manager = otherManagers(Random.nextInt(otherManagers.size))
    otherManagers -= manager
    manager.stop()

    Thread.sleep(2 * 1000)
  }

  def newMember[K, V](cache: Cache[K, V]): Unit = {
    val manager = createCacheManager
    manager.getCache[K, V](cache.getName)
    otherManagers += manager
  }
}
