package me.schlaubi

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.features.*
import io.ktor.features.ContentTransformationException
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.modules.plus
import me.schlaubi.config.Config
import me.schlaubi.crisp.StatusPageHealthChangePayload
import me.schlaubi.crisp.WebhookPayload
import me.schlaubi.crisp.WebhookPayloadModule
import me.schlaubi.json.InstantSerializer
import space.jetbrains.api.runtime.SpaceHttpClient
import space.jetbrains.api.runtime.resources.chats
import space.jetbrains.api.runtime.withServiceAccountTokenSource

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused", "UNUSED_PARAMETER") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    val spaceClient = SpaceHttpClient(HttpClient(OkHttp))
        .withServiceAccountTokenSource(
            Config.SPACE_CLIENT_ID,
            Config.SPACE_CLIENT_SECRET,
            Config.SPACE_URL
        )
    install(ContentNegotiation) {
        json(Json {
            classDiscriminator = "event"
            serializersModule = SerializersModule {
                contextual(InstantSerializer)
            } + WebhookPayloadModule
        })
    }

    routing {
        post("/crisp/receive") {
            if (context.request.queryParameters["key"] != Config.CRISP_SECRET) {
                return@post context.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
            }
            val payload = try {
                context.receive<WebhookPayload>()
            } catch (e: ContentTransformationException) {
                return@post context.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Bad Request", "message" to e.message)
                )
            }

            if (payload !is StatusPageHealthChangePayload) {
                return@post context.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Bad Request", "message" to "Invalid payload")
                )
            }

            spaceClient.chats.channels.messages.sendTextMessage(
                Config.SPACE_CHANNEL_ID,
                buildStatusMessage(payload)
            )

            context.respond(HttpStatusCode.Accepted, "OK")
        }
    }
}

private fun buildStatusMessage(event: StatusPageHealthChangePayload): String = """
    **Nodes**
    ${if(event.data.nodes.isEmpty()) "ALL" else event.data.nodes.joinToString("`, `", "`", "`") { "${it.label}:${it.replica}" }}
    **Status**
    ${event.data.health}
    **Time**
    ${event.timestamp}
    **Monitor Page**
    ${event.websiteId}
""".trimIndent()
