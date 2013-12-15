package org.littlewings.infinispan.querydsl

import scala.collection.JavaConverters._

import java.text.SimpleDateFormat

import org.infinispan.Cache
import org.infinispan.manager.DefaultCacheManager
import org.infinispan.query.Search
import org.infinispan.query.dsl.{Query, SortOrder}
// import org.infinispan.query.dsl.impl.AttributeCondition

import org.littlewings.infinispan.querydsl.entity.{Book, Author}

import org.scalatest.{BeforeAndAfterAll, FunSpec}
import org.scalatest.Matchers._

class BookQueryDslSpec extends FunSpec {
  val toDate = (dateString: String) => new SimpleDateFormat("yyyy/MM/dd").parse(dateString)

  val infinispanBook: Book =
    Book("978-1849518222",
         "Infinispan Data Grid Platform",
         "Making use of data grids for performance and scalability in Enterprise Java, using Infinispan from JBoss",
         3186,
         toDate("2012/06/30"),
         Author("Francesco Marchioni"), Author("Manik Surtani"))

  val hazelcastBook: Book =
    Book("978-1782167303",
         "Getting Started With Hazelcast",
         "An easy-to-follow and hands-on introduction to the highly scalable data distribution system, Hazelcast, and its advanced features.",
         4336,
         toDate("2013/08/27"),
         Author("Mat Johns"))

  val luceneBook: Book =
    Book("978-1933988177",
         "Lucene in Action",
         "New edition of top-selling book on the new version of Lucene the core open-source technology behind most full-text search and Intelligent Web applications.",
         5421,
         toDate("2010/06/30"),
         Author("Michael McCandless"), Author("Erik Hatcher"), Author("Otis Gospodnetic"))

  val books =
    Array(infinispanBook, hazelcastBook, luceneBook)

  describe("infinispan query dsl") {
    it("search simple in") {
      withCache[String, Book]("infinispan.xml", "bookCache") { cache =>
        books.foreach(book => cache.put(book.isbn, book))

        val searchManager = Search.getSearchManager(cache)
        val queryFactory = searchManager.getQueryFactory

        val query: Query =
          queryFactory
            .from(classOf[Book])
            .having("isbn")
            .in("978-1782167303")
            .toBuilder
            .build

        query.toString should include ("query=isbn:978-1782167303")
        query.list[Book] should have size 1
        query.list[Book].get(0) should be (hazelcastBook)
      }
    }

    it("search simple like") {
      withCache[String, Book]("infinispan.xml", "bookCache") { cache =>
        books.foreach(book => cache.put(book.isbn, book))

        val searchManager = Search.getSearchManager(cache)
        val queryFactory = searchManager.getQueryFactory

        val query: Query =
          queryFactory
            .from(classOf[Book])
            .having("summary")
            .like("%data%")
            .toBuilder
            .build

        query.toString should include ("query=summary:*data*")
        query.list[Book].asScala should contain theSameElementsAs (Array(infinispanBook,
                                                                         hazelcastBook))
      }
    }

    it("search price range") {
      withCache[String, Book]("infinispan.xml", "bookCache") { cache =>
        books.foreach(book => cache.put(book.isbn, book))

        val searchManager = Search.getSearchManager(cache)
        val queryFactory = searchManager.getQueryFactory

        val query: Query =
          queryFactory
            .from(classOf[Book])
            .orderBy("price", SortOrder.ASC)
            .having("price")
            .between(3000, 4500)
            .toBuilder
            .build

        query.toString should include ("query=price:[3000 TO 4500]")
        query.list[Book].asScala should contain theSameElementsInOrderAs (Array(infinispanBook,
                                                                                hazelcastBook))
      }
    }

    it("search date gte lte") {
      withCache[String, Book]("infinispan.xml", "bookCache") { cache =>
        books.foreach(book => cache.put(book.isbn, book))

        val searchManager = Search.getSearchManager(cache)
        val queryFactory = searchManager.getQueryFactory

        val query: Query =
          queryFactory
            .from(classOf[Book])
            .orderBy("price", SortOrder.ASC)
            .having("publisherDate")
            .gte(toDate("2012/01/01"))
            .and
            .having("publisherDate")
            .lte(toDate("2013/12/31"))
            .toBuilder
            .build

        query.toString should include ("query=+publisherDate:[20111231 TO *] +publisherDate:[* TO 20131230]")
        query.list[Book].asScala should contain theSameElementsInOrderAs (Array(infinispanBook,
                                                                                hazelcastBook))
      }
    }

    it("search like and in") {
      withCache[String, Book]("infinispan.xml", "bookCache") { cache =>
        books.foreach(book => cache.put(book.isbn, book))

        val searchManager = Search.getSearchManager(cache)
        val queryFactory = searchManager.getQueryFactory

        val query: Query =
          queryFactory
            .from(classOf[Book])
            .having("summary")
            .like("%data%")
            .and
            .having("title")
            .like("%Infinispan%")
            .toBuilder
            .build

        query.toString should include ("query=+summary:*data* +title:*Infinispan*")
        query.list[Book].asScala should contain theSameElementsAs (Array(infinispanBook))
      }
    }

    it("search wildcard and sort") {
      withCache[String, Book]("infinispan.xml", "bookCache") { cache =>
        books.foreach(book => cache.put(book.isbn, book))

        val searchManager = Search.getSearchManager(cache)
        val queryFactory = searchManager.getQueryFactory

        val query: Query =
          queryFactory
            .from(classOf[Book])
            .orderBy("price", SortOrder.DESC)
            .having("isbn")
            .like("*")
            .toBuilder
            .build

        query.toString should include ("query=isbn:*")
        query.list[Book].asScala should contain theSameElementsInOrderAs (Array(luceneBook,
                                                                                hazelcastBook,
                                                                                infinispanBook))
      }
    }

    it("search embedded entity") {
      withCache[String, Book]("infinispan.xml", "bookCache") { cache =>
        books.foreach(book => cache.put(book.isbn, book))

        val searchManager = Search.getSearchManager(cache)
        val queryFactory = searchManager.getQueryFactory

        val query: Query =
          queryFactory
            .from(classOf[Book])
            .having("authorsAsJava.name")
            .like("Manik%")
            .toBuilder
            .build

        query.toString should include ("query=authorsAsJava.name:Manik*")
        query.list should have size 1
        query.list[Book].asScala.apply(0) should be (infinispanBook)
      }
    }

    it("search nested condition") {
      withCache[String, Book]("infinispan.xml", "bookCache") { cache =>
        books.foreach(book => cache.put(book.isbn, book))

        val searchManager = Search.getSearchManager(cache)
        val queryFactory = searchManager.getQueryFactory

        val query: Query =
          queryFactory
            .from(classOf[Book])
            .orderBy("price", SortOrder.ASC)
            .having("summary")
            .like("%data%")
            .and(queryFactory
                  .having("title")
                  .like("%Infinispan%")
                  .or
                  .having("title")
                  .like("%Hazelcast%"))
            .toBuilder
            .build


        query.toString should include ("query=+summary:*data* +(title:*Infinispan* title:*Hazelcast*)")
        query.list[Book].asScala should contain theSameElementsInOrderAs (Array(infinispanBook,
                                                                                hazelcastBook))
      }
    }

    it("search projection") {
      withCache[String, Book]("infinispan.xml", "bookCache") { cache =>
        books.foreach(book => cache.put(book.isbn, book))

        val searchManager = Search.getSearchManager(cache)
        val queryFactory = searchManager.getQueryFactory

        val query: Query =
          queryFactory
            .from(classOf[Book])
            .setProjection("isbn", "title")
            .having("isbn")
            .like("978-1849518222")
            .toBuilder
            .build

        query.toString should include ("query=isbn:978-1849518222")
        query.list should have size 1
        // Projectionの場合、Objectの配列が返ってくる
        query
          .list[Any]
          .asScala
          .apply(0)
          .asInstanceOf[Array[Any]] should contain theSameElementsInOrderAs (Array(infinispanBook.isbn,
                                                                                   infinispanBook.title))
      }
    }
  }

  def withCache[K, V](fileName: String, cacheName: String)(fun: Cache[K, V] => Unit): Unit = {
    val manager = new DefaultCacheManager(fileName)
    try {
      fun(manager.getCache[K, V](cacheName))
    } finally {
      manager.stop()
    }
  }
}
