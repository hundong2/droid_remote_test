package com.droid.remote

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*

fun main() {
    embeddedServer(Netty, port = 8080) {
        configureRouting()
    }.start(wait = true)
}

fun Application.configureRouting() {
    install(ContentNegotiation) {
        json()
    }
    
    routing {
        get("/") {
            call.respondText("Droid Remote Test Service is running!")
        }
        
        get("/health") {
            call.respondText("OK")
        }
        
        get("/api/status") {
            call.respond(mapOf(
                "status" to "running",
                "service" to "droid-remote-test",
                "version" to "1.0.0"
            ))
        }
    }
}
