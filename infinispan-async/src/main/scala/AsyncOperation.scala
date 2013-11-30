import scala.collection.mutable.ListBuffer
//import scala.util.control.NonFatal

import java.util.Date
import java.util.concurrent.Future

import org.infinispan.api.{BasicCache, BasicCacheContainer}
import org.infinispan.manager.DefaultCacheManager
import org.infinispan.client.hotrod.RemoteCacheManager
import org.infinispan.util.concurrent.{FutureListener, NotifyingFuture}

object AsyncOperation {
  def createManager(mode: String): BasicCacheContainer =
    mode match {
      case "e" => new DefaultCacheManager
      case "r" => new RemoteCacheManager("localhost")
    }

  def main(args: Array[String]): Unit = {
    val manager = createManager(args.toList.headOption.getOrElse("r"))
    println(s"use [${manager.getClass.getName}]")
    val cache: BasicCache[String, String] = manager.getCache()

    val bigString =
                (0 to 10000000)
                  .foldLeft(new StringBuilder) { (b, i) =>
                  b ++= i.toString
                }.toString

    val log = (msg: String) => println(s"[${new Date}] $msg")

    log("Created Data Size[%1$,3d]".format(bigString.getBytes("UTF-8").size))

    val futures = new ListBuffer[NotifyingFuture[String]]

    log("Put Async Cache Entries Start")

    futures += cache.putAsync("key1", bigString)
    futures += cache.putAsync("key2", bigString)

    log("Put Async Cache Entries End")

    futures.foreach { f =>
      log(s"Future Done. [${f.get}]")
    }

    futures.clear()

    log("Get Async Cache Entries Start")

    futures += cache.getAsync("key1")
    futures += cache.getAsync("key2")

    log("Get Async Cache Entries Start")

    futures.foreach { f =>
      val s = f.get
      log("Future Done. Size = [%1$,3d]".format(s.getBytes("UTF-8").size))
    }

    log("Program End")
  }
}

/*
class Listener[T](val name: String) extends FutureListener[T] {
  def futureDone(future: Future[T]): Unit =
    try {
      future.get
      println(s"Future Done[$name]")
    } catch {
      case NonFatal(e) => e.printStackTrace()
    }
}
*/
