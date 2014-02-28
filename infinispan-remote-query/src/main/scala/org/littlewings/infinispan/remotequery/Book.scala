package org.littlewings.infinispan.remotequery

import scala.beans.BeanProperty

import java.util.Objects

object Book {
  def apply(isbn: String, title: String, price: Int, summary: String): Book = {
    val book = new Book
    book.isbn = isbn
    book.title = title
    book.price = price
    book.summary = summary
    book
  }
}

class Book {
  @BeanProperty
  var isbn: String = _

  @BeanProperty
  var title: String = _

  @BeanProperty
  var price: Int = _

  @BeanProperty
  var summary: String = _

  override def hashCode: Int =
    Objects.hash(isbn, title, Integer.valueOf(price), summary)

  override def equals(other: Any): Boolean = other match {
    case o: Book =>
      isbn == o.isbn && title == o.title &&
      price == o.price && summary == o.summary
    case _ => false
  }

  override def toString: String =
    s"isbn = $isbn, title = $title, price = $price, summary = $summary"
}
