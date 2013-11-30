import scala.collection.JavaConverters._

import java.util.concurrent.Executors

import org.infinispan.Cache
import org.infinispan.affinity.{KeyAffinityServiceFactory, RndKeyGenerator}
import org.infinispan.manager.DefaultCacheManager

object KeyAffinityServiceExample {
  def main(args: Array[String]): Unit = {
    val manager = new DefaultCacheManager("infinispan.xml")
    val cache = manager.getCache[AnyRef, String]("keyAffinityCache")

    try {
      val distributionManager = cache.getAdvancedCache.getDistributionManager

      println(distributionManager.getConsistentHash.locatePrimaryOwner("1"))
/*
      val localKeyExecutor = Executors.newSingleThreadExecutor
      val localKeyAffinityService =
        KeyAffinityServiceFactory.newKeyAffinityService(cache,
                                                             localKeyExecutor,
                                                             new RndKeyGenerator,
                                                             100)

      val address = manager.getAddress

      for (i <- 0 to 5) {
        val key = localKeyAffinityService.getKeyForAddress(address)
        cache.put(key, "1")
        println(cache.getAdvancedCache.getDistributionManager.getConsistentHash.locatePrimaryOwner(key))
        println(s"Generated KeyForAddress => value[$key], type[${key.getClass}]")
        println(localKeyAffinityService.getCollocatedKey(key))
      }

      localKeyExecutor.shutdown()

      for (key <- cache.keySet.asScala) {
        println(s"Key[$key], Primary Location[${distributionManager.getPrimaryLocation(key)}]")
      }

      val executor = Executors.newSingleThreadExecutor
      val addresses = cache.getAdvancedCache.getRpcManager.getMembers
      val keyAffinityService =
        KeyAffinityServiceFactory.newKeyAffinityService(cache,
                                                        addresses,
                                                        new RndKeyGenerator,
                                                        executor,
                                                        100)

      for (i <- 0 to 5) {
        val addr = addresses.get(i % addresses.size)
        val key = keyAffinityService.getKeyForAddress(addr)
        cache.put(key, "1")
        println(s"Generated KeyForAddress => value[$key], type[${key.getClass}]")
        println(s"Use Address[$addr], Primary Location[${distributionManager.getPrimaryLocation(key)}]" +
                " " + s"Locate[${distributionManager.locate(key)}]")
        val collocatedKey = keyAffinityService.getCollocatedKey(key)
        cache.put(collocatedKey, "1")
        println(s"CollocatedKey[$collocatedKey], Primary Location[${distributionManager.getPrimaryLocation(collocatedKey)}]")
      }

      executor.shutdown()
*/
    } finally {
      cache.stop()
      manager.stop()
    }
  }
}
