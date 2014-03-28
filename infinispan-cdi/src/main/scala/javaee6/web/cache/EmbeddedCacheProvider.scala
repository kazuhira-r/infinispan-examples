package javaee6.web.cache

import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.{Disposes, Produces}
import javax.inject.Inject

import org.infinispan.Cache
import org.infinispan.manager.EmbeddedCacheManager

class EmbeddedCacheProvider {
  @Inject
  private var cacheManager: EmbeddedCacheManager = _

  @CalcCache
  @Produces
  @ApplicationScoped
  def getCalcCache: Cache[String, Int] =
    cacheManager.getCache("calcCache")

  private def stopEmbeddedCacheManager(@CalcCache @Disposes cache: Cache[String, Int]): Unit =
    cache.stop
}
