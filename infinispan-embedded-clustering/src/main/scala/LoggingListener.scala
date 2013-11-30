import org.infinispan.notifications.Listener
import org.infinispan.notifications.cachelistener.annotation.{CacheEntryCreated, CacheEntryRemoved}
import org.infinispan.notifications.cachelistener.event.{CacheEntryCreatedEvent, CacheEntryRemovedEvent}

@Listener
class LoggingListener {
  @CacheEntryCreated
  def observeAdd(event: CacheEntryCreatedEvent[_, _]): Unit =
    if (!event.isPre) println(s"Cache entry with key [${event.getKey}] added in cache [${event.getCache}]")
    else ()

  @CacheEntryRemoved
  def observeRemoved(event: CacheEntryRemovedEvent[_, _]): Unit =
    println(s"Cache entry with key [${event.getKey}] removed in cache [${event.getCache}]")
}
