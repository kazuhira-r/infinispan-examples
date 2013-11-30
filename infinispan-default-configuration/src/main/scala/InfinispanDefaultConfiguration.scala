import org.infinispan.Cache
import org.infinispan.configuration.cache._
import org.infinispan.configuration.global.GlobalConfigurationBuilder
import org.infinispan.manager.{DefaultCacheManager, EmbeddedCacheManager}
import org.infinispan.transaction.{LockingMode, TransactionMode}
import org.infinispan.transaction.lookup.GenericTransactionManagerLookup

trait InfinispanCacheConfigurationPrintSupport {
  protected def createCacheManager: EmbeddedCacheManager

  def run(): Unit = {
    val manager = createCacheManager

    val defaultCache = manager.getCache[Any, Any]()
    val simpleNamedCache = manager.getCache[Any, Any]("simpleNamedCache")
    val namedCacheOverride = manager.getCache[Any, Any]("namedCacheOverride")

    try {
      for (cache <- List(
        defaultCache,
        simpleNamedCache,
         namedCacheOverride)) {
        println(s"========================= CacheName[${cache.getName}] =========================")
        print(cache.getCacheConfiguration.toString.replaceAll("(\\{|\\})", "$1\n"))
        println(s"========================= CacheName[${cache.getName}] =========================")
      }

      println(System.lineSeparator * 3)

      val cacheConfigurationString =
      (cache: Cache[_, _]) =>
      cache
        .getCacheConfiguration
        .toString
        .replaceAll("(\\{|\\})", "$1\n")
        .replaceAll("""@[0-9a-z]+""", "")

      val defaultCacheDefinition = cacheConfigurationString(defaultCache)

      for ((name, definition) <- List(
        (simpleNamedCache.getName, cacheConfigurationString(simpleNamedCache)),
        (namedCacheOverride.getName, cacheConfigurationString(namedCacheOverride))
      )) {
        val (_, diff) =
          defaultCacheDefinition.zip(definition)
            .span { case (c1, c2) => c1 == c2 }

        println(s"******************** defaultCache diff $name, Start ********************")
        diff.foreach { case (_, c) => print(c) }
        println(s"******************** defaultCache diff $name, End   ********************")
      }
    } finally {
      defaultCache.stop()
      simpleNamedCache.stop()
      namedCacheOverride.stop()

      manager.stop()
    }
  }
}

object InfinispanDeclarativeConfiguration extends InfinispanCacheConfigurationPrintSupport {
  protected def createCacheManager: EmbeddedCacheManager =
    new DefaultCacheManager("infinispan.xml")

  def main(args: Array[String]): Unit =
    run()
}

object InfinispanProgrammaticalConfiguration extends InfinispanCacheConfigurationPrintSupport {
  protected def createCacheManager: EmbeddedCacheManager = {
    val globalConfiguration =
      new GlobalConfigurationBuilder()
        .transport
        .defaultTransport
        .clusterName("configuration-cluster")
        .addProperty("configurationFile", "jgroups.xml")
        .globalJmxStatistics
        .enable
        .jmxDomain("org.infinispan")
        .cacheManagerName("DefaultCacheManager")
        .build

    val defaultCacheConfiguration =
      new ConfigurationBuilder()
        .clustering
        .cacheMode(CacheMode.DIST_SYNC)
        .transaction
        .transactionManagerLookup(new GenericTransactionManagerLookup)
        .transactionMode(TransactionMode.TRANSACTIONAL)
        .lockingMode(LockingMode.PESSIMISTIC)
        .autoCommit(true)
        .build

    val manager = new DefaultCacheManager(globalConfiguration, defaultCacheConfiguration)

    //val manager = new DefaultCacheManager("infinispan-default.xml")
    //val defaultCacheConfiguration = manager.getDefaultCacheConfiguration

    manager.defineConfiguration("namedCacheOverride",
                                new ConfigurationBuilder()
                                  .read(defaultCacheConfiguration)
                                  .storeAsBinary
                                  .enable
                                  .invocationBatching
                                  .enable
                                  .transaction
                                  .lockingMode(LockingMode.OPTIMISTIC)
                                  .autoCommit(false)
                                  .build)

    manager
  }

  def main(args: Array[String]): Unit =
    run()
}
