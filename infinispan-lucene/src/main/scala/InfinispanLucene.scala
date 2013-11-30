import org.infinispan.Cache
import org.infinispan.lucene.directory.DirectoryBuilder
import org.infinispan.manager.DefaultCacheManager

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.{Document, Field, TextField}
import org.apache.lucene.index.{DirectoryReader, IndexWriter, IndexWriterConfig}
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.Directory
import org.apache.lucene.util.Version

object InfinispanLucene {
  def main(args: Array[String]): Unit = {
    val manager = new DefaultCacheManager
    // 各種キャッシュの作成
    val metaDataCache: Cache[_, _] = manager.getCache("metaDataCache")
    val chunksCache: Cache[_, _] = manager.getCache("chunksCache")
    val distLocksCache: Cache[_, _] = manager.getCache("distLocksCache")

    val luceneVersion = Version.LUCENE_43

    // LuceneのDirectoryを作成
    val indexDir =
      DirectoryBuilder
        .newDirectoryInstance(metaDataCache, chunksCache, distLocksCache, "indexName")
        .create()

    // 今回使用するAnalyzer
    val analyzer = new StandardAnalyzer(luceneVersion)

    // インデックスへの登録
    val indexWriterConfig = new IndexWriterConfig(luceneVersion, analyzer)
    val indexWriter = new IndexWriter(indexDir, indexWriterConfig)

    val document = new Document
    val text = "This is the text to be indexed."
    document.add(new Field("fieldName", text, TextField.TYPE_STORED))
    
    indexWriter.addDocument(document)
    indexWriter.close()

    // インデックスからの読み出し
    val directoryReader = DirectoryReader.open(indexDir)
    val indexSearcher = new IndexSearcher(directoryReader)

    // Queryの作成
    val queryParser = new QueryParser(luceneVersion, "fieldName", analyzer)
    val query = queryParser.parse("text")
    val hits = indexSearcher.search(query, null, 1000).scoreDocs

    // 検索結果表示
    println(s"hits length => ${hits.length}")

    for (h <- hits) {
      val hitDoc = indexSearcher.doc(h.doc)
      println(s"Hit Text => ${hitDoc.get("fieldName")}")
    }

    directoryReader.close()

    indexDir.close()

    metaDataCache.stop()
    chunksCache.stop()
    distLocksCache.stop()
    manager.stop()
  }
}
