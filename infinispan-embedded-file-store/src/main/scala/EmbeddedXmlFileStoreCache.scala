import org.infinispan.manager.DefaultCacheManager

object EmbeddedXmlFileStoreCache {
  def main(args: Array[String]): Unit = {
    val manager = new DefaultCacheManager("infinispan.xml")

    val cache = manager.getCache[String, String]("xmlFileStoreCache")

    val range = 1 to 10
    range foreach { v =>
      val key = "key" + v
      println(s"$key => ${cache.get(key)}")
    }

    Thread.sleep(3000L)

    range foreach { v =>
      val key = "key" + v
      cache.put(key, new java.util.Date().toString)
    }

    range foreach { v =>
      val key = "key" + v
      println(s"$key => ${cache.get(key)}")
    }
  }
}
