package org.littlewings.infinispan.jta.inject

import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.{Disposes, Produces}
import javax.inject.Inject

import org.infinispan.Cache
import org.infinispan.manager.{DefaultCacheManager, EmbeddedCacheManager}

import org.littlewings.infinispan.jta.entity.User

class CacheProducer {
  @Produces
  @ApplicationScoped
  def getEmbeddedCacheManager: EmbeddedCacheManager =
    new DefaultCacheManager("infinispan-optimistic-tx.xml")

  def stopEmbeddedCacheManager(@Disposes manager: EmbeddedCacheManager): Unit =
    manager.stop()

  @Produces
  @ApplicationScoped
  def getCache(@Inject manager: EmbeddedCacheManager): Cache[Int, User] =
    manager.getCache()

  def stopCache(@Disposes cache: Cache[Int, User]): Unit =
    cache.stop()
}
