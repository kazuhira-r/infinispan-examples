import org.infinispan.notifications.Listener
import org.infinispan.notifications.cachelistener.annotation.{CacheEntryInvalidated, CacheEntryVisited}
import org.infinispan.notifications.cachelistener.event.{CacheEntryInvalidatedEvent, CacheEntryVisitedEvent}

@Listener
class CacheListener {
  @CacheEntryVisited
  def cacheEntryVisited(event: CacheEntryVisitedEvent[_, _]): Unit =
    println(s"Entry Visited Event: $event")

  @CacheEntryInvalidated
  def cacheEntryInvalidated(event: CacheEntryInvalidatedEvent[_, _]): Unit =
    println(s"Invalidated Event: $event")
}
