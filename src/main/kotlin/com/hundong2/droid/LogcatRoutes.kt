package com.hundong2.droid

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import mu.KotlinLogging
import kotlinx.serialization.Serializable

private val logger = KotlinLogging.logger {}

@Serializable
data class LogcatErrorResponse(
    val error: String
)

fun Route.logcatRoutes(deviceManager: DeviceManager) {
    route("/logcat") {
        // WebSocket endpoint for real-time logcat streaming
        webSocket("/{serial}") {
            val serial = call.parameters["serial"] ?: run {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Missing device serial"))
                return@webSocket
            }
            
            if (!InputValidator.isValidDeviceSerial(serial)) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Invalid device serial format"))
                return@webSocket
            }
            
            logger.info { "Starting logcat stream for device: $serial" }
            
            try {
                deviceManager.getLogcatFlow(serial, this as CoroutineScope)
                    .onEach { logLine ->
                        send(Frame.Text(logLine))
                    }
                    .catch { e ->
                        logger.error(e) { "Error in logcat stream for $serial" }
                        send(Frame.Text("ERROR: ${e.message}"))
                    }
                    .collect()
            } catch (e: ClosedReceiveChannelException) {
                logger.info { "Logcat stream closed for device: $serial" }
            } catch (e: Exception) {
                logger.error(e) { "Unexpected error in logcat stream for $serial" }
            } finally {
                logger.info { "Logcat stream ended for device: $serial" }
            }
        }
        
        // HTTP endpoint for getting recent logs (non-streaming)
        get("/{serial}/recent") {
            val serial = call.parameters["serial"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                LogcatErrorResponse("Missing serial")
            )
            
            if (!InputValidator.isValidDeviceSerial(serial)) {
                return@get call.respond(
                    HttpStatusCode.BadRequest,
                    LogcatErrorResponse("Invalid device serial format")
                )
            }
            
            val linesParam = call.request.queryParameters["lines"]
            
            if (linesParam != null && !InputValidator.isNumeric(linesParam)) {
                return@get call.respond(
                    HttpStatusCode.BadRequest,
                    LogcatErrorResponse("Lines parameter must be numeric")
                )
            }

            val lines = linesParam?.toIntOrNull() ?: 100

            // Limit to reasonable maximum
            val validLines = lines.coerceIn(1, 10000)
            
            val result = deviceManager.executeCommand(serial, "logcat -d -t $validLines")
            
            if (result.success) {
                call.respondText(result.output)
            } else {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    LogcatErrorResponse(result.error ?: "Failed to fetch logcat")
                )
            }
        }
        
        // Clear logcat
        post("/{serial}/clear") {
            val serial = call.parameters["serial"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                LogcatErrorResponse("Missing serial")
            )
            
            if (!InputValidator.isValidDeviceSerial(serial)) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    LogcatErrorResponse("Invalid device serial format")
                )
            }
            
            val result = deviceManager.executeCommand(serial, "logcat -c")
            
            if (result.success) {
                call.respondText("Logcat cleared successfully")
            } else {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    LogcatErrorResponse(result.error ?: "Failed to clear logcat")
                )
            }
        }
    }
}
