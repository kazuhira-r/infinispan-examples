import org.infinispan.Cache
import org.infinispan.manager.DefaultCacheManager

object EmbeddedCacheServer {
  val allKeys: Seq[Int] = 1 to 20

  def main(args: Array[String]): Unit = {
    val manager = new DefaultCacheManager("infinispan.xml")
    val cache = manager.getCache[String, String]("distCache")

    val keys = args.toList.headOption match {
      case Some("node1") => allKeys.take(10)
      case Some("node2") => allKeys.drop(10)
      case _ => 
        println("Please Input node1 or node2")
        sys.exit(1)
    }

    try {
      keys.foreach { i =>
        cache.put(s"key$i", s"value$i")
      }

      println("Initialized.")

      communicateWhile(cache, keys)
    } finally {
      manager.stop()
      println("CacheManager Stopped.")
    }
  }

  def communicateWhile(cache: Cache[String, String], keys: Seq[Int]): Unit =
    Iterator
      .continually(readLine())
      .withFilter(l => l != null && !l.isEmpty)
      .takeWhile(_ != "exit")
      .foreach { command => command.split("""\s+""").toList match {
          case "mylist" :: Nil =>
            keys.foreach { i =>
              val key = s"key$i"
              println(s"Key[$key] = ${cache.get(key)}")
            }
          case "list" :: Nil =>
            allKeys.foreach { i =>
              val key = s"key$i"
              println(s"Key[$key] = ${cache.get(key)}")
            }
          case "locate" :: Nil =>
            val dm = cache.getAdvancedCache.getDistributionManager
            allKeys.foreach { i =>
              val key = s"key$i"
              println(s"Key[$key]: PrimaryLocation = ${dm.getPrimaryLocation(key)}, Locate = ${dm.locate(key)}")
            }
          case "start" :: Nil =>
            cache.start()
            println("Cache Instance Started.")
          case "stop" :: Nil =>
            cache.stop()
            //cache.getCacheManager.removeCache(cache.getName)
            println("Cache Instance Stopped.")
          case _ => println(s"Unkwon Command[$command]")
        }
      }
}

