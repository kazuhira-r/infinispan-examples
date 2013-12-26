package org.littlewings.infinispan.query

import scala.collection.JavaConverters._

import java.text.SimpleDateFormat

import org.infinispan.Cache
import org.infinispan.manager.DefaultCacheManager
import org.infinispan.query.Search

import org.scalatest.FunSpec
import org.scalatest.Matchers._

import org.littlewings.infinispan.query.entity.Book

class InfinispanQuerySpec extends FunSpec {
  val toDate = (dateString: String) => new SimpleDateFormat("yyyy/MM/dd").parse(dateString)

  val luceneBook: Book =
    Book("978-4774127804",
         "Apache Lucene 入門 ～Java・オープンソース・全文検索システムの構築",
         "Luceneは全文検索システムを構築するためのJavaのライブラリです。",
         3360,
         toDate("2006/05/17"))

  val solrBook: Book =
    Book("978-4774161631",
         "[改訂新版] Apache Solr入門 オープンソース全文検索エンジン",
         "最新版Apaceh Solr Ver.4.5.1に対応するため大幅な書き直しと原稿の追加を行い、現在の開発環境に合わせて完全にアップデートしました。Apache Solrは多様なプログラミング言語に対応した全文検索エンジンです。",
         3780,
         toDate("2013/11/29"))

  val collectiveIntelligenceInActionBook: Book =
    Book("978-4797352009",
         "集合知イン・アクション",
         "レコメンデーションエンジンをつくるには?ブログやSNSのテキスト分析、ユーザー嗜好の予測モデル、レコメンデーションエンジン……Web 2.0の鍵「集合知」をJavaで実装しよう!",
         3990,
         toDate("2009/03/27"))

  val books: Array[Book] = Array(luceneBook, solrBook, collectiveIntelligenceInActionBook)

  describe("infinispan query spec") {
    it("keyword query") {
      withCache { cache =>
        books.foreach(book => cache.put(book.isbn, book))

        val searchManager = Search.getSearchManager(cache)
        val queryBuilder = searchManager.buildQueryBuilderForClass(classOf[Book]).get

        val luceneQuery =
          queryBuilder
            .keyword
            .onField("title")
            .andField("summary")
            .matching("オープンソース 全文検索システムの構築")
            .createQuery

        luceneQuery.toString should be ("(title:オープン title:ソース title:全文 title:検索 title:システム title:構築) (summary:オープン summary:ソース summary:全文 summary:検索 summary:システム summary:構築)")

        val query = searchManager.getQuery(luceneQuery, classOf[Book])

        val result = query.list

        result should have size 2
        result.get(0) should be (luceneBook)
        result.get(1) should be (solrBook)
      }
    }

    it("phrase query") {
      withCache { cache =>
        books.foreach(book => cache.put(book.isbn, book))

        val searchManager = Search.getSearchManager(cache)
        val queryBuilder = searchManager.buildQueryBuilderForClass(classOf[Book]).get

        val luceneQuery =
          queryBuilder
            .phrase
            .onField("title")
            .andField("summary")
            .sentence("オープンソース 全文検索システムの構築")
            .createQuery

        luceneQuery.toString should be ("title:\"オープン ソース 全文 検索 システム ? 構築\" summary:\"オープン ソース 全文 検索 システム ? 構築\"")

        val query = searchManager.getQuery(luceneQuery, classOf[Book])

        val result = query.list

        result should have size 1
        result.get(0) should be (luceneBook)
      }
    }

    it("range query") {
      withCache { cache =>
        books.foreach(book => cache.put(book.isbn, book))

        val searchManager = Search.getSearchManager(cache)
        val queryBuilder = searchManager.buildQueryBuilderForClass(classOf[Book]).get

        val luceneQuery =
          queryBuilder
            .range
            .onField("price")
            .from(3500)
            .to(4000)
            .createQuery

        luceneQuery.toString should be ("price:[3500 TO 4000]")

        val query = searchManager.getQuery(luceneQuery, classOf[Book])

        val result = query.list

        result should have size 2
        result.get(0) should be (solrBook)
        result.get(1) should be (collectiveIntelligenceInActionBook)
      }
    }

    it("bool query") {
      withCache { cache =>
        books.foreach(book => cache.put(book.isbn, book))

        val searchManager = Search.getSearchManager(cache)
        val queryBuilder = searchManager.buildQueryBuilderForClass(classOf[Book]).get

        val luceneQuery =
          queryBuilder
            .bool
              .should {
                queryBuilder
                  .keyword
                  .onField("title")
                  .matching("全文検索")
                  .createQuery
              }.should {
                queryBuilder
                  .keyword
                  .onField("summary")
                  .matching("java")
                  .createQuery
              }.createQuery

        luceneQuery.toString should be ("(title:全文 title:検索) summary:java")

        val query = searchManager.getQuery(luceneQuery, classOf[Book])

        val result = query.list

        result should have size 3
        result.get(0) should be (luceneBook)
        result.get(1) should be (solrBook)
        result.get(2) should be (collectiveIntelligenceInActionBook)
      }
    }

    it("clustered spec") {
      withCache { _ =>
        withCache { cache =>
          books.foreach(book => cache.put(book.isbn, book))
        }

        withCache { cache =>
          val searchManager = Search.getSearchManager(cache)
          val queryBuilder = searchManager.buildQueryBuilderForClass(classOf[Book]).get

          val luceneQuery =
            queryBuilder
              .keyword
              .onField("title")
              .andField("summary")
              .matching("オープンソース 全文検索システムの構築")
              .createQuery

          luceneQuery.toString should be ("(title:オープン title:ソース title:全文 title:検索 title:システム title:構築) (summary:オープン summary:ソース summary:全文 summary:検索 summary:システム summary:構築)")

          val query = searchManager.getQuery(luceneQuery, classOf[Book])

          val result = query.list

          result should have size 2
          result.get(0) should be (luceneBook)
          result.get(1) should be (solrBook)

          cache.getCacheManager.getCacheNames should contain theSameElementsAs(Array("bookCache",
                                                                                     "My-LuceneIndexesLocking",
                                                                                     "My-LuceneIndexesData",
                                                                                     "My-LuceneIndexesMetadata",
                                                                                     "__cluster_registry_cache__"))
        }
      }
    }
  }

  def withCache(fun: Cache[String, Book] => Unit): Unit = {
    val manager = new DefaultCacheManager("infinispan.xml")

    try {
      val cache = manager.getCache[String, Book]("bookCache")
      try {
        fun(cache)
      } finally {
        cache.stop()
      }
    } finally {
      manager.stop()
    }
  }
}
