@SerialVersionUID(1L)
class KeyEntry(var value: String) extends Serializable {
  override def toString: String =
    s"This Key Value is [$value]"

  override def equals(other: Any): Boolean =
    other match {
      case o: KeyEntry => value == o.value
      case _ => false
    }

  override def hashCode: Int =
    value.hashCode * 31
}

@SerialVersionUID(1L)
class ValueEntry(var value: String) extends Serializable {
  override def toString: String =
    s"This Entry Value is [$value]"
}
