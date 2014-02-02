package org.littlewings.infinispan.transaction

trait ThreadSupport {
  protected def spawn(fun: => Unit): Thread = {
    val thread = new Thread {
      override def run(): Unit = fun
    }
    thread.start()
    thread
  }

  protected def sleep(msec: Long): Unit =
    Thread.sleep(msec)
}
