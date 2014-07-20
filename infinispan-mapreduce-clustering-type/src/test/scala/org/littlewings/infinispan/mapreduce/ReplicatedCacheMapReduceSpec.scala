package org.littlewings.infinispan.mapreduce

import scala.collection.JavaConverters._

import java.util.concurrent.TimeUnit

import org.infinispan.Cache
import org.infinispan.distexec.mapreduce.MapReduceTask
import org.infinispan.manager.DefaultCacheManager

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class ReplicatedCacheMapReduceSpec extends FunSpec {
  describe("Replicated Cache Map／Reduce Spec") {
    it("print task per thread-names") {
      withCache("replCache", 4) { cache =>
        (1 to 10).foreach(i => cache.put(s"key$i", s"value$i"))

        val task = new MapReduceTask[String, String, String, Set[String]](cache, true)
        task
          .mappedWith(new ReplicatedCacheMapper)
          .reducedWith(new ReplicatedCacheReducer)

        val results = task.execute

        results.asScala.foreach { case (k, v) => println(s"Key[$k] => $v") }
      }
    }

    it("print key locate") {
      withCache("replCache", 4) { cache =>
        (1 to 10).foreach(i => cache.put(s"key$i", s"value$i"))

        val dm = cache.getAdvancedCache.getDistributionManager

        cache.keySet.asScala.toList.sorted.foreach { key =>
          println(s"Key[$key]: PrimaryLocation[${dm.getPrimaryLocation(key)}], locate:${dm.locate(key)}")
        }

        cache.getAdvancedCache.getRpcManager.getDefaultRpcOptions(true).timeout should be (15000)
        cache.getAdvancedCache.getRpcManager.getDefaultRpcOptions(true).timeUnit should be (TimeUnit.MILLISECONDS)
      }
    }
  }

  def withCache(cacheName: String, nodeNum: Int = 1)(fun: Cache[String, String] => Unit): Unit = {
    val managers = (1 to nodeNum).map(_ => new DefaultCacheManager("infinispan.xml"))

    try {
      managers.foreach(_.getCache[String, String](cacheName))

      val manager = managers.head
      val cache = manager.getCache[String, String](cacheName)

      try {
        fun(cache)
      } finally {
        cache.stop()
      }
    } finally {
      managers.foreach(_.stop())
    }
  }
}
