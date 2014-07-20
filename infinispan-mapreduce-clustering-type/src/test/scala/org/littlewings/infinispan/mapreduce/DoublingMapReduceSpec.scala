package org.littlewings.infinispan.mapreduce

import org.infinispan.Cache
import org.infinispan.distexec.mapreduce.MapReduceTask
import org.infinispan.manager.DefaultCacheManager

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class DoublingMapReduceSpec extends FunSpec {
  describe("Infinispan Map／Reduce Spec") {
    it("local cache") {
      withCache("localCache") { cache =>
        (1 to 10).foreach(i => cache.put(s"key$i", i))

        val task = new MapReduceTask[String, Int, String, Int](cache, true)
        task
          .mappedWith(new DoublingMapper)
          .reducedWith(new DoublingReducer)

        val result = task.execute(new SummerizeCollator)

        result should be (110)
      }
    }

    it("dist cache") {
      withCache("distCache", 4) { cache =>
        (1 to 10).foreach(i => cache.put(s"key$i", i))

        val task = new MapReduceTask[String, Int, String, Int](cache, true)
        task
          .mappedWith(new DoublingMapper)
          .reducedWith(new DoublingReducer)

        val result = task.execute(new SummerizeCollator)

        result should be (110)
      }
    }

    it("repl cache") {
      withCache("replCache", 4) { cache =>
        (1 to 10).foreach(i => cache.put(s"key$i", i))

        val task = new MapReduceTask[String, Int, String, Int](cache, true)
        task
          .mappedWith(new DoublingMapper)
          .reducedWith(new DoublingReducer)

        val result = task.execute(new SummerizeCollator)

        result should be (110)
      }
    }

    it("invl cache") {
      withCache("invlCache", 4) { cache =>
        (1 to 10).foreach(i => cache.put(s"key$i", i))

        the [IllegalStateException] thrownBy {
          new MapReduceTask[String, Int, String, Int](cache, true)
        } should have message "ISPN000231: Cache mode should be DIST or REPL, rather than INVALIDATION_SYNC"
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
