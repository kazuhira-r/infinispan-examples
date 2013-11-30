import org.infinispan.manager.DefaultCacheManager

object InfinispanDistributionManagerTest {
  def main(args: Array[String]): Unit = {
    val manager = new DefaultCacheManager("infinispan.xml")
    val cache = manager.getCache[String, String]

    val dm = cache.getAdvancedCache.getDistributionManager

    try {
      println("Local Address => " + manager.getAddress)

      println("Join Complete => " + dm.isJoinComplete)

      println("Rehash In Progress => " + dm.isRehashInProgress)

      println("Consistent Hash => " + dm.getConsistentHash)

      val (keys, values) =
        (1 to 100).map(i => (s"key$i", s"value$i")).unzip

      keys.zip(values).foreach { case (k, v) => cache.put(k, v) }

      keys.foreach { key =>
        println("Key[" + key + "]:" + System.lineSeparator +
                "\t\tPrimary Location => " + dm.getPrimaryLocation(key) + "," + System.lineSeparator +
                "\t\tLocality => " + dm.getLocality(key) + "," + System.lineSeparator +
                "\t\tLocate => " + dm.locate(key))
      }

      val rpcManager = cache.getAdvancedCache.getRpcManager
      println("All Cluster Members => " + rpcManager.getMembers)
    } finally {
      cache.stop()
      manager.stop()
    }
  }
}
