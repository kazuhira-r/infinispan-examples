package org.littlewings.infinispan.querydsl.entity

import scala.collection._
import scala.collection.JavaConverters._

import java.util.{Date, Objects}

import org.hibernate.search.annotations.{Analyze, Field, DateBridge, Indexed, IndexedEmbedded, Resolution, Store}

object Book {
  def apply(isbn: String,
            title: String,
            summary: String,
            price: Int,
            publisherDate: Date,
            authors: Author*): Book = {
    val book = new Book
    book.isbn = isbn
    book.title = title
    book.summary = summary
    book.price = price
    book.publisherDate = publisherDate
    book.authorsAsJava = Set(authors: _*).asJava
    book
  }
}

@SerialVersionUID(1L)
@Indexed
class Book extends Serializable {
  @Field(analyze = Analyze.NO, store = Store.YES)  // Projectionを使用する場合は、Store.YES
  var isbn: String = _

  @Field(analyze = Analyze.NO, store = Store.YES)  // Projectionを使用する場合は、Store.YES
  var title: String = _

  @Field(analyze = Analyze.NO)
  var summary: String = _

  @Field(analyze = Analyze.NO)
  var price: Int = _

  @Field(analyze = Analyze.NO)
  @DateBridge(resolution = Resolution.DAY)
  var publisherDate: Date = _

  @IndexedEmbedded
  var authorsAsJava: java.util.Set[Author] = _

  var authors: mutable.Set[Author] = authorsAsJava.asScala

  override def equals(other: Any): Boolean = other match {
    case ob: Book => isbn == ob.isbn && title == ob.title && summary == ob.summary &&
      price == ob.price && publisherDate == ob.publisherDate &&
      authorsAsJava == ob.authorsAsJava
    case _ => false
  }

  override def hashCode: Int =
    Objects.hash(isbn, title, summary, Integer.valueOf(price), publisherDate, authorsAsJava)

  override def toString: String =
    s"""Book[isbn = $isbn,
       |     title = $title,
       |     summary = $summary,
       |     price = $price,
       |     publisherDate = $publisherDate,
       |     authors = { ${Option(authorsAsJava).getOrElse(new java.util.HashSet).asScala.mkString(", ")} }]""".stripMargin

}
