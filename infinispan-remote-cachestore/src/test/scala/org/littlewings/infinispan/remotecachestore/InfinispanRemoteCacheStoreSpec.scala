package org.littlewings.infinispan.remotecachestore

import scala.collection.JavaConverters._

import java.util.Objects

import org.infinispan.Cache
import org.infinispan.client.hotrod.{RemoteCache, RemoteCacheManager}
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder
import org.infinispan.container.entries.InternalCacheEntry
import org.infinispan.manager.DefaultCacheManager

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class InfinispanRemoteCacheStoreSpec extends FunSpec {
  describe("no cache-store spec") {
    it("cluster down, missing data") {
      val clusterSize = 3
      val keysValues = (1 to 5).map(i => (s"key$i", new Entity(s"value$i")))

      withCache(clusterSize, "noStoreCache") { cache =>
        keysValues.foreach { case (k, v) => cache.put(k, v) }

        keysValues.foreach { case (k, v) =>
          cache.get(k) should be (new Entity(v.value))
        } 
      }

      withCache(clusterSize, "noStoreCache") { cache =>
        keysValues.foreach { case (k, v) =>
          cache.get(k) should be (null)
        }
      }
    }
  }

  describe("remote-cache-store spec") {
    it("saved data") {
      val clusterSize = 3
      val keysValues = (1 to 5).map(i => (s"key$i", new Entity(s"value$i")))

      withCache(clusterSize, "remoteStoreCache") { cache =>
        keysValues.foreach { case (k, v) => cache.put(k, v) }

        keysValues.foreach { case (k, v) =>
          cache.get(k) should be (new Entity(v.value))
        } 
      }

      withCache(clusterSize, "remoteStoreCache") { cache =>
        keysValues.foreach { case (k, v) =>
          cache.get(k) should be (new Entity(v.value))
        }
      }

      withRemoteCache[String, InternalCacheEntry]("storeCache") { remoteCache =>
        remoteCache should have size (keysValues.size)

        remoteCache.keySet should have size (1)
      }
    }
  }

  describe("remote-cache-store as raw spec") {
    it("saved raw data") {
      val clusterSize = 3
      val keysValues = (1 to 5).map(i => (s"key$i", new Entity(s"value$i")))

      withCache(clusterSize, "remoteStoreCacheAsRaw") { cache =>
        keysValues.foreach { case (k, v) => cache.put(k, v) }

        keysValues.foreach { case (k, v) =>
          cache.get(k) should be (new Entity(v.value))
        } 
      }

      withCache(clusterSize, "remoteStoreCacheAsRaw") { cache =>
        keysValues.foreach { case (k, v) =>
          cache.get(k) should be (new Entity(v.value))
        }
      }

      withRemoteCache[String, Entity]("storeCacheAsRaw") { remoteCache =>
        remoteCache should have size (keysValues.size)

        keysValues.foreach { case (k, v) =>
          remoteCache.get(k) should be (new Entity(v.value))
        }
      }
    }
  }

  def withCache(numInstances: Int, cacheName: String)(fun: Cache[String, Entity] => Unit): Unit = {
    val managers = (1 to numInstances).map(_ => new DefaultCacheManager("infinispan.xml"))
    val cache = managers.head.getCache[String, Entity](cacheName)

    try {
      fun(cache)
    } finally {
      for {
        manager <- managers
        cacheName <- manager.getCacheNames.asScala
      } {
        manager.getCache[Any, Any](cacheName).stop()
      }

      managers.foreach(_.stop())
    }
  }

  def withRemoteCache[K, V](cacheName: String)(fun: RemoteCache[K, V] => Unit): Unit = {
    val manager =
      new RemoteCacheManager(
        new ConfigurationBuilder()
          .addServer
          .host("localhost")
          .port(11222)
          .host("localhost")
          .port(12222)
          .build
        )

    try {
      val cache = manager.getCache[K, V](cacheName)
      fun(cache)
    } finally {
      manager.stop()
    }
  }
}

@SerialVersionUID(1L)
class Entity(val value: String) extends Serializable {
  override def equals(other: Any): Boolean =
    other match {
      case o: Entity => value == o.value
      case _ => false
    }

  override def hashCode: Int =
    Objects.hash(value)
}
