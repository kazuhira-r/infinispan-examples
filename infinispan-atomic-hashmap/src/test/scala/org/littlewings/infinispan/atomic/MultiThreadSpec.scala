package org.littlewings.infinispan.atomic

import scala.util.{Failure, Success, Try}

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

import org.infinispan.atomic.AtomicHashMap

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class MultiThreadSpec extends FunSpec {
  describe("multi thread spec") {
    it("use two thread") {
      val latch1 = new CountDownLatch(1)
      val latch2 = new CountDownLatch(1)

      val map = new AtomicHashMap[String, String]

      val thread1Result = new AtomicReference[Try[Int]](Success(0))
      val thread1 = new Thread {
        override def run(): Unit =
          thread1Result.set {
            Try {
              map.put("key1", "value1")
              map.put("key2", "value2")
              latch1.countDown()

              Thread.sleep(3 * 1000L)

              map.commit()

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

              // putは、Thread1と異なるキーならブロックしない
              // 同じキーをputすると、このコードの場合はデッドロックになる

              val start = System.currentTimeMillis

              // getは、Thread1がコミットするまでブロックする
              map.get("key1") should be ("value1")
              map.get("key2") should be ("value2")

              val elapsed = System.currentTimeMillis
              elapsed should be > (3 * 1000L)

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
    }
  }
}
