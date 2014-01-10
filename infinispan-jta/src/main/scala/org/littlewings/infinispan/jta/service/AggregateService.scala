package org.littlewings.infinispan.jta.service

import javax.ejb.{EJB, LocalBean, Stateless}

import org.littlewings.infinispan.jta.entity.User

@Stateless
@LocalBean
class AggregateService {
  @EJB
  private var userService: UserService = _

  @EJB
  private var cacheService: CacheService = _

  def create(user: User): Unit = {
    cacheService.put(user)
    userService.create(user)
  }

  def createCacheAndEntityFail(user: User): Unit = {
    cacheService.put(user)
    try {
      userService.createFail(user)
    } catch {
      case e: Exception =>
    }
  }

  def createEntityAndCacheFail(user: User): Unit = {
    userService.create(user)
    try {
      cacheService.putFail(user)
    } catch {
      case e: Exception =>
    }
  }

  def deleteAll(): Unit = {
    userService.removeAll()
    cacheService.clear()
  }
}
