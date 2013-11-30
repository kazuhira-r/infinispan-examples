import org.infinispan.manager.DefaultCacheManager

object EmbeddedClient {
  def main(args: Array[String]): Unit = {
    val manager = new DefaultCacheManager
    val cache = manager.getCache[String, Integer]

    println("put-1 return => " + cache.put("key1", 1))
    println("put-1 => " + cache.get("key1"))

    println("put-2 return => " + cache.put("key1", 2))
    println("put-2 => " + cache.get("key1"))

    println("remove return => " + cache.remove("key1"))
    println("remove => " + cache.get("key1"))

    cache.put("key1", 2)
    println("put-2-2 => " + cache.get("key1"))
    cache.remove("key1", 2)
    println("remove value => " + cache.get("key1"))

    cache.put("key1", 3)
    println("put-3 => " + cache.get("key1"))

    cache.remove("key1", 1)
    println("remove value, ng pattern => " + cache.get("key1"))

    cache.replace("key1", 3, 4)
    println("replace old, new => " + cache.get("key1"))

    cache.replace("key1", 3, 5)
    println("replace old, new, ng pattern => " + cache.get("key1"))

    cache.replace("key1", 6)
    println("replace => " + cache.get("key1"))
  }
}

