package example

import java.io.IOException

import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import org.infinispan.Cache
import org.infinispan.manager.{DefaultCacheManager, EmbeddedCacheManager}

@WebServlet(Array("/*"))
class SampleServlet extends HttpServlet {
  lazy val manager: EmbeddedCacheManager = new DefaultCacheManager
  lazy val cache: Cache[String, Int] = manager.getCache()

  @throws(classOf[IOException])
  @throws(classOf[ServletException])
  override protected def doGet(req: HttpServletRequest, res: HttpServletResponse): Unit = {
    if (cache.containsKey("counter")) {
      cache.put("counter", cache.get("counter") + 1)
    } else {
      cache.put("counter", 1)
    }

    res.getWriter.print(<html><h1>Counter = {cache.get("counter")}</h1></html>.toString)
  }

  override def destroy(): Unit = {
    try {
      cache.stop()
    } finally {
      manager.stop()
    }
  }
}
