import scala.collection.JavaConverters._

import java.util.{Calendar, Date}

import org.apache.lucene.analysis.cjk.CJKAnalyzer
import org.apache.lucene.queryParser.QueryParser
import org.apache.lucene.search.{Query, Sort, SortField}
import org.apache.lucene.util.Version

import org.hibernate.search.annotations.{DateBridge, Field, Indexed, IndexedEmbedded, Resolution}

import org.infinispan.Cache
import org.infinispan.query.{CacheQuery, Search}
import org.infinispan.manager.DefaultCacheManager

object QueryingExample {
  def main(args: Array[String]): Unit = {
    val manager = new DefaultCacheManager("infinispan.xml")
    val cache = manager.getCache[String, Book]()

    val luceneVersion = Version.LUCENE_36

    try {
      registerIndexData(cache)
      val queryParser = new QueryParser(luceneVersion, "authors.name", new CJKAnalyzer(luceneVersion)) 
      val query = queryParser.parse("odersky")
      //val queryParser = new QueryParser(luceneVersion, "title", new CJKAnalyzer(luceneVersion))
      //val query = queryParser.parse("(Lucene OR Scala) AND price:[3000 TO 4000]")
      //val sort = None
      val sort = Some(new Sort(new SortField("price", SortField.INT)))

      println(s"Query = $query")

      val results = search(cache, query, sort)

      println(s"Hits = ${results.size}")

      for (r <- results)
        println("Found --->" + System.lineSeparator + r)
    } finally {
      cache.stop()
      manager.stop()
    }
  }

  def registerIndexData(cache: Cache[String, Book]): Unit = {
    cache.put("1", Book(title = "Apache Lucene 入門 ～ Java・オープンソース・全文検索システムの構築",
                        description = """Luceneは全文検索システムを構築するためのJavaのライブラリです。
                        |Luceneを使えば,一味違う高機能なWebアプリケーションを作ることができます。
                        |""".stripMargin.replaceAll("""\r?\n""", ""),
                        price = 3360,
                        publisherYear = toDate(2006, 5, 17),
                        authors = Set(Author(name = "関口 宏司"))))
    cache.put("2", Book(title = "Lucene in Action",
                        description = """HIGHLIGHT New edition of top-selling book on the new version of Lucene
                        |--the core open-source technology behind most full-text search and 
                        |"Intelligent Web" applications. """.stripMargin.replaceAll("""\r?\n""", ""),
                        price = 4977,
                        publisherYear = toDate(2010, 6, 30),
                        authors = Set(
                          Author(name = "Michael McCandless"),
                          Author(name = "Erik Hatcher"),
                          Author(name = "Otis Gospodnetic"))))
    cache.put("3", Book(title = "Apache Solr入門 ー オープンソース全文検索エンジン",
                        description = """Apache Solrとは,オープンソースの検索エンジンです.
                        |Apache LuceneというJavaの全文検索システムをベースに豊富な拡張性をもたせ,
                        |多くの開発者が利用できるように作られました.""".stripMargin.replaceAll("""\r?\n""", ""),
                        price = 3680,
                        publisherYear = toDate(2010, 2, 20),
                        authors = Set(
                          Author(name = "関口 宏司"),
                          Author(name = "三部 靖夫"),
                          Author(name = "武田 光平"),
                          Author(name = "中野 猛"),
                          Author(name = "大谷 純"))))
    cache.put("4", Book(title = "Hibernate辞典 設定・マッピング・クエリ逆引きリファレンス",
                        description = """実践的なテクニックと豊富なサンプルで、利用上の悩みを解決!
                        |最新バージョン3.xに対応!""".stripMargin.replaceAll("""\r?\n""", ""),
                        price = 3200,
                        publisherYear = toDate(2008, 8, 7),
                          authors = Set(
                            Author("船木 健児"),
                            Author("三田 淳一"),
                            Author("佐藤 竜一"))))
    cache.put("5", Book(title = "Hibernate (開発者ノートシリーズ)",
                        description = """Javaプログラムからデータベースを利用する際に便利なのが，
                        |O／R（Object-Relational）マッピング・ツールである。O／Rマッピング・ツールを利用すると，
                        |データベースに格納してある表形式のデータをオブジェクトとして取り扱える。""".stripMargin.replaceAll("""\r?\n""", ""),
                        price = 2520,
                        publisherYear = toDate(2004, 12, 1),
                        authors = Set(
                          Author("James Elliott"),
                          Author("佐藤 直生"))))
    cache.put("6", Book(title = "Scalaスケーラブルプログラミング第2版",
                        description = "言語設計者自ら、その手法と思想を説くScalaプログラミングバイブル!",
                        price = 4830,
                        publisherYear = toDate(2011, 9, 27),
                        authors = Set(
                          Author("Martin Odersky"),
                          Author("Lex Spoon"),
                          Author("Bill Venners"),
                          Author("羽生田 栄一"),
                          Author("水島 宏太"),
                          Author("長尾 高弘"))))
    cache.put("7", Book(title = "Scala逆引きレシピ (PROGRAMMER’S RECiPE)",
                        description = """Scalaでコードを書く際の実践ノウハウが凝縮! 本書は、オブジェクト指向言語に
                        |関数型言語の特長をバランスよく取り込んだ、実用的なプログラミング言語「Scala(スカラ)」の
                        |逆引き解説書です。""".stripMargin.replaceAll("""\r?\n""", ""),
                        price = 3360,
                        publisherYear = toDate(2012, 7, 3),
                        authors = Set(
                          Author("竹添 直樹"),
                          Author("島本 多可子"))))
    cache.put("8", Book(title = "Scalaプログラミング入門",
                        description = """Scalaの生みの親、マーティン・オダースキー推薦!
                        |羽生田栄一解説「いまなぜScalaなのか」を掲載! """.stripMargin.replaceAll("""\r?\n""", ""),
                        price = 3360,
                        publisherYear = toDate(2010, 3, 18),
                        authors = Set(
                          Author("デイビッド・ポラック"),
                          Author("羽生田栄一"),
                          Author("大塚庸史"))))
    cache.put("9", Book(title = "プログラミングScala",
                        description = """プログラミング言語Scalaの解説書。
                        |Scala言語の基本的な機能やScala特有の設計について学ぶことができます。""".stripMargin.replaceAll("""\r?\n""", ""),
                        price = 3390,
                        publisherYear = toDate(2011, 1, 20),
                        authors = Set(
                          Author("Dean Wampler"),
                          Author("Alex Payne"),
                          Author("株式会社オージス総研 オブジェクトの広場編集部"))))
  }

  def search(cache: Cache[_, _], fullTextQuery: Query, sort: Option[Sort]): List[_]= {
    val searchManager = Search.getSearchManager(cache)
    val cacheQuery = searchManager.getQuery(fullTextQuery)

    sort.foreach(cacheQuery.sort)

    val results = cacheQuery.list
    results.asScala.toList
  }

  def toDate(year: Int, month: Int, day: Int): Date = {
    val calendar = Calendar.getInstance
    calendar.clear()
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, month - 1)
    calendar.set(Calendar.DATE, day)
    calendar.getTime
  }
}

object Book {
  def apply(title: String,
            description: String,
            price: Int,
            publisherYear: Date,
            authors: Set[Author]): Book = {
    val book = new Book
    book.title = title
    book.description = description
    book.price = price
    book.publisherYear = publisherYear
    book.authors = authors.asJava
    book
  }
}

@Indexed
class Book private {
  @Field
  var title: String = _
  @Field
  var description: String = _
  @Field
  var price: Int = _
  @Field
  @DateBridge(resolution = Resolution.YEAR)
  var publisherYear: Date = _
  @IndexedEmbedded
  var authors: java.util.Set[Author] = _

  override def toString: String =
    s"""Book[title = $title,
       |     price = $price,
       |     publisherYear = $publisherYear,
       |     authors = { ${authors.asScala.mkString(", ")} }]""".stripMargin
}

object Author {
  def apply(name: String, surname: String = null): Author = {
    val author = new Author
    author.name = name
    author.surname = surname
    author
  }
}

class Author private {
  @Field
  var name: String = _
  @Field
  var surname: String = _

  override def toString: String =
    s"Author[name = $name]"
}

class Hoge(val name: String) extends Serializable
