import java.util.Objects

import org.infinispan.distribution.group.{Group, Grouper}
import org.infinispan.manager.DefaultCacheManager

object InfinispanGroupApi {
  def main(args: Array[String]): Unit = {
    val manager = new DefaultCacheManager("infinispan.xml")
    val cacheAsCustomizedKeyClass = manager.getCache[KeyClass, KeyClass]("cacheAsCustomizedKeyClass")
    val cacheAsGrouper = manager.getCache[NoGroupKeyClass, NoGroupKeyClass]("cacheAsGrouper")
    val cacheAsGrouperSimple = manager.getCache[String, String]("cacheAsGrouperSimple")

    try {
      val groupKeyValues =
        Array(KeyClass("10000", "Sato", 20),
              KeyClass("10000", "Tanaka", 25),
              KeyClass("10000", "Suzuki", 30),
              KeyClass("20000", "Momimoto", 22),
              KeyClass("20000", "Hanada", 27),
              KeyClass("20000", "Yamamoto", 19),
              KeyClass("30000", "Ken", 22),
              KeyClass("30000", "Mike", 23),
              KeyClass("30000", "Jusmine", 21),
              KeyClass("40000", "hoge", 20),
              KeyClass("40000", "foo", 20),
              KeyClass("40000", "bar", 20),
              KeyClass("50000", "Java", 18),
              KeyClass("50000", "Scala", 10),
              KeyClass("50000", "Clojure", 6)
            )

      for (keyValue <- groupKeyValues)
        cacheAsCustomizedKeyClass.put(keyValue, keyValue)

      for (keyValue <- groupKeyValues) {
        val dm = cacheAsCustomizedKeyClass.getAdvancedCache.getDistributionManager
        println(s"PrimaryLocation: ${dm.getPrimaryLocation(keyValue)}, Locate:${dm.locate(keyValue)}")
        println(s"  $keyValue:${cacheAsCustomizedKeyClass.get(keyValue)}")
      }

      println("====================")

      val keyValues =
        Array(NoGroupKeyClass("10000", "Sato", 20),
              NoGroupKeyClass("10000", "Tanaka", 25),
              NoGroupKeyClass("10000", "Suzuki", 30),
              NoGroupKeyClass("20000", "Momimoto", 22),
              NoGroupKeyClass("20000", "Hanada", 27),
              NoGroupKeyClass("20000", "Yamamoto", 19),
              NoGroupKeyClass("30000", "Ken", 22),
              NoGroupKeyClass("30000", "Mike", 23),
              NoGroupKeyClass("30000", "Jusmine", 21),
              NoGroupKeyClass("40000", "hoge", 20),
              NoGroupKeyClass("40000", "foo", 20),
              NoGroupKeyClass("40000", "bar", 20),
              NoGroupKeyClass("50000", "Java", 18),
              NoGroupKeyClass("50000", "Scala", 10),
              NoGroupKeyClass("50000", "Clojure", 6)
            )

      for (keyValue <- keyValues)
        cacheAsGrouper.put(keyValue, keyValue)

      for (keyValue <- keyValues) {
        val dm = cacheAsGrouper.getAdvancedCache.getDistributionManager
        println(s"PrimaryLocation: ${dm.getPrimaryLocation(keyValue)}, Locate:${dm.locate(keyValue)}")
        println(s"  $keyValue:${cacheAsGrouper.get(keyValue)}")
      }

      println("====================")

      val simpleKeyValues =
        Array(("10001", "Sato"),
              ("10002", "Tanaka"),
              ("10003", "Suzuki"),
              ("20001", "Momimoto"),
              ("20002", "Hanada"),
              ("20003", "Yamamoto"),
              ("30001", "Ken"),
              ("30002", "Mike"),
              ("30003", "Jusmine"),
              ("40001", "hoge"),
              ("40002", "foo"),
              ("40003", "bar"),
              ("50001", "Java"),
              ("50002", "Scala"),
              ("50003", "Clojure")
            )

      for ((key, value) <- simpleKeyValues)
        cacheAsGrouperSimple.put(key, value)

      for ((key, _) <- simpleKeyValues) {
        val dm = cacheAsGrouperSimple.getAdvancedCache.getDistributionManager
        println(s"PrimaryLocation: ${dm.getPrimaryLocation(key)}, Locate:${dm.locate(key)}")
        println(s"  $key:${cacheAsGrouperSimple.get(key)}")
      }
    } finally {
      cacheAsCustomizedKeyClass.stop()
      cacheAsGrouper.stop()
      cacheAsGrouperSimple.stop()
      manager.stop()
    }
  }
}

/** 自分でGroup制御を行うキークラス **/
object KeyClass {
  def apply(code: String, name: String, age: Int): KeyClass =
    new KeyClass(code, name, age)
}

@SerialVersionUID(1L)
class KeyClass(val code: String, val name: String, val age: Int) extends Serializable {
  @Group
  def getCode: String = code

  override def equals(other: Any): Boolean = other match {
    case o: KeyClass => code == o.code && name == o.name && age == o.age
    case _ => false
  }

  override def hashCode: Int = Objects.hashCode(code, name, age)

  override def toString: String =
    s"KeyClass => code:[$code], name:[$name], age:[$age]"
}

/** 外部のGrouperでGroup制御を行うキークラス **/
object NoGroupKeyClass {
  def apply(code: String, name: String, age: Int): NoGroupKeyClass =
    new NoGroupKeyClass(code, name, age)
}

@SerialVersionUID(1L)
class NoGroupKeyClass(val code: String, val name: String, val age: Int) extends Serializable {
  override def equals(other: Any): Boolean = other match {
    case o: NoGroupKeyClass => code == o.code && name == o.name && age == o.age
    case _ => false
  }

  override def hashCode: Int = Objects.hashCode(code, name, age)

  override def toString: String =
    s"NoGroupKey => code:[$code], name:[$name], age:[$age]"
}

class MyGrouper extends Grouper[NoGroupKeyClass] {
  def computeGroup(key: NoGroupKeyClass, group: String): String = key.code
  def getKeyType: Class[NoGroupKeyClass] = classOf[NoGroupKeyClass]
}

class SimpleGrouper extends Grouper[String] {
  def computeGroup(key: String, group: String): String = key.head.toString
  def getKeyType: Class[String] = classOf[String]
}
