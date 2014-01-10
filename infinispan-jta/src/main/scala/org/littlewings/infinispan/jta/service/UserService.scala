package org.littlewings.infinispan.jta.service

import scala.collection.JavaConverters._

import javax.ejb.{LocalBean, Stateless}
import javax.persistence.{EntityManager, PersistenceContext}

import org.littlewings.infinispan.jta.entity.User

@Stateless
@LocalBean
class UserService {
  @PersistenceContext
  private var em: EntityManager = _

  def create(user: User): Unit =
    em.persist(user)

  def createFail(user: User): Unit = {
    em.persist(user)
    throw new RuntimeException("Oops!!")
  }

  def udpate(user: User): User =
    em.merge(user)

  def remove(user: User): Unit =
    em.remove(em.merge(user))

  def findById(id: Int): User =
    em.find(classOf[User], id)

  def findAllOrderById: Iterable[User] =
    em
      .createQuery("""|SELECT u
                      |  FROM User u
                      | ORDER BY u.id ASC""".stripMargin)
      .getResultList
      .asScala
      .asInstanceOf[Iterable[User]]

  def removeAll(): Unit =
    em
      .createQuery("DELETE FROM User")
      .executeUpdate()
}
