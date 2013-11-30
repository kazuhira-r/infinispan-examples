import javax.ws.rs.client.{ClientBuilder, ClientRequestFilter, Entity}
import javax.ws.rs.core.{MediaType, Response}

import org.jboss.resteasy.client.jaxrs.BasicAuthentication

object InfinispanRestClient {
  def main(args: Array[String]): Unit = {
    val cacheName = "namedCache"
    val range = 1 to 10

    val client = ClientBuilder.newBuilder.build
    client.register(new BasicAuthentication("username", "password$1"), classOf[ClientRequestFilter])

    // データの登録／更新
    range.foreach { i =>
      val (key, entity) = (s"key$i", Entity.text(s"value$i"))
      
      val response =
        client
          .target(s"http://localhost:8080/rest/$cacheName/$key")
          .request
          .put(entity)  // postでもOK
      println(s"Put $key Status => ${response.getStatus}")
      response.close()
    }

    // データの取得
    // キーの存在確認だけなら、headを使うみたい
    range.foreach { i =>
      val key = s"key$i"

      val response =
        client
          .target(s"http://localhost:8080/rest/$cacheName/$key")
          .request
          .get
      println(s"Get $key Status => ${response.getStatus}")
      println(s"Get Value Key[$key] = ${response.readEntity(classOf[String])}")
      response.close()
    }

    // 存在しないキーに対してアクセス
    {
      val key = "notFoundKey"

      val response =
        client
          .target(s"http://localhost:8080/rest/$cacheName/$key")
          .request
          .get

      // 存在しないキーに対しては、404が返る
      require(response.getStatus == Response.Status.NOT_FOUND.getStatusCode)
      response.close()
    }

    // 存在しないCacheに対してアクセス
    {
      val response =
        client
          .target(s"http://localhost:8080/rest/notExistCache")
          .request
          .get

      // 存在しないCacheに対しては、404が返る
      require(response.getStatus == Response.Status.NOT_FOUND.getStatusCode)
      response.close()      
    }

    // キー一覧の取得
    {
      val response =
        client
          .target(s"http://localhost:8080/rest/$cacheName")
          // .request(MediaType.TEXT_HTML_TYPE)  // デフォルト
          .request(MediaType.APPLICATION_JSON_TYPE)
          // .request(MediaType.APPLICATION_XML_TYPE)
          // .request(MediaType.TEXT_PLAIN_TYPE)
          // .request(MediaType.WILDCARD_TYPE)  // TEXT_HTML_TYPEと同じ
          .get
      println(s"Get $cacheName Status => ${response.getStatus}")
      println(s"Get $cacheName Value = ${response.readEntity(classOf[String])}")
      response.close()
    }

    // Cacheから特定キーのデータを削除
    range.withFilter(_ % 2 == 0).foreach { i =>
      val key = s"key$i"

      val response =
        client
          .target(s"http://localhost:8080/rest/$cacheName/$key")
          .request
          .delete

      println(s"Delete $key Status => ${response.getStatus}")
      response.close()
    }

    // 削除後のデータの確認
    {
      val response =
        client
          .target(s"http://localhost:8080/rest/$cacheName")
          .request(MediaType.TEXT_PLAIN_TYPE)
          .get
      require(response.readEntity(classOf[String]).split("""\s+""").size == 5)
      response.close()
    }

    // Cacheの全データを削除
    {
      val response =
        client
          .target(s"http://localhost:8080/rest/$cacheName")
          .request
          .delete
      println(s"Delete $cacheName Status => ${response.getStatus}")
      response.close()
    }

    // 削除後のデータの確認
    {
      val response =
        client
          .target(s"http://localhost:8080/rest/$cacheName")
          .request(MediaType.TEXT_PLAIN_TYPE)
          .get
      require(response.readEntity(classOf[String]).isEmpty)
      response.close()
    }

    // Clientのクローズ
    client.close()
  }
}
