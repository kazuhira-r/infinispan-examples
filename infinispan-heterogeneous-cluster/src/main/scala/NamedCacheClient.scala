import scala.collection.JavaConverters._

import scala.io.StdIn

import org.infinispan.distribution.ch.DefaultConsistentHash
import org.infinispan.manager.DefaultCacheManager

object NamedCacheClient {
  def main(args: Array[String]): Unit = {
    val manager = new DefaultCacheManager("infinispan.xml")

    try {
      val cache = manager.getCache[String, String]("heterogeneous-cache")
      val advancedCache = cache.getAdvancedCache
      val dm = advancedCache.getDistributionManager
      val rpc = advancedCache.getRpcManager

      val (keys, values) =
        (1 to 10).map { i => (s"key$i", s"value$i") }.unzip

      Iterator
        .continually(StdIn.readLine())
        .takeWhile(_ != "exit")
        .withFilter(l => l != null & !l.isEmpty)
        .foreach { command =>
          command.split("\\s").toList match {
            case "put-all" :: Nil =>
              // データ登録
              println("  [Put Cache Value]")
              keys.zip(values).foreach { case (k, v) =>
                println(s"  Key:$k => $v")
                cache.put(k, v)
              }
            case "get" :: key :: Nil =>
              // 特定のキーの値を取得
              println("  [Get]")
              println(s"  Key:$key => ${cache.get(key)}")
            case "get-all" :: Nil =>
              // すべてのキーの値を取得
              println("  [Get All]")
              keys foreach { k =>
                println(s"  Key:$k => ${cache.get(k)}")
              }
            case "clear" :: Nil =>
              // キャッシュクリア
              println("  [Clear]")
              cache.clear()
            case "members" :: Nil =>
              // クラスタのメンバー一覧を表示
              println("  [Cluster Members]")
              rpc.getMembers.asScala.foreach(m => println(s"  $m"))
            case "self" :: Nil =>
              // 自分自身を表示
              println("  [Self]")
              println(s"  ${manager.getAddress}")
            case "locate" :: Nil =>
              println("  [Entry Locate]")
              keys.foreach { key =>
                println(s"  Key:$key, PrimaryLocation => ${dm.getPrimaryLocation(key)} , Locate => ${dm.locate(key)}")
              }
            case "capacity-factors" :: Nil =>
              val ch =
                advancedCache
                  .getDistributionManager
                  .getConsistentHash
                  .asInstanceOf[org.infinispan.distribution.ch.DefaultConsistentHash]
              println("  [Capacity Factors]")
              println(s"  ${ch.getCapacityFactors}")
            case unknown => println(s"Unkwon Command[$unknown]")
          }
        }

      println("Exit NamedCacheClient.")

      cache.stop()
    } finally {
      manager.stop()
    }
  }
}
