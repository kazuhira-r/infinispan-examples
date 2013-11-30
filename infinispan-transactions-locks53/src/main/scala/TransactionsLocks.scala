import java.util.Date
import javax.transaction.Status

import org.infinispan.Cache
import org.infinispan.configuration.cache.{ConfigurationBuilder, VersioningScheme}
import org.infinispan.manager.DefaultCacheManager
import org.infinispan.transaction.{LockingMode, TransactionMode}
import org.infinispan.transaction.lookup.{GenericTransactionManagerLookup, JBossStandaloneJTAManagerLookup}
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
                                  //.transactionManagerLookup(new JBossStandaloneJTAManagerLookup)
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

    try {
      val cache = manager.getCache[String, String](cacheName)

      cache.put("key", "value")

      val firstThread = new FirstWriteThread(cache)
      val delayThread = new DelayWriteThread(cache)
      firstThread.start()
      delayThread.start()

      firstThread.join()
      delayThread.join()

      println(s"Last Value => ${cache.get("key")}")

      cache.stop()
    } finally {
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
    } catch {
      case e: Exception => log(s"Exception Cached [${e.toString()}]")
    } finally {
      log(s"Status => ${tm.getStatus}")
      tm.getStatus match {
        case Status.STATUS_ACTIVE =>
          log("try commit")
          tm.commit()
          log("committed")
        case _ => tm.rollback()
      }
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

      val cacheEntry = cache.getAdvancedCache.getCacheEntry("key")
      println(s"$cacheEntry, meta = ${cacheEntry.getMetadata.version}")
    } catch {
      case e: Exception => log(s"Exception Cached [${e.toString()}]")
    } finally {
      log(s"Status => ${tm.getStatus}")
      tm.getStatus match {
        case Status.STATUS_ACTIVE =>
          log("try commit")
          tm.commit()
          log("committed")
        case _ => tm.rollback()
      }
    }

    log("end transaction")
  }
}
