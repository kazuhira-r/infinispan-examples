package org.littlewings.infinispan.persistence

import scala.collection._

import java.io._
import java.nio.file.{Files, Paths}
import java.util.concurrent.{Callable, Executor}

import org.infinispan.commons.io.ByteBuffer
import org.infinispan.executors.ExecutorAllCompletionService
import org.infinispan.marshall.core.MarshalledEntry
import org.infinispan.metadata.InternalMetadata
// import org.infinispan.persistence.spi.{CacheLoader, CacheWriter, ExternalStore, InitializationContext}
import org.infinispan.persistence.spi.{AdvancedCacheLoader, AdvancedCacheWriter, AdvancedLoadWriteStore, InitializationContext, PersistenceException}
import org.infinispan.persistence.{PersistenceUtil, TaskContextImpl}

object SimpleMapCacheStore {
  private var numberOfCluster: Int = 1
  private var current: Int = 1

  private val instances: mutable.Map[String, SimpleMapCacheStore[_, _]] = mutable.Map.empty

  def instances(n: Int): Unit =
    numberOfCluster = n

  def aquireStoreName(): String = {
    require(numberOfCluster + 1 > current)

    val name = s"store-${current}.dmp"
    current += 1

    name
  }

  def register(store: SimpleMapCacheStore[_, _]): Unit = synchronized {
    val storeName = aquireStoreName()
    store.storeName = storeName

    instances += (storeName -> store)
  }
}

// class SimpleMapCacheStore[K, V] extends CacheLoader[K, V] with CacheWriter[K, V] {
// class SimpleMapCacheStore[K, V] extends ExternalStore[K, V] {  // こちらでも可
class SimpleMapCacheStore[K, V] extends AdvancedCacheLoader[K, V] with AdvancedCacheWriter[K, V] {
// class SimpleMapCacheStore[K, V] extends AdvancedLoadWriteStore[K, V] {  // こちらでも可
  private var store: mutable.Map[K,(V, Array[Byte])] = mutable.HashMap.empty
  private var storeName: String = _

  private var ctx: InitializationContext = _

  // from Lifecyele
  override def start(): Unit = {
    SimpleMapCacheStore.register(this)

    println(s"Store Name = $storeName")

    val path = Paths.get(storeName)

    if (Files.exists(path)) {
      val is = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(path)))

      try {
        store = is.readObject.asInstanceOf[mutable.Map[K, (V, Array[Byte])]]
      } finally {
        is.close()
      }

      println(s"Loaded From Store[$storeName], keys = ${store.keys}")
    } else {
      println(s"Store[$storeName], not exists.")
    }
  }

  // from Lifecyele
  override def stop(): Unit = {
    if (!store.isEmpty) {
      val path = Paths.get(storeName)
      val os = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(path)))

      try {
        os.writeObject(store)
      } finally {
        os.close()
      }

      println(s"Store[$storeName] saved, keys = ${store.keys}")
    } else {
      val path = Paths.get(storeName)
      Files.deleteIfExists(path)

      println(s"Store[$storeName] is empty.")
    }
  }

  // from CacheLoader, CacheWriter
  override def init(ctx: InitializationContext): Unit =
    this.ctx = ctx

  // from CacheLoader
  override def contains(key: K): Boolean = {
    if (store.contains(key)) {
      println(s"Store[$storeName] contains key[$key]")
      true
    } else {
      println(s"Store[$storeName] not contains key[$key]")
      false
    }
  }

  // from CacheLoader
  override def load(key: K): MarshalledEntry[K, V] =
    load(key, true, true)

  def load(key: K, fetchValue: Boolean, fetchMetadata: Boolean): MarshalledEntry[K, V] = {
    println(s"Store[$storeName] try load key[$key], fetchValue[$fetchValue], fetchMetadata[$fetchMetadata]")

    val value = store.get(key)

    val marshaller = ctx.getMarshaller

    val binaryKey =  marshaller.objectToByteBuffer(key)

    val factory = ctx.getByteBufferFactory
    val keyBuffer = factory.newByteBuffer(binaryKey, 0, binaryKey.size)

    value match {
      case Some((v, m)) =>
        val valueBuffer =
          if (fetchValue) {
            val binaryValue = marshaller.objectToByteBuffer(v)
            factory.newByteBuffer(binaryValue, 0, binaryValue.size)
          } else {
            null
          }

        val metaBuffer =
          if (fetchMetadata && m != null)
            factory.newByteBuffer(m, 0, m.size)
          else
            null

        val marshalledEntry =
          ctx
            .getMarshalledEntryFactory
            .newMarshalledEntry(keyBuffer, valueBuffer, metaBuffer)
            .asInstanceOf[MarshalledEntry[K, V]]

        val now = System.currentTimeMillis
        val metadata = marshalledEntry.getMetadata

        if (metadata != null && metadata.isExpired(now)) {
          println(s"Store[$storeName] loaded, but expire entry, key[$key]")
          store -= key
          null
        } else {
          marshalledEntry
        }
      case None =>
        println(s"Store[$storeName] missing key[$key]")
        null
    }
  }

  // from CacheWriter
  override def write(entry: MarshalledEntry[K, V]): Unit = {
    println(s"Store[$storeName] write (key, value) = (${entry.getKey}, ${entry.getValue})")
    store += (entry.getKey -> (entry.getValue -> entry.getMetadataBytes.getBuf))
  }

  // from CacheWriter
  override def delete(key: K): Boolean = {
    println(s"Store[$storeName] remove[$key]")

    if (store.contains(key)) {
      store -= key
      true
    } else {
      false
    }
  }

  // from AdvancedCacheLoader
  override def process(filter: AdvancedCacheLoader.KeyFilter[K],
                       task: AdvancedCacheLoader.CacheLoaderTask[K, V],
                       executor: Executor,
                       fetchValue: Boolean,
                       fetchMetadata: Boolean): Unit = {
    println(s"Store[$storeName] process, fetchValue[$fetchValue], fetchMetadata[$fetchMetadata]")

    val filterOrLoadAll =
      PersistenceUtil.notNull(filter).asInstanceOf[AdvancedCacheLoader.KeyFilter[K]]

    val eacs = new ExecutorAllCompletionService(executor)
    val taskContext = new TaskContextImpl

    store
      .keys
      .withFilter { k =>
          filterOrLoadAll.shouldLoadKey(k)
      }
      .foreach { k =>
        if (!taskContext.isStopped) {
          eacs.submit(new Callable[Void] {
            @throws(classOf[Exception])
            override def call(): Void = {
              val marshalledEntry = load(k, fetchValue, fetchMetadata)

              if (marshalledEntry != null) {
                println(s"Store[$storeName] loaded key[$k], on process")
                task.processEntry(marshalledEntry, taskContext)
              }

              null
            }
          })
        }
      }

    eacs.waitUntilAllCompleted()

    if (eacs.isExceptionThrown()) {
      throw new PersistenceException("Execution exception!", eacs.getFirstException());
    }
  }

  // from AdvancedCacheLoader
  override def size: Int = {
    println(s"Store[$storeName] size")
    store.size
  }

  // from AdvancedCacheWriter
  override def clear(): Unit = {
    println(s"Store[$storeName] clear")
    store.clear()
  }

  // from AdvancedCacheWriter
  override def purge(executor: Executor,
                     listener: AdvancedCacheWriter.PurgeListener[_]): Unit = {
    println(s"Store[$storeName] purge")

    executor.execute(new Runnable {
      override def run(): Unit = {
        val now = System.currentTimeMillis
        val marshaller = ctx.getMarshaller
        val factory = ctx.getByteBufferFactory
        val marshalledEntryFactory = ctx.getMarshalledEntryFactory

        store.foreach { case (k, (v, m)) =>
          val (kb, vb, mb) =
            (marshaller.objectToByteBuffer(k), marshaller.objectToByteBuffer(v), m)
          val (keyBuffer, valueBuffer, metaBuffer) =
            (factory.newByteBuffer(kb, 0, kb.size),
             factory.newByteBuffer(vb, 0, vb.size),
             factory.newByteBuffer(mb, 0, mb.size))

          val marshalledEntry =
            marshalledEntryFactory
              .newMarshalledEntry(keyBuffer, valueBuffer, metaBuffer)

          if (marshalledEntry.getMetadata.isExpired(now)) {
            println(s"Store[$storeName] purge key[$k]")
            store -= k

            if (listener != null) {
              println(s"Store[$storeName] listner nofiticate, key[$k]")
              val task =
                listener.asInstanceOf[AdvancedCacheWriter.PurgeListener[K]]
              task.entryPurged(k)
            }
          }
        }
      }
    })
  }
}
