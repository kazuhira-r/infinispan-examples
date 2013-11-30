import java.util.Date

import org.infinispan.Cache
import org.infinispan.manager.DefaultCacheManager

object InfinispanTransactions {
  def main(args: Array[String]): Unit = {
    val manager = new DefaultCacheManager("infinispan.xml")
    val cache = manager.getCache[String, String]()

    val range = 1 to 3
    val keys = range map (i => s"key$i")

    /*
    val tm = cache.getAdvancedCache.getTransactionManager
    tm.begin()

    range foreach (i => cache.put(keys(i - 1), s"value$i"))
    var values = keys map (k => cache.get(k)) mkString(", ")
    println(s"results => $values")

    tm.commit()
    tm.rollback()

    values = keys map (k => cache.get(k)) mkString(", ")
    println(s"results => $values")
    */

    range foreach (i => cache.put(keys(i - 1), s"value$i"))

    val (th1, th2) = (new Thread1(cache, keys), new Thread2(cache, keys))
    th1.start()
    th2.start()

    th1.join()
    th2.join()

    val values = keys map (k => cache.get(k)) mkString(", ")
    println(s"results => $values")
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

class Thread1(cache: Cache[String, String], knownKeys: Seq[String]) extends Thread("thread-1")
                                                                    with ThreadSupport {
  override def run(): Unit = {
    log("start")

    val tm = cache.getAdvancedCache.getTransactionManager

    log("begin transaction")
    tm.begin()

    w(2000)

    val initialValues = knownKeys map (k => cache.get(k))
    log(s"read initial values ${initialValues.mkString("[", ", ", "]")}")

    w(3000)

    val nextValues = knownKeys map (k => cache.get(k))
    log(s"read next values ${nextValues.mkString("[", ", ", "]")}")

    log("commit transaction")
    tm.commit()

    log("end")
  }
}

class Thread2(cache: Cache[String, String], knownKeys: Seq[String]) extends Thread("thread-2")
                                                                    with ThreadSupport {
  override def run(): Unit = {
    log("start")

    val tm = cache.getAdvancedCache.getTransactionManager

    log("begin transaction")
    tm.begin()

    w(3000)

    log(s"Key[${knownKeys(1)}] update")
    cache.put(knownKeys(1), "Updated value2")

    log("commit transaction")
    tm.commit()

    log("end")
  }
}
  
