package org.littlewings.infinispan.statetranfer

import org.infinispan.Cache
import org.infinispan.manager.DefaultCacheManager

trait EmbeddedCacheSupport {
  def withCache[K, V](configFileName: String, cacheName: String = null)(fun: (Cache[K, V], Long) => Unit): Unit = {
    val manager = new DefaultCacheManager(configFileName)
    try {
      val startTime = System.currentTimeMillis

      val cache =
        if (cacheName != null) manager.getCache[K, V](cacheName)
        else manager.getCache[K, V]

      val elapsedTime = System.currentTimeMillis - startTime

      fun(cache, elapsedTime)
    } finally {
      manager.stop()
    }
  }
}
