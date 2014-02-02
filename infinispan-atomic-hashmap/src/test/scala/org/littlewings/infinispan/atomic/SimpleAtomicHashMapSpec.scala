package org.littlewings.infinispan.atomic

import org.infinispan.atomic.{AtomicMap, AtomicHashMap, AtomicHashMapDelta, Delta, NullDelta}

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class SimpleAtomicHashMapSpec extends FunSpec {
  describe("simple atomic hashmap spec") {
    it("simple use AtomicMap") {
      val map: AtomicMap[String, String] = new AtomicHashMap
      map.put("key1", "value1")
      map.get("key1") should be ("value1")
      map should have size 1
    }

    it("simple use AtomicHashMap") {
      val map = new AtomicHashMap[String, String]
      map.put("key1", "value1")
      map.put("key2", "value2")
      map.get("key1") should be ("value1")
      map.get("key2") should be ("value2")
      map should have size 2
    }

    it("delta") {
      val map = new AtomicHashMap[String, String]
      map.put("key1", "value1")
      map.put("key2", "value2")

      // commitするまでは、AtomicHashMapDeltaになる
      val delta1: Delta = map.delta
      delta1 should be (a [AtomicHashMapDelta])

      val atomicHashMapDelta = delta1.asInstanceOf[AtomicHashMapDelta]
      atomicHashMapDelta.getKeys should contain only ("key1", "key2")
      atomicHashMapDelta.getChangeLogSize should be (2)

      // コミット
      map.commit()

      // commitすると、DeltaはNullDeltaになる
      val delta2: Delta = map.delta
      delta2 should be (a [NullDelta])
    }

    it("merge") {
      val map1 = new AtomicHashMap[String, String]
      val map2 = new AtomicHashMap[String, String]

      map1.put("key1", "value1")
      map1.put("key2", "value2")

      map2.put("key1", "value1-1")
      map2.put("key2", "value2-1")
      map2.put("key3", "value3")

      val delta = map1.delta
      val deltaAware = delta.merge(map2)

      // mergeの引数と戻り値は、同じ参照
      deltaAware should be theSameInstanceAs map2

      // コミットする
      map1.commit()
      deltaAware.commit()

      // map1は、通常通り
      map1.keySet should contain only ("key1", "key2")
      map1.values should contain only ("value1", "value2")

      // map2は、key1とkey2がmap1とマージされている
      map2.keySet should contain only ("key1", "key2", "key3")
      map2.values should contain only ("value1", "value2", "value3")
    }

    it("remove") {
      val map = new AtomicHashMap[String, String]
      map.put("key1", "value1")
      map.put("key2", "value2")
      map.commit()

      map.remove("key2")

      // removeの分も、Deltaになる
      val delta = map.delta
      delta should be (a [AtomicHashMapDelta])

      val atomicHashMapDelta = delta.asInstanceOf[AtomicHashMapDelta]
      atomicHashMapDelta.getChangeLogSize should be (1)

      map.commit()

      map.delta should be (a [NullDelta])
    }

    it("update") {
      val map = new AtomicHashMap[String, String]
      map.put("key1", "value1")
      map.commit()

      map.put("key1", "new-value1")

      // 更新もDeltaになる
      val delta = map.delta
      delta should be (a [AtomicHashMapDelta])

      // Deltaは、新旧データを保持している
      val atomicHashMapDelta = delta.asInstanceOf[AtomicHashMapDelta]
      atomicHashMapDelta.getChangeLogSize should be (1)
      atomicHashMapDelta.toString should be ("AtomicHashMapDelta{changeLog=[PutOperation{key=key1, oldValue=value1, newValue=new-value1}],hasClear=false}")

      map.commit()

      map.delta should be (a [NullDelta])
    }
  }
}
