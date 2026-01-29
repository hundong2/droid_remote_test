package com.hundong2.droid

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class CommandRequest(
    val command: String
)

@Serializable
data class CommandResponse(
    val success: Boolean,
    val output: String,
    val error: String? = null
)

fun Route.commandRoutes(deviceManager: DeviceManager) {
    route("/command") {
        // Execute shell command
        post("/{serial}") {
            val serial = call.parameters["serial"] ?: return@post call.respondText(
                "Missing device serial",
                status = HttpStatusCode.BadRequest
            )
            
            val request = try {
                call.receive<CommandRequest>()
            } catch (e: Exception) {
                return@post call.respondText(
                    "Invalid request body",
                    status = HttpStatusCode.BadRequest
                )
            }
            
            val result = deviceManager.executeCommand(serial, request.command)
            
            val response = CommandResponse(
                success = result.success,
                output = result.output,
                error = result.error
            )
            
            call.respond(response)
        }
        
        // Convenience endpoints for common commands
        post("/{serial}/install") {
            val serial = call.parameters["serial"] ?: return@post call.respondText("Missing serial")
            val apkPath = call.receiveParameters()["apk_path"] ?: return@post call.respondText("Missing apk_path")
            
            val result = deviceManager.executeCommand(serial, "pm install $apkPath")
            call.respond(CommandResponse(result.success, result.output, result.error))
        }
        
        post("/{serial}/uninstall") {
            val serial = call.parameters["serial"] ?: return@post call.respondText("Missing serial")
            val packageName = call.receiveParameters()["package"] ?: return@post call.respondText("Missing package")
            
            val result = deviceManager.executeCommand(serial, "pm uninstall $packageName")
            call.respond(CommandResponse(result.success, result.output, result.error))
        }
        
        post("/{serial}/reboot") {
            val serial = call.parameters["serial"] ?: return@post call.respondText("Missing serial")
            
            val result = deviceManager.executeCommand(serial, "reboot")
            call.respond(CommandResponse(result.success, result.output, result.error))
        }
        
        get("/{serial}/screen") {
            val serial = call.parameters["serial"] ?: return@get call.respondText("Missing serial")
            
            val result = deviceManager.executeCommand(serial, "dumpsys window displays | grep 'init'")
            call.respond(CommandResponse(result.success, result.output, result.error))
        }
        
        post("/{serial}/input") {
            val serial = call.parameters["serial"] ?: return@post call.respondText("Missing serial")
            val text = call.receiveParameters()["text"] ?: return@post call.respondText("Missing text")
            
            val result = deviceManager.executeCommand(serial, "input text \"$text\"")
            call.respond(CommandResponse(result.success, result.output, result.error))
        }
        
        post("/{serial}/tap") {
            val serial = call.parameters["serial"] ?: return@post call.respondText("Missing serial")
            val params = call.receiveParameters()
            val x = params["x"] ?: return@post call.respondText("Missing x coordinate")
            val y = params["y"] ?: return@post call.respondText("Missing y coordinate")
            
            val result = deviceManager.executeCommand(serial, "input tap $x $y")
            call.respond(CommandResponse(result.success, result.output, result.error))
        }
        
        post("/{serial}/swipe") {
            val serial = call.parameters["serial"] ?: return@post call.respondText("Missing serial")
            val params = call.receiveParameters()
            val x1 = params["x1"] ?: return@post call.respondText("Missing x1")
            val y1 = params["y1"] ?: return@post call.respondText("Missing y1")
            val x2 = params["x2"] ?: return@post call.respondText("Missing x2")
            val y2 = params["y2"] ?: return@post call.respondText("Missing y2")
            val duration = params["duration"] ?: "300"
            
            val result = deviceManager.executeCommand(serial, "input swipe $x1 $y1 $x2 $y2 $duration")
            call.respond(CommandResponse(result.success, result.output, result.error))
        }
        
        post("/{serial}/keyevent") {
            val serial = call.parameters["serial"] ?: return@post call.respondText("Missing serial")
            val keycode = call.receiveParameters()["keycode"] ?: return@post call.respondText("Missing keycode")
            
            val result = deviceManager.executeCommand(serial, "input keyevent $keycode")
            call.respond(CommandResponse(result.success, result.output, result.error))
        }
    }
}
