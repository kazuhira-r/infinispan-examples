package org.littlewings.infinspan.jta.jaxrs

import javax.inject.Inject
import javax.ws.rs.{DELETE, GET, Path, PathParam, Produces, PUT}
import javax.ws.rs.core.MediaType

import org.infinispan.Cache

import org.littlewings.infinispan.jta.entity.User
import org.littlewings.infinispan.jta.service.{AggregateService, CacheService, UserService}

@Path("tx")
class TxResource {
  @Inject
  private var userService: UserService = _

  @Inject
  private var cacheService: CacheService = _

  @Inject
  private var aggregateService: AggregateService = _

  private def createUser(id: Int): User =
    id match {
      case 1 => User(1, "カツオ", "磯野", 11)
      case 2 => User(2, "ワカメ", "磯野", 9)
      case 3 => User(3, "タラオ", "フグ田", 3)
    }

  @GET
  @Path("{id}")
  @Produces(Array(MediaType.TEXT_PLAIN))
  def index(@PathParam("id") id: Int): String =
    s"""|Database => ${userService.findById(id)}
        |Cache    => ${cacheService.get(id)}
        |""".stripMargin

  @GET
  @Produces(Array(MediaType.TEXT_PLAIN))
  def index: String =
    s"""|Database =>
        |${userService.findAllOrderById.mkString("  ", System.lineSeparator + "  ", "")}
        |Cache =>
        |${cacheService.findAllOrderById.mkString("  ", System.lineSeparator + "  " , "")}
        |""".stripMargin

  @PUT
  @Path("{id}")
  @Produces(Array(MediaType.TEXT_PLAIN))
  def create(@PathParam("id") id: Int): String = {
    val user = createUser(id)
    aggregateService.create(user)
    user.toString
  }

  @PUT
  @Path("failEntity/{id}")
  @Produces(Array(MediaType.TEXT_PLAIN))
  def createCacheAndFail(@PathParam("id") id: Int): String = {
    val user = createUser(id)
    aggregateService.createCacheAndEntityFail(user)
    user.toString
  }

  @PUT
  @Path("failCache/{id}")
  @Produces(Array(MediaType.TEXT_PLAIN))
  def createEntityAndCacheFail(@PathParam("id") id: Int): String = {
    val user = createUser(id)
    aggregateService.createEntityAndCacheFail(user)
    user.toString
  }

  @DELETE
  def deleteAll(): Unit =
    aggregateService.deleteAll()
}
