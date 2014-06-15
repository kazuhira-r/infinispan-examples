package org.littlewings.infinispan.velocity

import java.io.InputStream

import org.infinispan.io.GridFilesystem

import org.apache.commons.collections.ExtendedProperties
import org.apache.velocity.exception.ResourceNotFoundException
import org.apache.velocity.runtime.resource.Resource
import org.apache.velocity.runtime.resource.loader.ResourceLoader

class InfinispanGridFileResourceLoader extends ResourceLoader {
  private def withGfs[A](fun: GridFilesystem => A): A =
    fun(InfinispanGridFileSystemLocator.getGridFileSystem)

  override def init(configuration: ExtendedProperties): Unit = {
  }

  override def getLastModified(resource: Resource): Long =
    withGfs { gfs =>
      val file = gfs.getFile(resource.getName)

      if (file.exists)
        file.lastModified
      else
        0
    }

  override def isSourceModified(resource: Resource): Boolean =
    withGfs { gfs =>
      val file = gfs.getFile(resource.getName)

      if (file.exists)
        file.lastModified != resource.getLastModified
      else
        true
    }

  override def resourceExists(name: String): Boolean =
    withGfs { gfs =>
      gfs.getFile(name).exists
    }

  @throws(classOf[ResourceNotFoundException])
  override def getResourceStream(templateName: String): InputStream =
    withGfs { gfs =>
      val file = gfs.getFile(templateName)

      if (file.exists)
        gfs.getInput(templateName)
      else
        throw new ResourceNotFoundException(s"Resource[$templateName] Not Found in GridFileSystem")
    }
}
