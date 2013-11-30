import scala.util.Try

import org.infinispan.client.hotrod.{Flag, RemoteCacheManager, VersionedValue}
//import org.infinispan.client.hotrod.{RemoteCache, RemoteCacheManager}

import java.util.Properties

object HotRodClient {
  def main(args: Array[String]): Unit = {
    val properties = new Properties
    properties.put("infinispan.client.hotrod.server_list", "localhost:11222")
    properties.put("infinispan.client.hotrod.force_return_values", "true")

    val manager = new RemoteCacheManager(properties)
    //val manager = new RemoteCacheManager("localhost")
    val cache = manager.getCache[String, Integer]()

    println("put-1 return => " + cache.put("key1", 1))
    println("put-1 => " + cache.get("key1"))

    println("put-2 return => " + cache.put("key1", 2))
    println("put-2 => " + cache.get("key1"))

    println("remove return => " + cache.remove("key1"))
    println("remove => " + cache.get("key1"))

    cache.put("key1", 2)
    println("put-2-2 => " + cache.get("key1"))
    var version = cache.getVersioned("key1")
    println(s"current value => ${version.getValue}, current version ${version.getVersion}")
    cache.removeWithVersion("key1", version.getVersion)
    println("removeWithVersion value => " + cache.get("key1"))

    cache.put("key1", 3)
    println("put-3 => " + cache.get("key1"))

    version = cache.getVersioned("key1")
    println(s"current value => ${version.getValue}, current version ${version.getVersion}")
    cache.removeWithVersion("key1", 1)  // 適当なバージョンを指定
    println("removeWithVersion value, ng pattern => " + cache.get("key1"))

    version = cache.getVersioned("key1")
    println(s"current value => ${version.getValue}, current version ${version.getVersion}")
    cache.replaceWithVersion("key1", 4, version.getVersion)
    println("replaceWithVersion old, new => " + cache.get("key1"))

    version = cache.getVersioned("key1")
    println(s"current value => ${version.getValue}, current version ${version.getVersion}")
    cache.replaceWithVersion("key1", 5, 1)  // 適当なバージョンを指定
    println("replaceWithVersion old, new, ng pattern => " + cache.get("key1"))

    cache.replace("key1", 6)
    println("replace => " + cache.get("key1"))
  }
/*
  def main(args: Array[String]): Unit = {
    val manager = new RemoteCacheManager("localhost")
    val cache = manager.getCache[String, Integer]()

    cache.put("key1", 1)
    println("put-1 => " + cache.get("key1"))

    cache.put("key1", 2)
    println("put-2 => " + cache.get("key1"))

    cache.remove("key1")
    println("remove => " + cache.get("key1"))

    cache.put("key1", 2)
    println("put-2-2 => " + cache.get("key1"))
    println(Try { cache.remove("key1", 2) })
    println("remove value => " + cache.get("key1"))

    cache.put("key1", 3)
    println("put-3 => " + cache.get("key1"))

    println(Try { cache.remove("key1", 1) })
    println("remove value, ng pattern => " + cache.get("key1"))

    println(Try { cache.replace("key1", 3, 4) })
    println("replace old, new => " + cache.get("key1"))

    println(Try { cache.replace("key1", 3, 5) })
    println("replace old, new, ng pattern => " + cache.get("key1"))

    cache.replace("key1", 6)
    println("replace => " + cache.get("key1"))
  }
*/

/*
  def main(args: Array[String]): Unit = {
    val manager: RemoteCacheManager = new RemoteCacheManager("localhost:11222")
    // デフォルトポートを使うなら、以下でOK
    // val manager: RemoteCacheManager = new RemoteCacheManager("localhost")
    //val cache: RemoteCache[String, Integer] = manager.getCache("namedCache")
    val cache: RemoteCache[String, Integer] = manager.getCache()

    val keys = (1 to 3) map (i => s"key$i")

    keys foreach (k => println(s"key[$k] => value[${cache.get(k)}]"))

    keys foreach (k => cache.get(k) match {
      case null => cache.put(k, 1)
      case v => cache.put(k, v.toInt + 1)
    })

    keys foreach (k => println(s"key[$k] => value[${cache.get(k)}]"))
  }
*/
}
