package org.littlewings.infinispan.mapreduce

import scala.collection.JavaConverters._

import org.infinispan.distexec.mapreduce.{Collator, Collector, Mapper, Reducer}

@SerialVersionUID(1L)
class ReplicatedCacheMapper extends Mapper[String, String, String, Set[String]] {
  override def map(key: String, value: String, collector: Collector[String, Set[String]]): Unit =
    collector.emit(key, Set(s"Mapper-${Thread.currentThread.getName}"))
}

@SerialVersionUID(1L)
class ReplicatedCacheReducer extends Reducer[String, Set[String]] {
  override def reduce(key: String, iter: java.util.Iterator[Set[String]]): Set[String] =
    iter.asScala.foldLeft(Set.empty[String]) { (acc, c) => acc ++ c } + s"Reducer-${Thread.currentThread.getName}"
}
