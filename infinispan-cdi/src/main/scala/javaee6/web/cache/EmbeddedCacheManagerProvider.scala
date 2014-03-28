package javaee6.web.cache

import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.{Disposes, Produces}

import org.infinispan.manager.{DefaultCacheManager, EmbeddedCacheManager}

class EmbeddedCacheManagerProvider {
  @Produces
  @ApplicationScoped
  def getDefaultEmbeddedCacheManager: EmbeddedCacheManager =
    new DefaultCacheManager("infinispan.xml")

  private def stopEmbeddedCacheManager(@Disposes cacheManager: EmbeddedCacheManager): Unit =
    cacheManager.stop()
}
