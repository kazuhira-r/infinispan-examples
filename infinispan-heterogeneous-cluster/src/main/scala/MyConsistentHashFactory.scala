import org.infinispan.commons.hash.Hash
import org.infinispan.distribution.ch.{DefaultConsistentHash, DefaultConsistentHashFactory}
import org.infinispan.remoting.transport.Address

class MyConsistentHashFactory extends DefaultConsistentHashFactory {
    println("====== called constructor ======")
/*
(hashFunction: Hash, numOwners: Int, numSegments: Int,
                               members: java.uti
l.List[Address], capacityFactors: java.util.Map[Address, Float])
      extends DefaultConsistentHashFactory(hashFunction, numOwners, numSegments, members, capacityFactors)
*/
  override def create(hashFunction: Hash, numOwners: Int, numSegments: Int,
                      members: java.util.List[Address], capacityFactors: java.util.Map[Address, java.lang.Float]): DefaultConsistentHash = {
    println("====== called ======")
    println(capacityFactors)
    super.create(hashFunction, numOwners, numSegments, members, capacityFactors)
  }

  override def updateMembers(baseCH: DefaultConsistentHash, actualMembers: java.util.List[Address],
                             actualCapacityFactors: java.util.Map[Address, java.lang.Float]): DefaultConsistentHash = {
    println("====== called updatemembers ======")
    println(actualCapacityFactors)
    super.updateMembers(baseCH, actualMembers, actualCapacityFactors)
  }
}
