package org.littlewings.infinispan.mapreduce

import java.util.concurrent.{ExecutionException, TimeUnit}

import org.infinispan.Cache
import org.infinispan.commons.CacheException
import org.infinispan.distexec.mapreduce.MapReduceTask
import org.infinispan.manager.DefaultCacheManager

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class SlowMapReduceSpec extends FunSpec {
  describe("Infinispan Slow Map／Reduce Spec") {
    it("default timeout") {
      withCache("distCache", 4) { cache =>
        (1 to 10).foreach(i => cache.put(s"key$i", i))

        val task = new MapReduceTask[String, Int, String, Int](cache, true)

        task.timeout(TimeUnit.MILLISECONDS) should be (15000)
      }
    }

    it("timeout test") {
      withCache("distCache", 4) { cache =>
        (1 to 10).foreach(i => cache.put(s"key$i", i))

        val task = new MapReduceTask[String, Int, String, Int](cache, true)

        task
          .mappedWith(new SlowMapper(1, TimeUnit.SECONDS))
          .reducedWith(new SlowReducer(1, TimeUnit.SECONDS))
          .timeout(3, TimeUnit.SECONDS)

        the [CacheException] thrownBy {
          task.execute
        } getCause() should be (a [ExecutionException])
      }
    }
  }

  def withCache(cacheName: String, nodeNum: Int = 1)(fun: Cache[String, Int] => Unit): Unit = {
    val managers = (1 to nodeNum).map(_ => new DefaultCacheManager("infinispan.xml"))

    try {
      managers.foreach(_.getCache[String, Int](cacheName))

      val manager = managers.head
      val cache = manager.getCache[String, Int](cacheName)

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
