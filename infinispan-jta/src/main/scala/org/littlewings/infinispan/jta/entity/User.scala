package org.littlewings.infinispan.jta.entity

import scala.beans.BeanProperty

import javax.persistence.{Column, Entity, Id, Table, Version}

object User {
  def apply(id: Int, firstName: String, lastName: String, age: Int): User = {
    val user = new User
    user.id = id
    user.firstName = firstName
    user.lastName = lastName
    user.age = age
    user
  }
}

@SerialVersionUID(1L)
@Entity
@Table(name = "user")
class User extends Serializable {
  @Id
  @BeanProperty
  var id: Int = _

  @Column(name = "first_name")
  @BeanProperty
  var firstName: String = _

  @Column(name = "last_name")
  @BeanProperty
  var lastName: String = _

  @Column
  @BeanProperty
  var age: Int = _

  @Column(name = "version_no")
  @Version
  @BeanProperty
  var versionNo: Int = _

  override def toString: String =
    s"id = $id, firstName = $firstName, lastName = $lastName, age = $age, versionNo = $versionNo"
}
