import org.infinispan.notifications.Listener
import org.infinispan.notifications.cachelistener.annotation._
import org.infinispan.notifications.cachelistener.event._

//@Listener
//@Listener(sync = true)
@Listener(sync = false)
class CacheLevelListener extends ThreadNameLogSupport
                         with SimpleClassNameLogSupport
                         with PrePostLogSupport {
  @CacheEntryCreated
  def cacheEntryCreated(event: CacheEntryCreatedEvent[_, _]): Unit =
    log(event, s"作成イベント => ${event.getKey + ":" + event.getValue}")

  @CacheEntryRemoved
  def cacheEntryRemoved(event: CacheEntryRemovedEvent[_, _]): Unit =
    log(event, s"削除イベント => ${event.getKey + ":" + event.getValue}, 古い値 => ${event.getOldValue}")

  @CacheEntryModified
  def cacheEntryModified(event: CacheEntryModifiedEvent[_, _]): Unit =
    log(event, s"変更イベント => ${event.getKey + ":" + event.getValue}, isCreated? => ${event.isCreated}")

  @CacheEntryVisited
  def cacheEntryVisited(event: CacheEntryVisitedEvent[_, _]): Unit =
    log(event, s"参照イベント => ${event.getKey + ":" + event.getValue}")

  @CacheEntryLoaded
  def cacheEntryLoaded(event: CacheEntryLoadedEvent[_, _]): Unit =
    log(event, s"ロード完了イベント => ${event.getKey + ":" + event.getValue}")

  /** ひとつのメソッドで、複数のイベントを受け取ることも可能 **/
  @CacheEntryActivated
  @CacheEntryPassivated
  def cacheEntryActivatedOrPassivated(e: CacheEntryEvent[_, _]): Unit =
    e.getType match {
      case Event.Type.CACHE_ENTRY_ACTIVATED =>
        val event = e.asInstanceOf[CacheEntryActivatedEvent[_, _]]
        log(event, s"活性化イベント => ${event.getKey + ":" + event.getValue}")
      case Event.Type.CACHE_ENTRY_PASSIVATED =>
        val event = e.asInstanceOf[CacheEntryPassivatedEvent[_, _]]
        log(event, s"非活性化イベント => ${event.getKey + ":" + event.getValue}")
      case _ => throw new IllegalArgumentException(e.getType.toString)
    }

  @CacheEntriesEvicted
  def cacheEntriesEvicted(event: CacheEntriesEvictedEvent[_, _]): Unit =
    log(event, s"エビクトイベント => ${event.getEntries}")

  @CacheEntryInvalidated
  def cacheEntryInvalidated(event: CacheEntryInvalidatedEvent[_, _]): Unit =
    log(event, s"無効化イベント => ${event.getKey + ":" + event.getValue}")

  @TransactionRegistered
  def transactionRegistered(event: TransactionRegisteredEvent[_, _]): Unit =
    log(event, s"トランザクション登録 => ${event.getGlobalTransaction}, isOriginLocal? => ${event.isOriginLocal}")

  @TransactionCompleted
  def transactionCompleted(event: TransactionCompletedEvent[_, _]): Unit =
    log(event, s"トランザクション完了 => ${event.getGlobalTransaction}, isTransactionSuccessful? => ${event.isTransactionSuccessful}, isOriginLocal? => ${event.isOriginLocal}")

  @DataRehashed
  def dataRehashed(event: DataRehashedEvent[_, _]): Unit =
    log(event, s"データリハッシュ => ${event.getNewTopologyId}, memberAtStart => ${event.getMembersAtStart}, memberAtEnd => ${event.getMembersAtEnd}, getConsistentHashAtStart => ${event.getConsistentHashAtStart}, getConsistentHashAtEnd => ${event.getConsistentHashAtEnd}")

  @TopologyChanged
  def topologyChanged(event: TopologyChangedEvent[_, _]): Unit =
    log(event, s"トポロジ変更 => ${event.getNewTopologyId}, getConsistentHashAtStart => ${event.getConsistentHashAtStart}, getConsistentHashAtEnd => ${event.getConsistentHashAtEnd}")
}
