package org.littlewings.infinispan.jta.service

import scala.collection.JavaConverters._

import javax.ejb.{LocalBean, Stateless}
import javax.inject.Inject

import org.infinispan.Cache

import org.littlewings.infinispan.jta.entity.User

@Stateless
@LocalBean
class CacheService {
  @Inject
  private var cache: Cache[Int, User] = _

  def get(id: Int): User =
    cache.get(id)

  def put(user: User): Unit =
    cache.put(user.id, user)

  def putFail(user: User): Unit = {
    cache.put(user.id, user)
    throw new RuntimeException("Oops!")
  }

  def clear(): Unit =
    cache.clear()

  def findAllOrderById: Iterable[User] =
    cache.values.asScala.toSeq.sortWith { _.id < _.id }
}
