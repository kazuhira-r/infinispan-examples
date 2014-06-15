package org.littlewings.infinispan.velocity

import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

class TemplateViewServlet extends HttpServlet {
  override protected def doGet(req: HttpServletRequest, res: HttpServletResponse): Unit = {
    req.setCharacterEncoding("UTF-8")

    req.setAttribute("name", Option(req.getParameter("name")).getOrElse("かずひら"))
    req.setAttribute("word", Option(req.getParameter("word")).getOrElse("こんにちは！"))

    val templateFile =
      Option(req.getParameter("template"))
        .getOrElse("default") + ".vm"

    req
      .getRequestDispatcher(s"/WEB-INF/template/${templateFile}")
      .forward(req, res)
  }
}
