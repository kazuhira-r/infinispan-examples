import org.infinispan.manager.DefaultCacheManager

object InfinispanL1Caching extends EmbeddedCacheServerSupport {
  def main(args: Array[String]): Unit = {
    val manager = new DefaultCacheManager("infinispan.xml")
    val cache = manager.getCache[String, String]("l1CacheEnabled")

    cache.addListener(new CacheListener)

    println(cache.asInstanceOf[org.infinispan.CacheImpl[_, _]].getConfigurationAsXmlString)

    try {
      val readValues = () => {
        nodeNameTo.values.foreach { v =>
          (v to v + increase).foreach { i =>
            cache.get(s"key$i")
          }
        }
      }

      val printDataLocality = () => {
        val dm = cache.getAdvancedCache.getDistributionManager

        val rpcManager = cache.getAdvancedCache.getRpcManager
        println("All Cluster Members => " + rpcManager.getMembers)

        nodeNameTo.values.foreach { v =>
          (v to v + increase).foreach { i =>
            val key = s"key$i"
            println(s"Data Locality: Key[$key] => PrimaryLocation[${dm.getPrimaryLocation(key)}], "
                    + s"Locate:${dm.locate(key)}")
          }
        }
      }

      (1 to 5).foreach { i =>
        readValues()
        printDataLocality()
        readValues()
        readValues()
        val waitTime = 10 * 1000L
        println(s"Sleeping... ${waitTime}sec")
        Thread.sleep(waitTime)
      }
    } finally {
      cache.stop()
      manager.stop()
    }
  }
}
