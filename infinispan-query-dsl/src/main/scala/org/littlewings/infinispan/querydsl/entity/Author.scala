package org.littlewings.infinispan.querydsl.entity

import java.util.Objects

import org.hibernate.search.annotations.{Analyze, Field}

object Author {
  def apply(name: String): Author = {
    val author = new Author
    author.name = name
    author
  }
}

@SerialVersionUID(1L)
class Author extends Serializable {
  @Field(analyze = Analyze.NO)
  var name: String = _

  override def equals(other: Any): Boolean = other match {
    case oa: Author => name == oa.name
    case _ => false
  }

  override def hashCode: Int =
    Objects.hash(name)

  override def toString: String =
    s"Author[name = $name]"
}
