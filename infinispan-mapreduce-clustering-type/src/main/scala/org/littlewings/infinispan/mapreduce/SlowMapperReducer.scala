package org.littlewings.infinispan.mapreduce

import scala.collection.JavaConverters._

import org.infinispan.distexec.mapreduce.{Collector, Mapper, Reducer}

import java.util.concurrent.TimeUnit

@SerialVersionUID(1L)
class SlowMapper(waitTime: Long, timeUnit: TimeUnit) extends Mapper[String, Int, String, Int] {
  override def map(key: String, value: Int, collator: Collector[String, Int]): Unit = {
    timeUnit.sleep(waitTime)
    collator.emit(key, value)
  }
}

@SerialVersionUID(1L)
class SlowReducer(waitTime: Long, timeUnit: TimeUnit) extends Reducer[String, Int] {
  override def reduce(key: String, iter: java.util.Iterator[Int]): Int = {
    timeUnit.sleep(waitTime)
    iter.asScala.sum
  }
}
