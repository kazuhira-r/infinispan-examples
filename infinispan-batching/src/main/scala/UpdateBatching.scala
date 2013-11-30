import scala.concurrent.SyncVar

import org.infinispan.Cache
import org.infinispan.manager.DefaultCacheManager

object UpdateBatching {
  def main(args: Array[String]): Unit = {
    val manager = new DefaultCacheManager("infinispan.xml")
    val cache = manager.getCache[String, String]("batchingCache")
    println(cache.getCacheConfiguration)

    try {
      val shared = new SyncVar[String]

      val writeThread = new UpdateThread(cache, shared)
      val readThread = new ReadThread(cache, shared)
      writeThread.start()
      readThread.start()

      writeThread.join()
      readThread.join()
    } finally {
      cache.stop()
      manager.stop()
    }
  }
}

trait ThreadSupport {
  self: Thread =>

  def w(millsec: Long): Unit = {
    p(s"Waiting $millsec msec...")
    Thread.sleep(millsec)
  }

  def p(msg: String): Unit =
    println(s"${getName} => $msg")  
}

class UpdateThread(cache: Cache[String, String], shared: SyncVar[String]) extends Thread("update-thread")
                                                                          with ThreadSupport {
  override def run(): Unit = {
    cache.put("not-batching-key", "not-batching-value")

    cache.put("shared-update-key", "shared-update-value")
    cache.put("shared-remove-key", "shared-remove-value")

    p("----- non batching writed -----")

    w(3000L)
    shared.take()
     
    cache.startBatch()

    cache.put("batching-key1", "batching-value1")
    cache.put("batching-key2", "batching-value2")
    cache.put("batching-key3", "batching-value3")

    cache.remove("shared-remove-key")
    cache.put("shared-update-key", "shared-update-value-updated")

    p("----- batch writed, not committed -----")

    shared.put("writed")
    w(3000L)
    shared.take()

    val end = false
    cache.endBatch(end)

    p(s"----- batch update end!![$end] -----")

    shared.put("commited")
  }
}

class ReadThread(cache: Cache[String, String], shared: SyncVar[String]) extends Thread("read-thread")
                                                                        with ThreadSupport {
  override def run(): Unit = {
    w(3000L)

    p("----- initial read -----")

    p(s"not-batching-key => ${cache.get("not-batching-key")}")

    p(s"shared-update-key => ${cache.get("shared-update-key")}")
    p(s"shared-remove-key => ${cache.get("shared-remove-key")}")

    shared.put("----- readed -----")

    w(3000L)

    shared.take()

    p("in batching read")

    p(s"not-batching-key => ${cache.get("not-batching-key")}")

    p(s"batching-key1 => ${cache.get("batching-key1")}")
    p(s"batching-key2 => ${cache.get("batching-key2")}")
    p(s"batching-key3 => ${cache.get("batching-key3")}")

    p(s"shared-update-key => ${cache.get("shared-update-key")}")
    p(s"shared-remove-key => ${cache.get("shared-remove-key")}")

    shared.put("readed")

    w(3000L)

    shared.take()

    p("----- end batching read -----")

    p(s"not-batching-key => ${cache.get("not-batching-key")}")

    p(s"batching-key1 => ${cache.get("batching-key1")}")
    p(s"batching-key2 => ${cache.get("batching-key2")}")
    p(s"batching-key3 => ${cache.get("batching-key3")}")

    p(s"shared-update-key => ${cache.get("shared-update-key")}")
    p(s"shared-remove-key => ${cache.get("shared-remove-key")}")
  }
}
