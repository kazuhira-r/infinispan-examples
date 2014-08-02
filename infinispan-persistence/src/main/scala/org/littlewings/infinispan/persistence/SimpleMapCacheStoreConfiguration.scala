package org.littlewings.infinispan.persistence

import java.util.Properties

import org.infinispan.commons.configuration.BuiltBy
import org.infinispan.commons.configuration.ConfigurationFor
import org.infinispan.configuration.cache.{AbstractStoreConfiguration, AsyncStoreConfiguration, SingletonStoreConfiguration}

@BuiltBy(classOf[SimpleMapCacheStoreConfigurationBuilder[_, _]])
@ConfigurationFor(classOf[SimpleMapCacheStore[_, _]])
class SimpleMapCacheStoreConfiguration(purgeOnStartup: Boolean,
                                       fetchPersistenceState: Boolean,
                                       ignoreModifications: Boolean,
                                       async: AsyncStoreConfiguration,
                                       singletonStore: SingletonStoreConfiguration,
                                       preload: Boolean,
                                       shared: Boolean,
                                       properties: Properties)
  extends AbstractStoreConfiguration(purgeOnStartup,
                                     fetchPersistenceState,
                                     ignoreModifications,
                                     async,
                                     singletonStore,
                                     preload,
                                     shared,
                                     properties)

