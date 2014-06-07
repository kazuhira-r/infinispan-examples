package org.littlewings.infinispan.jdbccachestore

import org.infinispan.persistence.keymappers.{Key2StringMapper, TwoWayKey2StringMapper}

@SerialVersionUID(1L)
class KeyClass(val value: String) extends Serializable {
  override def equals(other: Any): Boolean =
    other match {
      case o: KeyClass => value == o.value
      case _ => false
    }

  override def hashCode: Int =
    value.##
}

@SerialVersionUID(1L)
class ValueClass(val value: String) extends Serializable {
  override def equals(other: Any): Boolean =
    other match {
      case o: ValueClass => value == o.value
      case _ => false
    }

  override def hashCode: Int =
    value.##
}

class KeyClass2StringMapper extends Key2StringMapper {
  override def getStringMapping(key: Any): String =
    key.asInstanceOf[KeyClass].value

  override def isSupportedType(keyType: Class[_]): Boolean =
    keyType == classOf[KeyClass]
}

class TwoWayKeyClass2StringMapper extends TwoWayKey2StringMapper {
  override def getKeyMapping(stringKey: String): AnyRef =
    new KeyClass(stringKey)

  override def getStringMapping(key: Any): String =
    key.asInstanceOf[KeyClass].value

  override def isSupportedType(keyType: Class[_]): Boolean =
    keyType == classOf[KeyClass]
}
