package javaee6.web.rest

import javax.inject.Inject
import javax.ws.rs.{GET, Path, QueryParam}

import org.infinispan.Cache

import javaee6.web.cache.CalcCache

@Path("calc")
class CalcResource {
  @CalcCache
  @Inject
  private var cache: Cache[String, Int] = _

  @GET
  @Path("add")
  def add(@QueryParam("p1") p1: Int, @QueryParam("p2") p2: Int): String = {
    val key = s"$p1:$p2"

    cache.containsKey(key) match {
      case true => s"Result By Cache[${cache.getName}] => ${cache.get(key)}"
      case false =>
        Thread.sleep(3 * 1000L)
        val value = p1 + p2
        cache.put(key, value)
        s"Result By Real => $value"
    }
  }
}
