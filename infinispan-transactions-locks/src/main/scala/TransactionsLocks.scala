import java.util.Date
import javax.transaction.Status

import org.infinispan.Cache
import org.infinispan.configuration.cache.{ConfigurationBuilder, VersioningScheme}
import org.infinispan.manager.DefaultCacheManager
import org.infinispan.transaction.{LockingMode, TransactionMode}
import org.infinispan.transaction.lookup.GenericTransactionManagerLookup
import org.infinispan.util.concurrent.IsolationLevel

import org.infinispan.configuration.cache.CacheMode
import org.infinispan.configuration.global.GlobalConfigurationBuilder

object TransactionsLocks {
  def main(args: Array[String]): Unit = {
    val globalConfiguration = new GlobalConfigurationBuilder()
      .transport
      .defaultTransport
      .clusterName("transaction-cluster")
      .addProperty("configurationFile", "jgroups.xml")
      .build

    val manager = new DefaultCacheManager(globalConfiguration)
    //val manager = new DefaultCacheManager

    val cacheName = "transactionalCache"
    manager.defineConfiguration(cacheName,
                                new ConfigurationBuilder()
                                  .clustering
                                  .cacheMode(CacheMode.DIST_SYNC)
                                  .transaction
                                  .transactionMode(TransactionMode.TRANSACTIONAL)
                                  .transactionManagerLookup(new GenericTransactionManagerLookup)
                                /** Optimisitic Lock **/
                                  .lockingMode(LockingMode.OPTIMISTIC)
                                  .autoCommit(true)
                                  .locking
                                  //.isolationLevel(IsolationLevel.READ_COMMITTED)
                                  .isolationLevel(IsolationLevel.REPEATABLE_READ)
                                  .writeSkewCheck(true)
                                  .versioning
                                  .enabled(true)
                                  .scheme(VersioningScheme.SIMPLE)

                                /** Pessimitic Lock **/
                                /*
                                  .lockingMode(LockingMode.PESSIMISTIC)
                                  .autoCommit(true)
                                  .locking
                                //.isolationLevel(IsolationLevel.READ_COMMITTED)
                                  .isolationLevel(IsolationLevel.REPEATABLE_READ)
                                  .lockAcquisitionTimeout(0L)
                                  */
                                  .build)

    val cache = manager.getCache[String, String](cacheName)

    cache.put("key", "value")

    try {
      val firstThread = new FirstWriteThread(cache)
      val delayThread = new DelayWriteThread(cache)
      firstThread.start()
      delayThread.start()

      firstThread.join()
      delayThread.join()

      println(s"Last Value => ${cache.get("key")}")
    } finally {
      cache.stop()
      manager.stop()
    }
  }
}

trait ThreadSupport {
  self: Thread =>

  protected def w(mills: Long): Unit = {
    log(s"Waiting $mills msec...")
    Thread.sleep(mills)
  }

  protected def log(msg: String): Unit =
    println(s"[${new Date}] <${self.getName}> $msg")
}


class FirstWriteThread(cache: Cache[String, String]) extends Thread("first-write-thread")
                                                     with ThreadSupport {
  override def run(): Unit = {
    log("start transaction")

    val tm = cache.getAdvancedCache.getTransactionManager
    try {
      tm.begin()

      cache.put("key", "value-by-first-write-thread")
      log("updated!!")

      log(cache.get("key"))

      w(1200L)

      log("try commit")
      tm.commit()
      log("committed")
    } catch {
      case e: Exception =>
        tm.getStatus match {
          case Status.STATUS_ACTIVE => tm.rollback()
          case Status.STATUS_MARKED_ROLLBACK => tm.rollback()
          case _ =>
        }

        log(s"Exception Cached [${e.toString()}]")
    }

    log("end transaction")
  }
}

class DelayWriteThread(cache: Cache[String, String]) extends Thread("delay-write-thread")
                                                     with ThreadSupport {
  override def run(): Unit = {
    log("start transaction")

    val tm = cache.getAdvancedCache.getTransactionManager
    try {
      tm.begin()

      w(1000L)

      cache.put("key", "value-by-delay-write-thread")
      log("updated!!")

      log(cache.get("key"))

      w(500L)

      log("try commit")
      tm.commit()
      log("committed")

      println(cache.getAdvancedCache.getCacheEntry("key", null, null).getVersion)
    } catch {
      case e: Exception =>
        tm.getStatus match {
          case Status.STATUS_ACTIVE => tm.rollback()
          case Status.STATUS_MARKED_ROLLBACK => tm.rollback()
          case _ =>
        }
        log(s"Exception Cached [${e.toString()}]")
    }

    log("end transaction")
  }
}
