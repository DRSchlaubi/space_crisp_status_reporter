package me.schlaubi.crisp

import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

internal val WebhookPayloadModule = SerializersModule {
    polymorphic(WebhookPayload::class) {
        subclass(StatusPageHealthChangePayload::class, StatusPageHealthChangePayload.serializer())
    }
}

interface WebhookPayload {
    @SerialName("website_id")
    val websiteId: String
    val data: WebhookPayloadData

    @Contextual
    val timestamp: Instant
}

@Serializable
@SerialName("status:health:changed")
data class StatusPageHealthChangePayload(
    @SerialName("website_id")
    override val websiteId: String,
    override val data: StatusPageHealthChangeData,
    @Contextual
    override val timestamp: Instant
) : WebhookPayload

interface WebhookPayloadData

@Serializable
data class StatusPageHealthChangeData(
    @SerialName("website_id")
    val websiteId: String,
    val health: Health,
    val nodes: List<Node>
) : WebhookPayloadData {
    @Serializable
    enum class Health {
        @SerialName("healthy")
        HEALTHY,

        @SerialName("sick")
        SICK,

        @SerialName("dead")
        DEAD
    }

    @Serializable
    data class Node(
        val label: String,
        val replica: String
    )
}
