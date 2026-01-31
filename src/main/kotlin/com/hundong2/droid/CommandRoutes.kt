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

@Serializable
data class ErrorResponse(
    val error: String
)

fun Route.commandRoutes(deviceManager: DeviceManager) {
    route("/command") {
        // Execute shell command
        post("/{serial}") {
            val serial = call.parameters["serial"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Missing device serial")
            )
            
            if (!InputValidator.isValidDeviceSerial(serial)) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Invalid device serial format")
                )
            }
            
            val request = try {
                call.receive<CommandRequest>()
            } catch (e: Exception) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Invalid request body")
                )
            }
            
            // Basic validation - commands should not be empty
            if (request.command.trim().isEmpty()) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Command cannot be empty")
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
            val serial = call.parameters["serial"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Missing serial")
            )
            
            if (!InputValidator.isValidDeviceSerial(serial)) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Invalid device serial format")
                )
            }
            
            val params = call.receiveParameters()
            val apkPath = params["apk_path"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Missing apk_path")
            )
            
            // Validate APK path - must be absolute path with no command injection chars
            if (!InputValidator.isSafeShellString(apkPath) || !apkPath.endsWith(".apk")) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Invalid APK path")
                )
            }
            
            // Use proper shell escaping by quoting the path
            val result = deviceManager.executeCommand(serial, "pm install \"$apkPath\"")
            call.respond(CommandResponse(result.success, result.output, result.error))
        }
        
        post("/{serial}/uninstall") {
            val serial = call.parameters["serial"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Missing serial")
            )
            
            if (!InputValidator.isValidDeviceSerial(serial)) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Invalid device serial format")
                )
            }
            
            val params = call.receiveParameters()
            val packageName = params["package"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Missing package")
            )
            
            // Validate package name
            if (!InputValidator.isValidPackageName(packageName)) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Invalid package name format")
                )
            }
            
            val result = deviceManager.executeCommand(serial, "pm uninstall $packageName")
            call.respond(CommandResponse(result.success, result.output, result.error))
        }
        
        post("/{serial}/reboot") {
            val serial = call.parameters["serial"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Missing serial")
            )
            
            if (!InputValidator.isValidDeviceSerial(serial)) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Invalid device serial format")
                )
            }
            
            val result = deviceManager.executeCommand(serial, "reboot")
            call.respond(CommandResponse(result.success, result.output, result.error))
        }
        
        get("/{serial}/screen") {
            val serial = call.parameters["serial"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Missing serial")
            )
            
            if (!InputValidator.isValidDeviceSerial(serial)) {
                return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Invalid device serial format")
                )
            }
            
            val result = deviceManager.executeCommand(serial, "dumpsys window displays | grep 'init'")
            call.respond(CommandResponse(result.success, result.output, result.error))
        }
        
        post("/{serial}/input") {
            val serial = call.parameters["serial"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Missing serial")
            )
            
            if (!InputValidator.isValidDeviceSerial(serial)) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Invalid device serial format")
                )
            }
            
            val params = call.receiveParameters()
            val text = params["text"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Missing text")
            )
            
            // Escape text for safe shell usage
            val safeText = InputValidator.escapeShellText(text)
            
            val result = deviceManager.executeCommand(serial, "input text \"$safeText\"")
            call.respond(CommandResponse(result.success, result.output, result.error))
        }
        
        post("/{serial}/tap") {
            val serial = call.parameters["serial"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Missing serial")
            )
            
            if (!InputValidator.isValidDeviceSerial(serial)) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Invalid device serial format")
                )
            }
            
            val params = call.receiveParameters()
            val x = params["x"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Missing x coordinate")
            )
            val y = params["y"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Missing y coordinate")
            )
            
            // Validate coordinates are numeric and in reasonable range
            if (!InputValidator.isNumeric(x) || !InputValidator.isNumeric(y)) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Coordinates must be numeric")
                )
            }
            
            if (!InputValidator.isInRange(x, 0, 10000) || !InputValidator.isInRange(y, 0, 10000)) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Coordinates must be within valid screen bounds (0-10000)")
                )
            }
            
            val result = deviceManager.executeCommand(serial, "input tap $x $y")
            call.respond(CommandResponse(result.success, result.output, result.error))
        }
        
        post("/{serial}/swipe") {
            val serial = call.parameters["serial"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Missing serial")
            )
            
            if (!InputValidator.isValidDeviceSerial(serial)) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Invalid device serial format")
                )
            }
            
            val params = call.receiveParameters()
            val x1 = params["x1"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Missing x1")
            )
            val y1 = params["y1"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Missing y1")
            )
            val x2 = params["x2"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Missing x2")
            )
            val y2 = params["y2"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Missing y2")
            )
            val duration = params["duration"] ?: "300"
            
            // Validate all parameters are numeric
            if (!InputValidator.isNumeric(x1) || !InputValidator.isNumeric(y1) ||
                !InputValidator.isNumeric(x2) || !InputValidator.isNumeric(y2) ||
                !InputValidator.isNumeric(duration)) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("All parameters must be numeric")
                )
            }
            
            // Validate coordinates are in reasonable range
            if (!InputValidator.isInRange(x1, 0, 10000) || !InputValidator.isInRange(y1, 0, 10000) ||
                !InputValidator.isInRange(x2, 0, 10000) || !InputValidator.isInRange(y2, 0, 10000)) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Coordinates must be within valid screen bounds (0-10000)")
                )
            }
            
            // Validate duration is in reasonable range (1-10000ms)
            if (!InputValidator.isInRange(duration, 1, 10000)) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Duration must be between 1 and 10000 milliseconds")
                )
            }
            
            val result = deviceManager.executeCommand(serial, "input swipe $x1 $y1 $x2 $y2 $duration")
            call.respond(CommandResponse(result.success, result.output, result.error))
        }
        
        post("/{serial}/keyevent") {
            val serial = call.parameters["serial"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Missing serial")
            )
            
            if (!InputValidator.isValidDeviceSerial(serial)) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Invalid device serial format")
                )
            }
            
            val params = call.receiveParameters()
            val keycode = params["keycode"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Missing keycode")
            )
            
            // Validate keycode is numeric and in valid Android keycode range
            if (!InputValidator.isNumeric(keycode)) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Keycode must be numeric")
                )
            }
            
            if (!InputValidator.isInRange(keycode, 0, 999)) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Keycode must be within valid Android keycode range (0-999)")
                )
            }
            
            val result = deviceManager.executeCommand(serial, "input keyevent $keycode")
            call.respond(CommandResponse(result.success, result.output, result.error))
        }
    }
}
