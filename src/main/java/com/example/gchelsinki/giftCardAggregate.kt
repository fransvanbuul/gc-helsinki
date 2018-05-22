package com.example.gchelsinki

import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.TargetAggregateIdentifier
import org.axonframework.commandhandling.model.AggregateIdentifier
import org.axonframework.commandhandling.model.AggregateLifecycle.apply
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.spring.stereotype.Aggregate
import org.springframework.context.annotation.Profile

data class IssueCmd(@TargetAggregateIdentifier val id: String, val value: Int)
data class RedeemCmd(@TargetAggregateIdentifier val id: String, val value: Int)
data class IssuedEvt(val id: String, val value: Int)
data class RedeemedEvt(val id: String, val value: Int)

@Aggregate
@Profile("command")
class GiftCard {

    @AggregateIdentifier
    private var id: String? = null
    private var balance: Int = 0

    constructor() {}

    @CommandHandler
    constructor(cmd: IssueCmd) {
        if (cmd.value <= 0)
            throw IllegalArgumentException("value <= 0")
        apply(IssuedEvt(cmd.id, cmd.value))
    }

    @CommandHandler
    fun handle(cmd: RedeemCmd) {
        if (cmd.value <= 0)
            throw IllegalArgumentException("value <= 0")
        if (cmd.value > balance)
            throw IllegalArgumentException("value > balance")
        apply(RedeemedEvt(cmd.id, cmd.value))
    }

    @EventSourcingHandler
    fun on(evt: IssuedEvt) {
        this.id = evt.id
        this.balance = evt.value
    }

    @EventSourcingHandler
    fun on(evt: RedeemedEvt) {
        this.balance -= evt.value
    }

}
