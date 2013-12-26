package org.littlewings.infinispan.query.entity

import java.util.{Date, Objects}

import org.hibernate.search.annotations.{Analyze, Analyzer, Field, DateBridge, Indexed}
import org.hibernate.search.annotations.{Resolution, Store}

import org.apache.lucene.analysis.ja.JapaneseAnalyzer

object Book {
  def apply(isbn: String,
            title: String,
            summary: String,
            price: Int,
            publisherDate: Date): Book = {
    val book = new Book
    book.isbn = isbn
    book.title = title
    book.summary = summary
    book.price = price
    book.publisherDate = publisherDate
    book
  }
}

@Indexed
@SerialVersionUID(1L)
class Book extends Serializable {
  @Field(analyze = Analyze.NO)
  var isbn: String = _

  @Field
  @Analyzer(impl = classOf[JapaneseAnalyzer])
  var title: String = _

  @Field
  @Analyzer(impl = classOf[JapaneseAnalyzer])
  var summary: String = _

  @Field(analyze = Analyze.NO)
  var price: Int = _

  @Field(analyze = Analyze.NO)
  @DateBridge(resolution = Resolution.DAY)
  var publisherDate: Date = _

  override def equals(other: Any): Boolean = other match {
    case ob: Book => isbn == ob.isbn && title == ob.title && summary == ob.summary &&
      price == ob.price && publisherDate == ob.publisherDate
    case _ => false
  }

  override def hashCode: Int =
    Objects.hash(isbn, title, summary, Integer.valueOf(price), publisherDate)

  override def toString: String =
    s"""Book[isbn = $isbn,
       |     title = $title,
       |     summary = $summary,
       |     price = $price,
       |     publisherDate = $publisherDate""".stripMargin
}
