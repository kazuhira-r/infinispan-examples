package org.littlewings.infinispan.mapreduce

import scala.collection.JavaConverters._

import org.infinispan.distexec.mapreduce.{Collator, Collector, Mapper, Reducer}

@SerialVersionUID(1L)
class DoublingMapper extends Mapper[String, Int, String, Int] {
  override def map(key: String, value: Int, collector: Collector[String, Int]): Unit =
    collector.emit(key, value * 2)
}

@SerialVersionUID(1L)
class DoublingReducer extends Reducer[String, Int] {
  override def reduce(key: String, iter: java.util.Iterator[Int]): Int =
    iter.asScala.sum
}

class SummerizeCollator extends Collator[String, Int, Int] {
  override def collate(reduceResults: java.util.Map[String, Int]): Int =
    reduceResults.asScala.foldLeft(0) { case (acc, (k, v)) => acc + v }
}
