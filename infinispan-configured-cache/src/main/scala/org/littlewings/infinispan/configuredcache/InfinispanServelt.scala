package org.litltewings.infinispan.configuredcache

import java.io.IOException

import javax.annotation.Resource
import javax.servlet.annotation.WebServlet
import javax.servlet.ServletException
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import org.infinispan.Cache
import org.infinispan.manager.{CacheContainer, EmbeddedCacheManager}

@WebServlet(Array("/cache/access"))
class InfinispanServlet extends HttpServlet {
  @Resource(lookup = "java:jboss/infinispan/container/my-cache-container")
  private var cacheContainer: CacheContainer = _  // CacheContainerで受けても可
  // private var cacheContainer: EmbeddedCacheManager = _  // EmbeddedCacheManagerで受けても可

  @Resource(lookup = "java:jboss/infinispan/cache/my-cache-container/my-cache-1")
  private var cache: Cache[String, Integer] = _

  @throws(classOf[IOException])
  @throws(classOf[ServletException])
  override protected def doGet(req: HttpServletRequest, res: HttpServletResponse): Unit = {
    res.setContentType("text/html; charset=UTF-8")

    val counter =
      cache.get("counter") match {
        case null => 1
        case n => n + 1
      }
    
    cache.put("counter", counter)

    val fromManagerCache = cacheContainer.getCache[String, String]("my-cache-2")

    res.getWriter.write {
      <html>
        <body>
          <p>{s"container = ${cacheContainer}"}</p>
          <p>{s"container class name = ${cacheContainer.getClass.getName}"}</p>
          <p>{s"container have cache names = ${cacheContainer.asInstanceOf[EmbeddedCacheManager].getCacheNames}"}</p>
          <p>{s"injected cache name = ${cache.getName}"}</p>
          <p>{s"injected cache transactionMode  = ${cache.getCacheConfiguration.transaction.transactionMode}"}</p>
          <p>{s"get cache name  = ${fromManagerCache.getName}"}</p>
          <p>{s"get cache transactionMode  = ${fromManagerCache.getCacheConfiguration.transaction.transactionMode}"}</p>
          <p>{s"counter = ${counter}"}</p>
        </body>
      </html>.toString + System.lineSeparator
    }
  }
}
