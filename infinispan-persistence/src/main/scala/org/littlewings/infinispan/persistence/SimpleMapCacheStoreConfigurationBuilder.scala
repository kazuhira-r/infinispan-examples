package org.littlewings.infinispan.persistence

import org.infinispan.commons.configuration.Builder
import org.infinispan.configuration.cache.{AbstractStoreConfigurationBuilder, PersistenceConfigurationBuilder, StoreConfiguration}

class SimpleMapCacheStoreConfigurationBuilder[K, V](builder: PersistenceConfigurationBuilder)
  extends AbstractStoreConfigurationBuilder[SimpleMapCacheStoreConfiguration,
                                            SimpleMapCacheStoreConfigurationBuilder[_, _]](builder) {

  override def self: SimpleMapCacheStoreConfigurationBuilder[K, V] =
    this

  override def create: SimpleMapCacheStoreConfiguration =
    new SimpleMapCacheStoreConfiguration(purgeOnStartup,
                                         fetchPersistentState,
                                         ignoreModifications,
                                         async.create,
                                         singletonStore.create,
                                         preload,
                                         shared,
                                         properties)

  override def read(template: SimpleMapCacheStoreConfiguration): Builder[_] = {
    fetchPersistentState = template.fetchPersistentState
    ignoreModifications = template.ignoreModifications
    properties = template.properties
    purgeOnStartup = template.purgeOnStartup
    async.read(template.async)
    singletonStore.read(template.singletonStore)
    preload = template.preload
    shared = template.shared

    this
  }
}
