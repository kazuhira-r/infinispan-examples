package org.littlewings.infinispan.remotequery

import javax.management.{MBeanServerFactory, ObjectName}
import javax.management.remote.{JMXConnector, JMXConnectorFactory, JMXServiceURL}

import org.infinispan.client.hotrod.{RemoteCache, RemoteCacheManager, Search}
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder
import org.infinispan.client.hotrod.impl.ConfigurationProperties
import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller
import org.infinispan.query.dsl.{Query, SortOrder}
import org.infinispan.query.remote.ProtobufMetadataManager
import org.infinispan.commons.util.Util

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class RemoteQuerySpec extends FunSpec {
  val javaee6Book: Book = Book("978-4798124605",
                               "Beginning Java EE 6 GlassFish 3で始めるエンタープライズJava",
                               4410,
                               "エンタープライズJava入門書の決定版！Java EE 6は、大規模な情報システム構築に用いられるエンタープライズ環境向けのプログラミング言語です。")

  val javaee5Book: Book = Book("978-4798120546",
                               "マスタリングJavaEE5 第2版",
                               5670,
                               "EJB3.0、JPA、JSF、Webサービスを完全網羅。新たにJBoss AS、Hibernateにも対応!JavaEE5は、J2EEの高い機能性はそのままに、アプリケーションの開発生産性を高めることを主眼とした、サーバサイドJavaにおけるプラットフォーム、開発、デプロイメントに関する標準仕様です。")

  val jaxrsBook: Book = Book("978-4873114675",
                             "JavaによるRESTfulシステム構築",
                             3360,
                             "Java EE 6でサポートされたJAX-RSの特徴とRESTfulアーキテクチャ原則を使って、Javaでの分散Webサービスを設計開発する方法を学ぶ書籍。")

  val luceneBook: Book = Book("978-4774127804",
                              "Apache Lucene 入門 Java・オープンソース・全文検索システムの構築",
                              3360,
                              "Luceneは全文検索システムを構築するためのJavaのライブラリです。Luceneを使えば,一味違う高機能なWebアプリケーションを作ることができます。")

  val solrBook: Book = Book("978-4774161631",
                            "[改訂新版] Apache Solr入門 オープンソース全文検索エンジン",
                            3780,
                            "最新版Apaceh Solr Ver.4.5.1に対応するため大幅な書き直しと原稿の追加を行い、現在の開発環境に合わせて完全にアップデートしました。Apache Solrは多様なプログラミング言語に対応した全文検索エンジンです。")

  val books: Array[Book] = Array(javaee6Book,
                                 javaee5Book,
                                 jaxrsBook,
                                 luceneBook,
                                 solrBook)

  describe("remote query spec") {
    it("clear data") {
      withCache { cache =>
        cache.clear
      }
    }

    it("put data") {
      withCache { cache =>
        registerProtofileToServer("/book.protobin")

        val context =
          ProtoStreamMarshaller.getSerializationContext(cache.getRemoteCacheManager)

        context.registerProtofile("/book.protobin")
        context.registerMarshaller(classOf[Book], new BookMarshaller)

        books.foreach(b => cache.put(b.isbn, b))
      }
    }

    it("get data") {
      withCache { cache =>
        registerProtofileToServer("/book.protobin")

        val context =
          ProtoStreamMarshaller.getSerializationContext(cache.getRemoteCacheManager)

        context.registerProtofile("/book.protobin")
        context.registerMarshaller(classOf[Book], new BookMarshaller)

        cache.get(solrBook.isbn) should be (solrBook)
      }
    }

    it("query #1") {
      withCache { cache =>
        registerProtofileToServer("/book.protobin")

        val context =
          ProtoStreamMarshaller.getSerializationContext(cache.getRemoteCacheManager)

        context.registerProtofile("/book.protobin")
        context.registerMarshaller(classOf[Book], new BookMarshaller)

        val queryFactory = Search.getQueryFactory(cache)

        val query: Query =
          queryFactory
            .from(classOf[Book])
            .having("isbn")
            .in("978-4798124605")
            .toBuilder
            .build

        val books = query.list[Book]
        books should have size 1
        books.get(0) should be (javaee6Book)
      }
    }

    it("query #2") {
      withCache { cache =>
        registerProtofileToServer("/book.protobin")

        val context =
          ProtoStreamMarshaller.getSerializationContext(cache.getRemoteCacheManager)

        context.registerProtofile("/book.protobin")
        context.registerMarshaller(classOf[Book], new BookMarshaller)

        val queryFactory = Search.getQueryFactory(cache)

        val query: Query =
          queryFactory
            .from(classOf[Book])
            .having("summary")
            .like("%Lucene%")
            .toBuilder
            .build

        val books = query.list[Book]
        books should have size 1
        books.get(0) should be (luceneBook)
      }
    }

    it("query #3") {
      withCache { cache =>
        registerProtofileToServer("/book.protobin")

        val context =
          ProtoStreamMarshaller.getSerializationContext(cache.getRemoteCacheManager)

        context.registerProtofile("/book.protobin")
        context.registerMarshaller(classOf[Book], new BookMarshaller)

        val queryFactory = Search.getQueryFactory(cache)

        val query: Query =
          queryFactory
            .from(classOf[Book])
            .orderBy("price", SortOrder.DESC)
            .having("title")
            .like("%Java%")
            .and
            .having("price")
            .between(3500, 6000)
            .toBuilder
            .build

        val books = query.list[Book]
        books should have size 2
        books should contain theSameElementsInOrderAs Array(javaee5Book, javaee6Book)
      }
    }
  }

  private def withCache(fun: RemoteCache[String, Book] => Unit): Unit = {
    val manager =
      new RemoteCacheManager(
        new ConfigurationBuilder()
          .addServer
          .host("localhost")
          .port(11222)
          .marshaller(new ProtoStreamMarshaller)
          .build
      )

    try {
      val cache = manager.getCache[String, Book]("indexingCache")
      fun(cache)
      cache.stop()
    } finally {
      manager.stop()
    }
  }

  private def registerProtofileToServer(filePath: String): Unit = {
    val is = getClass.getResourceAsStream(filePath)

    val descriptor = 
      try {
        Util.readStream(is)
      } finally {
        is.close()
      }

    val serviceUrl = new JMXServiceURL(s"service:jmx:remoting-jmx://localhost:9999")
    val jmxConnector = JMXConnectorFactory.connect(serviceUrl)

    try {
      val jmxConnection = jmxConnector.getMBeanServerConnection()

      val jmxDomain = "jboss.infinispan"
      val objectName =
        new ObjectName(s"$jmxDomain:type=RemoteQuery,name=${ObjectName.quote("local")},component=${ProtobufMetadataManager.OBJECT_NAME}")

      jmxConnection.invoke(objectName,
                           "registerProtofile",
                           Array[AnyRef](descriptor),
                           Array(classOf[Array[Byte]].getName))

    } finally {
      jmxConnector.close()
    }
  }
}
