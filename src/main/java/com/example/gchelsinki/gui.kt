package com.example.gchelsinki

import com.vaadin.data.provider.AbstractBackEndDataProvider
import com.vaadin.data.provider.DataProvider
import com.vaadin.data.provider.Query
import com.vaadin.server.DefaultErrorHandler
import com.vaadin.server.VaadinRequest
import com.vaadin.spring.annotation.SpringUI
import com.vaadin.ui.*
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.queryhandling.responsetypes.ResponseTypes
import java.util.stream.Stream

@SpringUI
class GiftCardGUI(val commandGateway: CommandGateway,
                  queryGateway: QueryGateway) : UI() {

    val dataProvider: DataProvider<GiftCardSummary, Void>

    init {
        this.dataProvider = dataProvider(queryGateway)
    }

    private fun dataProvider(queryGateway: QueryGateway): DataProvider<GiftCardSummary, Void> {
        return object : AbstractBackEndDataProvider<GiftCardSummary, Void>() {
            override fun fetchFromBackEnd(query: Query<GiftCardSummary, Void>): Stream<GiftCardSummary> {
                return queryGateway
                        .query(
                                DataQuery(query.offset, query.limit),
                                ResponseTypes.multipleInstancesOf(GiftCardSummary::class.java))
                        .join()
                        .stream()
            }

            override fun sizeInBackEnd(query: Query<GiftCardSummary, Void>): Int {
                return queryGateway
                        .query(SizeQuery(),
                                ResponseTypes.instanceOf(Int::class.java))
                        .join()
            }
        }
    }

    override fun init(vaadinRequest: VaadinRequest) {

        val commands = HorizontalLayout()
        commands.setSizeFull()
        commands.addComponents(issuePanel(), redeemPanel())

        val layout = VerticalLayout()
        layout.setSizeFull()
        layout.addComponents(commands, summaryGrid())

        content = layout

        errorHandler = object : DefaultErrorHandler() {
            override fun error(event: com.vaadin.server.ErrorEvent) {
                var cause = event.throwable
                while (cause.cause != null) cause = cause.cause
                Notification.show("Error", cause.message, Notification.Type.ERROR_MESSAGE)
            }
        }

    }

    private fun issuePanel(): Panel {
        val id = TextField("Id")
        val value = TextField("value")
        val submit = Button("submit")

        submit.addClickListener { evt ->
            val idStr = id.value
            val `val` = Integer.parseInt(value.value)
            commandGateway.sendAndWait<Any>(IssueCmd(idStr, `val`))
            Notification.show("Success", Notification.Type.HUMANIZED_MESSAGE)
                    .addCloseListener { closeEvt -> dataProvider.refreshAll() }
        }

        val form = FormLayout()
        form.setMargin(true)
        form.addComponents(id, value, submit)

        val panel = Panel("Issue")
        panel.content = form
        return panel
    }

    private fun redeemPanel(): Panel {
        val id = TextField("Id")
        val value = TextField("value")
        val submit = Button("submit")

        submit.addClickListener { evt ->
            val idStr = id.value
            val `val` = Integer.parseInt(value.value)
            commandGateway.sendAndWait<Any>(RedeemCmd(idStr, `val`))
            Notification.show("Success", Notification.Type.HUMANIZED_MESSAGE)
                    .addCloseListener { closeEvt -> dataProvider.refreshAll() }

        }

        val form = FormLayout()
        form.setMargin(true)
        form.addComponents(id, value, submit)

        val panel = Panel("Redeem")
        panel.content = form
        return panel
    }

    private fun summaryGrid(): Grid<GiftCardSummary> {
        return Grid<GiftCardSummary>().apply {
            addColumn { it.id }.setCaption("ID")
            addColumn { it.initialBalance }.setCaption("Initial Balance")
            addColumn { it.issuedAt }.setCaption("Issued At")
            addColumn { it.currentBalance }.setCaption("Current Balance")
            dataProvider = this@GiftCardGUI.dataProvider
            setSizeFull()
        }
    }

}
