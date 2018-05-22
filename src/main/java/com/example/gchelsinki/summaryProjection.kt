package com.example.gchelsinki

import org.axonframework.config.EventHandlingConfiguration
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.Timestamp
import org.axonframework.queryhandling.QueryHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.EntityManager
import javax.persistence.Id

data class DataQuery(val offset: Int, val limit: Int)
class SizeQuery() { override fun toString() = "SizeQuery()" }

@Entity
data class GiftCardSummary(@Id var id: String? = null,
        var initialBalance: Int = 0,
        var issuedAt: Instant? = null,
        var currentBalance: Int = 0) {
    fun redeem(value: Int) {
        currentBalance -= value
    }
}

@Component
@Profile("query")
class GiftCardSummaryProjection(val entityManager: EntityManager) {

    @EventHandler
    fun on(evt: IssuedEvt, @Timestamp timestamp: Instant) {
        entityManager.persist(GiftCardSummary(
                evt.id,
                evt.value,
                timestamp,
                evt.value
        ))
    }

    @EventHandler
    fun on(evt: RedeemedEvt) {
        entityManager.find(GiftCardSummary::class.java, evt.id)
                .redeem(evt.value)
    }

    @QueryHandler
    fun handle(query: SizeQuery): Int {
        return entityManager.createQuery<java.lang.Long>(
                "SELECT COUNT (c) FROM GiftCardSummary c", java.lang.Long::class.java
        ).singleResult.toInt()
    }

    @QueryHandler
    fun handle(query: DataQuery): List<GiftCardSummary> {
        return entityManager.createQuery(
                "SELECT c FROM GiftCardSummary c ORDER BY c.id", GiftCardSummary::class.java
        ).resultList
    }

    @Autowired
    fun configure(config: EventHandlingConfiguration) {
        config.registerTrackingProcessor(javaClass.`package`.name)
    }

}
