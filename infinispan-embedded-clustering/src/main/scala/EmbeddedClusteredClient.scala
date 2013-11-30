import org.infinispan.manager.DefaultCacheManager

object EmbeddedClusteredClient {
  def main(args: Array[String]): Unit = {
    val (selfName, pairNames) = args.toList match {
      case s :: restNames => (s, restNames)
      case _ => sys.exit(1)
    }
    new EmbeddedClusteredClient(selfName, pairNames).run
  }
}

class EmbeddedClusteredClient(val selfName: String, val pairNames: List[String]) {
  def run(): Unit = {
    val manager = new DefaultCacheManager("infinispan.xml")
    val cache = manager.getCache[String, String]()
    cache.addListener(new LoggingListener)

    try {
      val range = 1 to 5
      val keys = range.map(k => key(k.toString))
      val values = range.map(v => s"value$v")
      val pairNamesKeys = pairNames.flatMap(pn => range.map(k => key(pn, k.toString)))

      (keys zip values) foreach { case (k, v) => cache.put(k, v) }

      println("Sleeping...")
      Thread.sleep(10000L)

      keys foreach (k => log(s"$k => ${cache.get(k)}"))
      pairNamesKeys foreach (k => log(s"$k => ${cache.get(k)}"))
    } finally {
      cache.stop()
      manager.stop()
    }
  }

  def key(seed: String): String = key(selfName, seed)
  def key(name: String, seed: String): String = s"${name}:${seed}"

  def log(message: String): Unit =
    println(s"[$selfName] received, $message")
}
