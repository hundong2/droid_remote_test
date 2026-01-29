package com.hundong2.droid

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.time.Duration

private val logger = KotlinLogging.logger {}

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    install(CORS) {
        anyHost()
        allowHeader("Content-Type")
    }

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    val deviceManager = DeviceManager()
    
    routing {
        get("/") {
            call.respondText("Droid Remote Test Server - Ktor + Adam")
        }
        
        // Device routes
        deviceRoutes(deviceManager)
        
        // Logcat routes
        logcatRoutes(deviceManager)
        
        // Command routes
        commandRoutes(deviceManager)
    }
    
    logger.info { "Droid Remote Test Server started on port 8080" }
}
