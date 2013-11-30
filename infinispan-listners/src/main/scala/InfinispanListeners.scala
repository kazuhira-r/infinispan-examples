import scala.collection.JavaConverters._

import javax.transaction.Status

import org.infinispan.Cache
import org.infinispan.configuration.cache.CacheMode
import org.infinispan.manager.DefaultCacheManager
import org.infinispan.transaction.TransactionMode

object InfinispanListeners extends ThreadNameLogSupport
                           with SimpleClassNameLogSupport {
  def main(args: Array[String]): Unit = {
    System.setProperty("nodeId", "master")

    val manager = new DefaultCacheManager("infinispan.xml")
    val cache = manager.getCache[String, String]("listenersCache")

    manager.addListener(new CacheManagerLevelListener)
    cache.addListener(new CacheLevelListener)

    cache.stop()
    cache.start()

    cache.addListener(new CacheLevelListener)

    try {
      simplePattern(cache)
      useTransactionIfEnabled(cache)
      manyEntries(cache, 100, 110)
    } finally {
      cache.stop()
      manager.stop()
    }
  }

  def simplePattern(cache: Cache[String, String]): Unit = {
    val entry = ("key1", "value1")
    log(s"エントリ $entry を参照します")
    cache.get(entry._1)

    log(s"エントリ $entry を登録します")
    cache.put(entry._1, entry._2)

    log(s"エントリ $entry を参照します")
    cache.get(entry._1)

    val entryReplace = ("key1", "value1-replace")
    log(s"エントリ $entryReplace をreplaceで更新します")
    cache.replace(entryReplace._1, entryReplace._2)

    val entryPut = ("key1", "value1-put")
    log(s"エントリ $entryPut をputで更新します")
    cache.put(entryPut._1, entryPut._2)

    log(s"エントリ $entry を削除します")
    cache.remove(entry._1)
  }

  def manyEntries(cache: Cache[String, String], start: Int, end: Int): Unit = {
    val keysValues = (start to end) map (i => (s"key$i", s"value$i"))
    log(s"エントリを ${keysValues.size} 個参照します")
    keysValues.foreach { case (k, _) => cache.get(k) }

    log(s"エントリを ${keysValues.size} 個登録します")
    keysValues.foreach { case (k, v) => cache.put(k, v) }

    /*
    log(s"エントリを ${keysValues.size} 個参照します")
    keysValues.foreach { case (k, _) => cache.get(k) }
    */
  }

  def useTransactionIfEnabled(cache: Cache[String, String]): Unit =
    cache.getCacheConfiguration.transaction.transactionMode match {
      case TransactionMode.TRANSACTIONAL =>
        val tm = cache.getAdvancedCache.getTransactionManager

        try {
          log("トランザクションを開始します")
          tm.begin()

          val pair1 = ("transactional-key1", "transactional-value1")
          log(s"データ $pair1 を登録します")
          cache.put(pair1._1, pair1._2)

          val pair1Update = ("transactional-key1", "transactional-value1-update")
          log(s"データ $pair1Update を更新します")
          cache.put(pair1Update._1, pair1Update._2)

          log("トランザクションをコミットします")
          tm.commit()
        } catch {
          case th: Throwable =>
            th.printStackTrace()
            tm.getStatus match {
              case Status.STATUS_ACTIVE | Status.STATUS_MARKED_ROLLBACK =>
                log("トランザクションをロールバックします")
                tm.rollback()
              case _ =>
            }
        }

        log("トランザクションを開始します")
        tm.begin()

        val pair1 = ("transactional-key1", "transactional-value1")
        log(s"データ $pair1 を削除します")
        cache.remove(pair1._1)

        val pair2 = ("transactional-key2", "transactional-value2")
        log(s"データ $pair2 を登録します")
        cache.put(pair2._1, pair2._2)

        log("トランザクションをロールバックします")
        tm.rollback()
      case _ =>
    }
}
