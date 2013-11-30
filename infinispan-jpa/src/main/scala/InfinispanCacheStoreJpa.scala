import scala.collection.JavaConverters._

import scala.beans.BeanProperty

import javax.persistence.{Column, Entity, Id, Persistence, Table, Version}

import org.infinispan.CacheException
import org.infinispan.manager.DefaultCacheManager

object InfinispanCacheStoreJpaExample {
  def main(args: Array[String]): Unit = {
    val manager = new DefaultCacheManager("infinispan.xml")

    try {
      println("===== Create Cache =====")
      val cache = manager.getCache[Int, User]("cacheStoreJpa")
      println("===== Created Cache =====")

      // Cache#clearすると、全データをDELETEします
      cache.clear()

      val user = User(1, "カツオ", "磯野", 11)
      cache.put(user.id, user)

      println("===== Current Cache1 =====")
      println(cache.get(user.id))
      require(cache.get(user.id).age == 11)
      println("===== Current Cache1 =====")

      /*
      // JPAで直接更新
      println("===== JPA Update =====")
      val entityManagerFactory = Persistence.createEntityManagerFactory("infinispan.cachestore.jpa.example")
      val entityManager = entityManagerFactory.createEntityManager()
      val emTx = entityManager.getTransaction

      emTx.begin()

      val query = entityManager.createQuery("SELECT u FROM User u")

      query.getResultList.asScala.foreach {
        case u: User =>
          println(s"Selected JPA: $u")
          u.age += 1
          // ここで更新
          entityManager.merge(u)
          println(s"Updated JPA: $u")

          // 年齢は増えている
          require(u.age == 12)
      }

      emTx.commit()

      entityManager.close()
      entityManagerFactory.close()
      println("===== JPA Updated =====")
      */

      // JPAを直接使って更新してしまうと、実はここでロードしようとしてもCacheからはズレてしまう…
      println("===== Current Cache2 =====")
      println(cache.get(user.id))
      require(cache.get(user.id).age == 11)  // 年齢が増えていない
      println("===== Current Cache2 =====")

      try {
        val u = cache.get(user.id)
        u.age += 1
        cache.put(u.id, u)

        require(cache.get(u.id).age == 12)
      } catch {
        // 上記JPAのコードを実行した場合は、
        // 管理されている状態とズレているので、例外となる
        case e: CacheException => println(s"Exception => ${e.getMessage}")
      }

      //cache.remove(user.id)

      cache.stop()
    } finally {
      manager.stop()
    }
  }
}

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
  @BeanProperty var lastName: String = _

  @Column
  @BeanProperty
  var age: Int = _

  @Column(name = "version_no")
  @Version
  @BeanProperty
  var versionNo: Int = _

  override def toString(): String =
    s"id = $id, firstName = $firstName, lastName = $lastName, age = $age, versionNo = $versionNo"
}
