import org.infinispan.manager.DefaultCacheManager

trait EmbeddedCacheServerSupport {
  val nodeNameTo: Map[String, Int] = Map("node1" -> 10, "node2" -> 20)
  val increase: Int = 5
}

object EmbeddedCacheServer extends EmbeddedCacheServerSupport {
  def main(args: Array[String]): Unit = {
    val start = nodeNameTo
      .get(args.toList.headOption.getOrElse(""))
      .getOrElse(throw new IllegalArgumentException(s"Required NodeName[${nodeNameTo.keys.mkString(" or ")}]"))

    val cache = new DefaultCacheManager("infinispan.xml").getCache[Any, Any]("l1CacheEnabled")
    cache.addListener(new CacheListener)

    val fun = (now: Long) => {
      (start to start + increase).foreach { i => cache.put(s"key$i", s"value${i}-${now}") }
      Thread.sleep(5 * 1000L)
    }

    Stream
      .continually(System.currentTimeMillis)
      .foreach(fun)
  }
}
