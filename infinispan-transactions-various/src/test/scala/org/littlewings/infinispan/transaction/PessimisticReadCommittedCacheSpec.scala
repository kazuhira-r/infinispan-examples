package org.littlewings.infinispan.transaction

import scala.util.{Success, Try}

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

import javax.transaction.RollbackException

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class PessimisticReadCommittedCacheSpec extends FunSpec
                                        with InfinispanCacheSupport[String, String]
                                        with ThreadSupport {
  describe("pessimistic read-committed cache spec") {
    it("test") {
      withCache("pessimisticReadCommittedCache") { cache =>
        val tm = cache.getAdvancedCache.getTransactionManager
        tm.begin()
        cache.put("key1", "value1")
        cache.put("key2", "value2")
        cache.put("key3", "value3")
        tm.commit()

        val result1 = new AtomicReference[Try[Int]](Success(0))
        val result2 = new AtomicReference[Try[Int]](Success(0))

        val latch1 = new CountDownLatch(1)
        val latch2 = new CountDownLatch(1)
        val latch3 = new CountDownLatch(1)
        val latch4 = new CountDownLatch(1)

        val thread1 = spawn {
          result1.set {
            Try {
              val tm = cache.getAdvancedCache.getTransactionManager
              tm.begin()

              cache.put("key4", "value4-1")
              cache.put("key5", "value5-1")

              cache.put("key1", "value1-1")
              cache.put("key2", "value2-1")

              latch1.countDown()

              latch2.await()

               tm.commit()
              //tm.rollback()
             
              latch3.await()

              latch4.countDown()

              cache.size
            }
          }
        }

        val thread2 = spawn {
          result2.set {
            Try {
              val tm = cache.getAdvancedCache.getTransactionManager
              tm.begin()

              latch1.await()

              // 別スレッドが更新中のキーにはアクセスできない
              // cache.get("key1") should be ("value1")

              // 他のキーなら参照可能
              cache.get("key3") should be ("value3")

              // 別スレッドが更新中のキーは、このケースの場合アクセスできない
              // cache.put("key5", "value5-2")

              // 関係ないキーは、追加可能
              cache.put("key6", "value6-2")

              tm.commit()

              latch2.countDown()

              latch3.countDown()

              latch4.await()

              cache.size
            }
          }
        }

        Array(thread1, thread2).foreach(_.join())

        result1.get.get should be (6)
        result2.get.get should be (6)

        cache.get("key1") should be ("value1-1")
        cache.get("key2") should be ("value2-1")
        cache.get("key3") should be ("value3")
        cache.get("key4") should be ("value4-1")
        cache.get("key5") should be ("value5-1")
        cache.get("key6") should be ("value6-2")
      }
    }
  }
}
