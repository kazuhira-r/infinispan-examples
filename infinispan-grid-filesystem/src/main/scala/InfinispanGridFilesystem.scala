import scala.util.Try

import java.io.{BufferedReader, BufferedWriter, FileInputStream, InputStreamReader, OutputStreamWriter}
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

import org.infinispan.io.{GridFile, GridFilesystem}
import org.infinispan.manager.DefaultCacheManager

object InfinispanGridFilesystem {
  def main(args: Array[String]): Unit = {
    val manager = new DefaultCacheManager("infinispan.xml")

    val dataCache = manager.getCache[String, Array[Byte]]("dataCache")
    val metaCache = manager.getCache[String, GridFile.Metadata]("metaCache")

    try {
      val filesystem = new GridFilesystem(dataCache, metaCache)

      useFile(filesystem)
      useOio(filesystem)
      useNio(filesystem)
    } finally {
      metaCache.stop()
      dataCache.stop()
      manager.stop()
    }
  }

  def useFile(filesystem: GridFilesystem): Unit = {
    // ファイル作成
    val file = filesystem.getFile("file.txt")

    println(file.exists) // => false

    file.createNewFile()

    println(file.exists) // => true

    println(file.getAbsolutePath) // => /file.txt
    println(Try(file.getCanonicalPath)) // => Failure(java.lang.UnsupportedOperationException: Not implemented)
    println(Try(file.toURI)) // => Failure(java.lang.UnsupportedOperationException: Not implemented)

    // ディレクトリ作成
    val dirs = filesystem.getFile("/directory/subdirectoy/subsubdirectory")
    println(dirs.exists)  // => false

    dirs.mkdirs()

    println(dirs.exists)  // => true
  }

  def useOio(filesystem: GridFilesystem): Unit = {
    for {
      // ローカルファイルから
      fis <- new FileInputStream("src/main/scala/InfinispanGridFilesystem.scala")
      isr <- new InputStreamReader(fis, "UTF-8")
      reader <- new BufferedReader(isr)

      // Grid Filesystemへ
      os <- filesystem.getOutput("/InfinispanGridFilesystem.scala")
      osw <- new OutputStreamWriter(os, "UTF-8")
      writer <- new BufferedWriter(osw)
    } {
      val chars =
        Iterator
          .continually(reader.read())
          .takeWhile(_ != -1)
          .map(_.asInstanceOf[Char])
          .toArray
      writer.write(chars, 0, chars.size)
    }

    for {
      // Grid Filesystemから読み出し
      is <- filesystem.getInput("/InfinispanGridFilesystem.scala")
      isr <- new InputStreamReader(is, "UTF-8")
      reader <- new BufferedReader(isr)
    } {
      val lines =
        Iterator
          .continually(reader.readLine())
          .takeWhile(_ != null)
          .toList
      val max = lines.size.toString.size
      
      for ((line, index) <- lines.iterator.zipWithIndex) {
        val f = "%1$0" + max + "d: %2$s%n"
        printf(f, index, line)
      }
    }
  }

  def useNio(filesystem: GridFilesystem): Unit = {
    // ディレクトリ作成
    filesystem.getFile("/sbt-configuration/").mkdir()

    for {
      // ローカルファイルから
      localReadChannel <- new FileInputStream("build.sbt").getChannel

      // Grid Filesystemへ
      gridWriteChannel <- filesystem.getWritableChannel("/sbt-configuration/build.sbt")
    } {
      // コピー
      localReadChannel.transferTo(0, localReadChannel.size, gridWriteChannel)
    }

    for (gridReadChannel <- filesystem.getReadableChannel("/sbt-configuration/build.sbt")) {
      val byteBuffer = ByteBuffer.allocate(512)
      val builder = new StringBuilder

      byteBuffer.clear()

      Iterator
        .continually(gridReadChannel.read(byteBuffer))
        .takeWhile(len => (len != -1 && len != 0) || byteBuffer.position > 0)  // 最終的に、readの結果が0のままになる…
        .foreach { len =>
          byteBuffer.flip()
          val charBuffer = StandardCharsets.UTF_8.decode(byteBuffer)
          byteBuffer.compact()

          builder ++= charBuffer.toString
        }

      while (byteBuffer.hasRemaining) {
        val charBuffer = StandardCharsets.UTF_8.decode(byteBuffer)
        builder ++= charBuffer.toString
      }

      builder.lines.foreach(l => println(s"Nio: $l"))
    }
  }

  implicit class CloseableWrapper[A <: AutoCloseable](val underlying: A) extends AnyVal {
    def foreach(f: A => Unit): Unit =
      try {
        f(underlying)
      } finally {
        if (underlying != null) {
          underlying.close()
        }
      }

    /*
    def use[B](f: A => B): B =
      try {
        f(underlying)
      } finally {
        if (underlying != null) {
          underlying.close()
        }
      }

    def foreach(f: A => Unit): Unit = use(f)

    // 結果の持ち出しは、諦めました…
    def map[B](f: A => B): B = use(f)
    def flatMap[B <: AutoCloseable](f: A => CloseableWrapper[B]): CloseableWrapper[B] =
      use(x => f(x).underlying)
    */
  }
}
