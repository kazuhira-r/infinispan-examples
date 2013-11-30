import scala.collection.JavaConverters._

import org.infinispan.manager.DefaultCacheManager
import org.infinispan.distexec.mapreduce.{Collator, Collector, Mapper, MapReduceTask, Reducer}

object MapReduceExample {
  def main(args: Array[String]): Unit = {
    val manager = new DefaultCacheManager("infinispan.xml")
    val cache = manager.getCache[String, String]()

    cache.put("1", "Hello world here I am")
    cache.put("2", "Infinispan rules the world")
    cache.put("3", "JUDCon is in Boston")
    cache.put("4", "JBoss World is in Boston as well")
    cache.put("12","JBoss Application Server")
    cache.put("15", "Hello world")
    cache.put("14", "Infinispan community")
    cache.put("15", "Hello world")
    
    cache.put("111", "Infinispan open source")
    cache.put("112", "Boston is close to Toronto")
    cache.put("113", "Toronto is a capital of Ontario")
    cache.put("114", "JUDCon is cool")
    cache.put("211", "JBoss World is awesome")
    cache.put("212", "JBoss rules")
    cache.put("213", "JBoss division of RedHat ")
    cache.put("214", "RedHat community")

    val task = new MapReduceTask[String, String, String, Int](cache, true, true)
    val ranking =
      task.mappedWith(new WordCountMapper)
        .reducedWith(new WordCountReducer)
        .execute(new WordCountCollator)
        //.execute

    /*
    println("----- [MapReduceResult] START -----")
    map.asScala.foreach { case (k, v) => println(s"Key[$k] = $v") }
    println("----- [MapReduceResult] END -----")
    */

    ranking.foreach(println)

    cache.stop()
    manager.stop()
  }
}

@SerialVersionUID(1L)
class WordCountMapper extends Mapper[String, String, String, Int] {
  def map(key: String, value: String, c: Collector[String, Int]): Unit = {
    println(s"[Mapper] input key => [$key], input value => [$value]")
    """\s+""".r.split(value).foreach(c.emit(_, 1))
  }
}

@SerialVersionUID(1L)
class WordCountReducer extends Reducer[String, Int] {
  def reduce(key: String, iter: java.util.Iterator[Int]): Int = {
    println(s"[Reducer] input key => [$key]")
    iter.asScala.sum
  }
}

class WordCountCollator extends Collator[String, Int, List[(String, Int)]] {
  def collate(reducedResults: java.util.Map[String, Int]): List[(String, Int)] =
    reducedResults
      .asScala
      .toSeq
      .sortWith {
        case (kv1, kv2) =>
          kv1._2.compareTo(kv2._2) match {
            case 0 => kv1._1.toLowerCase < kv2._1.toLowerCase
            case n if n > 0 => true
            case n if n < 0 => false
          }
      }
      .toList
}
