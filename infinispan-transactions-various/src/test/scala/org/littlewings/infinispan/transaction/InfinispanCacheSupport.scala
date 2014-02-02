package org.littlewings.infinispan.transaction

import org.infinispan.Cache
import org.infinispan.manager.DefaultCacheManager

trait InfinispanCacheSupport[K, V] {
  protected def withCache[R](cacheName: String)(fun: Cache[K, V] => R): R = {
    val manager = new DefaultCacheManager("infinispan.xml")

    try {
      val cache =
        if (cacheName == null || cacheName.isEmpty) manager.getCache[K, V]
        else manager.getCache[K, V](cacheName)

      try {
        fun(cache)
      } finally {
        cache.stop()
      }

    } finally {
      manager.stop()
    }
  }

  protected def withCache[R](fun: Cache[K, V] => R): R =
    withCache(null.asInstanceOf[String])(fun)
}
