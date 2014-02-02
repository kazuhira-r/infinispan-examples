package org.littlewings.infinispan.atomic

import scala.util.{Failure, Success, Try}

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

import javax.transaction.RollbackException

import org.infinispan.Cache
import org.infinispan.atomic.{AtomicHashMap, AtomicHashMapDelta, AtomicMapLookup}
import org.infinispan.manager.DefaultCacheManager

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class MultiThreadWithCacheSpec extends FunSpec {
  describe("multi thread spec") {
    it("use two thread to AtomicMap repeatable-read") {
      withCache("repeatableReadCache") { cache =>
        val latch1 = new CountDownLatch(1)
        val latch2 = new CountDownLatch(1)

        // 先に、AtomicMapを作成しておく
        val tm = cache.getAdvancedCache.getTransactionManager
        tm.begin()
        val map = AtomicMapLookup.getAtomicMap[String, String, String](cache, "atomic-map", true)
        tm.commit()

        val thread1Result = new AtomicReference[Try[Int]](Success(0))
        val thread1 = new Thread {
          override def run(): Unit =
            thread1Result.set {
              Try {
                val tm = cache.getAdvancedCache.getTransactionManager
                tm.begin()

                val map = AtomicMapLookup.getAtomicMap[String, String, String](cache, "atomic-map")

                map.put("key1", "value1")
                map.put("key2", "value2")
                latch1.countDown()

                Thread.sleep(3 * 1000L)

                // 先にThread2がコミットしているため、こちらは負ける
                a [RollbackException] should be thrownBy tm.commit()

                latch2.await()

                map.size
              }
            }
        }

        val thread2Result = new AtomicReference[Try[Int]](Success(0))
        val thread2 = new Thread {
          override def run(): Unit =
            thread2Result.set {
              Try {
                latch1.await()

                val tm = cache.getAdvancedCache.getTransactionManager
                tm.begin()

                val map = AtomicMapLookup.getAtomicMap[String, String, String](cache, "atomic-map")

                val start = System.currentTimeMillis

                // 別スレッドが更新しているキーに対して、getはできない（待ってしまう）
                // map.get("key1") should be ("value1")
                // map.get("key2") should be ("value2")
                map.put("key1", "value1-1")
                map.put("key3", "value3")

                val elapsed = System.currentTimeMillis
                elapsed should be > (3 * 1000L)

                tm.commit()

                latch2.countDown()

                map.size
              }
            }
        }

        Array(thread1, thread2)
          .map { t => t.start(); t }
          .foreach(_.join())

        thread1Result.get.get should be (2)
        thread2Result.get.get should be (2)

        // 結果は、Thread2のものが入っている
        map.get("key1") should be ("value1-1")
        map.get("key3") should be ("value3")
      }
    }

    it("use two thread to AtomicMap read-committed") {
      withCache("readCommittedCache") { cache =>
        val latch1 = new CountDownLatch(1)
        val latch2 = new CountDownLatch(1)

        // 先に、AtomicMapを作成しておく
        val tm = cache.getAdvancedCache.getTransactionManager
        tm.begin()
        val map = AtomicMapLookup.getAtomicMap[String, String, String](cache, "atomic-map", true)
        tm.commit()

        val thread1Result = new AtomicReference[Try[Int]](Success(0))
        val thread1 = new Thread {
          override def run(): Unit =
            thread1Result.set {
              Try {
                val tm = cache.getAdvancedCache.getTransactionManager
                tm.begin()

                val map = AtomicMapLookup.getAtomicMap[String, String, String](cache, "atomic-map")

                map.put("key1", "value1")
                map.put("key2", "value2")
                latch1.countDown()

                Thread.sleep(3 * 1000L)

                // この場合は、コミット可能
                tm.commit()

                latch2.await()

                map.size
              }
            }
        }

        val thread2Result = new AtomicReference[Try[Int]](Success(0))
        val thread2 = new Thread {
          override def run(): Unit =
            thread2Result.set {
              Try {
                latch1.await()

                val tm = cache.getAdvancedCache.getTransactionManager
                tm.begin()

                val map = AtomicMapLookup.getAtomicMap[String, String, String](cache, "atomic-map")

                val start = System.currentTimeMillis

                // 別スレッドが更新しているキーに対して、getはできない（待ってしまう）
                // map.get("key1") should be ("value1")
                // map.get("key2") should be ("value2")
                map.put("key1", "value1-1")
                map.put("key3", "value3")

                val elapsed = System.currentTimeMillis
                elapsed should be > (3 * 1000L)

                tm.commit()

                latch2.countDown()

                map.size
              }
            }
        }

        Array(thread1, thread2)
          .map { t => t.start(); t }
          .foreach(_.join())

        thread1Result.get.get should be (3)
        thread2Result.get.get should be (3)

        // 結果は、Thread2のもので上書かれている？
        map.get("key1") should be ("value1-1")
        map.get("key2") should be ("value2")
        map.get("key3") should be ("value3")
      }
    }

    it("use two thread to FineGrainedAtomicMap repeatable-read") {
      withCache("repeatableReadCache") { cache =>
        val latch1 = new CountDownLatch(1)
        val latch2 = new CountDownLatch(1)

        // 先に、AtomicMapを作成しておく
        val tm = cache.getAdvancedCache.getTransactionManager
        tm.begin()
        val map = AtomicMapLookup.getFineGrainedAtomicMap[String, String, String](cache, "fine-grained-atomic-map", true)
        tm.commit()

        val thread1Result = new AtomicReference[Try[Int]](Success(0))
        val thread1 = new Thread {
          override def run(): Unit =
            thread1Result.set {
              Try {
                val tm = cache.getAdvancedCache.getTransactionManager
                tm.begin()

                val map = AtomicMapLookup.getFineGrainedAtomicMap[String, String, String](cache, "fine-grained-atomic-map", true)

                map.put("key1", "value1")
                map.put("key2", "value2")
                latch1.countDown()

                Thread.sleep(3 * 1000L)

                // 先にThread2がコミットしているため、こちらは負ける
                tm.commit()

                latch2.await()

                map.size
              }
            }
        }

        val thread2Result = new AtomicReference[Try[Int]](Success(0))
        val thread2 = new Thread {
          override def run(): Unit =
            thread2Result.set {
              Try {
                latch1.await()

                val tm = cache.getAdvancedCache.getTransactionManager
                tm.begin()

                val map = AtomicMapLookup.getFineGrainedAtomicMap[String, String, String](cache, "fine-grained-atomic-map", true)

                val start = System.currentTimeMillis

                // 別スレッドが更新しているキーに対して、getはできない（待ってしまう）
                // map.get("key1") should be ("value1")
                // map.get("key2") should be ("value2")
                map.put("key1", "value1-1")
                map.put("key3", "value3")

                val elapsed = System.currentTimeMillis
                elapsed should be > (3 * 1000L)

                tm.commit()

                latch2.countDown()

                map.size
              }
            }
        }

        Array(thread1, thread2)
          .map { t => t.start(); t }
          .foreach(_.join())

        thread1Result.get.get should be (3)
        thread2Result.get.get should be (2)

        // 結果は、更新分のThread2はなくなり、put分が反映されている
        map.get("key1") should be ("value1")
        map.get("key2") should be ("value2")
        map.get("key3") should be ("value3")
      }
    }

    it("use two thread to FineGrainedAtomicMap read-committed") {
      withCache("readCommittedCache") { cache =>
        val latch1 = new CountDownLatch(1)
        val latch2 = new CountDownLatch(1)

        // 先に、AtomicMapを作成しておく
        val tm = cache.getAdvancedCache.getTransactionManager
        tm.begin()
        val map = AtomicMapLookup.getFineGrainedAtomicMap[String, String, String](cache, "fine-grained-atomic-map", true)
        tm.commit()

        val thread1Result = new AtomicReference[Try[Int]](Success(0))
        val thread1 = new Thread {
          override def run(): Unit =
            thread1Result.set {
              Try {
                val tm = cache.getAdvancedCache.getTransactionManager
                tm.begin()

                val map = AtomicMapLookup.getFineGrainedAtomicMap[String, String, String](cache, "fine-grained-atomic-map", true)

                map.put("key1", "value1")
                map.put("key2", "value2")
                latch1.countDown()

                Thread.sleep(3 * 1000L)

                // 先にThread2がコミットしているため、こちらは負ける
                tm.commit()

                latch2.await()

                map.size
              }
            }
        }

        val thread2Result = new AtomicReference[Try[Int]](Success(0))
        val thread2 = new Thread {
          override def run(): Unit =
            thread2Result.set {
              Try {
                latch1.await()

                val tm = cache.getAdvancedCache.getTransactionManager
                tm.begin()

                val map = AtomicMapLookup.getFineGrainedAtomicMap[String, String, String](cache, "fine-grained-atomic-map", true)

                val start = System.currentTimeMillis

                // 別スレッドが更新しているキーに対して、getはできない（待ってしまう）
                // map.get("key1") should be ("value1")
                // map.get("key2") should be ("value2")
                map.put("key1", "value1-1")
                map.put("key3", "value3")

                val elapsed = System.currentTimeMillis
                elapsed should be > (3 * 1000L)

                tm.commit()

                latch2.countDown()

                map.size
              }
            }
        }

        Array(thread1, thread2)
          .map { t => t.start(); t }
          .foreach(_.join())

        thread1Result.get.get should be (3)
        thread2Result.get.get should be (3)

        // 結果は、Thread2のもので上書かれている？
        map.get("key1") should be ("value1-1")
        map.get("key2") should be ("value2")
        map.get("key3") should be ("value3")
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
