package org.littlewings.infinispan.velocity

import scala.collection.JavaConverters._

import javax.servlet.{ServletContextEvent, ServletContextListener}

import org.infinispan.io.{GridFile, GridFilesystem}
import org.infinispan.manager.{DefaultCacheManager, EmbeddedCacheManager}

object InfinispanGridFileSystemLocator {
  private var manager: EmbeddedCacheManager = _
  private var gridFileSystem: GridFilesystem = _

  private def init(): Unit = {
    manager = new DefaultCacheManager("infinispan.xml")

    val templateDataCache = manager.getCache[String, Array[Byte]]("templateDataCache")
    val metaDataCache = manager.getCache[String, GridFile.Metadata]("templateMetaDataCache")

    gridFileSystem = new GridFilesystem(templateDataCache, metaDataCache)
  }

  def getGridFileSystem: GridFilesystem = gridFileSystem

  private def destroy(): Unit = {
    try {
      for (cacheName <- manager.getCacheNames.asScala) {
        manager.getCache(cacheName).stop()
      }
    } finally {
      manager.stop()
    }
  }
}

class InfinispanGridFileSystemLocator extends ServletContextListener {
  override def contextInitialized(sce: ServletContextEvent): Unit =
    InfinispanGridFileSystemLocator.init()

  override def contextDestroyed(sce: ServletContextEvent): Unit =
    InfinispanGridFileSystemLocator.destroy()
}
