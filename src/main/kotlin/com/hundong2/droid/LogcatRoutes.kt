package com.hundong2.droid

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

fun Route.logcatRoutes(deviceManager: DeviceManager) {
    route("/logcat") {
        // WebSocket endpoint for real-time logcat streaming
        webSocket("/{serial}") {
            val serial = call.parameters["serial"] ?: run {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Missing device serial"))
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
            val serial = call.parameters["serial"] ?: return@get call.respondText("Missing serial")
            val lines = call.request.queryParameters["lines"]?.toIntOrNull() ?: 100
            
            val result = deviceManager.executeCommand(serial, "logcat -d -t $lines")
            
            if (result.success) {
                call.respondText(result.output)
            } else {
                call.respondText("Error: ${result.error}", status = io.ktor.http.HttpStatusCode.InternalServerError)
            }
        }
        
        // Clear logcat
        post("/{serial}/clear") {
            val serial = call.parameters["serial"] ?: return@post call.respondText("Missing serial")
            
            val result = deviceManager.executeCommand(serial, "logcat -c")
            
            if (result.success) {
                call.respondText("Logcat cleared successfully")
            } else {
                call.respondText("Error: ${result.error}", status = io.ktor.http.HttpStatusCode.InternalServerError)
            }
        }
    }
}
