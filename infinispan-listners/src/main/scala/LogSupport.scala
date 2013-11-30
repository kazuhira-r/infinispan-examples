import org.infinispan.notifications.cachelistener.event.Event

trait LogSupport {
  def log(msg: => Any): Unit = {
    println(s"${prefix}${msg}")
  }

  protected def prefix: String = ""
}

trait SimpleClassNameLogSupport extends LogSupport {
  override protected def prefix: String =
    s"${super.prefix}[${getClass.getSimpleName}]# "
}

trait ThreadNameLogSupport extends LogSupport {
  override protected def prefix: String =
    s"${super.prefix}[${Thread.currentThread.getName}] "
}

trait PrePostLogSupport extends LogSupport {
  def log(event: Event[_, _], msg: Any): Unit =
    if (event.isPre)
      log("[Pre ] " + msg)
    else 
      log("[Post] " + msg)
}
