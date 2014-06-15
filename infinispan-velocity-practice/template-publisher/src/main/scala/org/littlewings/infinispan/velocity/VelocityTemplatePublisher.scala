package org.littlewings.infinispan.velocity

import java.nio.file.{Files, Paths}

import org.infinispan.io.{GridFile, GridFilesystem}
import org.infinispan.manager.DefaultCacheManager

object VelocityTemplatePublisher {
  def main(args: Array[String]): Unit = {
    System.setProperty("java.net.preferIPv4Stack", "true")

    val (fromFilePath, toFilePath) = args.toList match {
      case f :: t :: Nil => (f, t)
      case _ =>
        Console.err.println("Input: [fromFilePath] [toFilePath]")
        sys.exit(1)
    }

    publishGridFileSystem(fromFilePath, toFilePath)
  }

  private def publishGridFileSystem(fromFilePath: String, toFilePath: String): Unit = {
    val manager = new DefaultCacheManager("infinispan.xml")

    try {
      val templateDataCache = manager.getCache[String, Array[Byte]]("templateDataCache")
      val metaDataCache = manager.getCache[String, GridFile.Metadata]("templateMetaDataCache")

      val gridFileSystem = new GridFilesystem(templateDataCache, metaDataCache)

      val file = gridFileSystem.getFile(toFilePath)
      if (!file.exists() && file.getParentFile != null) {
        file.getParentFile.mkdirs()
      }

      val os = gridFileSystem.getOutput(file.asInstanceOf[GridFile])
      try {
        for (b <- Files.readAllBytes(Paths.get(fromFilePath))) {
          os.write(b)
        }
      } finally {
        os.close()
      }
    } finally {
      manager.stop()
    }
  }
}
