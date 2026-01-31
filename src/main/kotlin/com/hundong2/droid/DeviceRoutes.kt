package com.hundong2.droid

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class DeviceInfo(
    val serial: String,
    val state: String,
    val model: String? = null,
    val product: String? = null,
    val device: String? = null,
    val transportId: String? = null
)

fun Route.deviceRoutes(deviceManager: DeviceManager) {
    route("/devices") {
        get {
            val devices = deviceManager.getDevices()
            val deviceInfos = devices.map { device ->
                // Get basic device properties
                val properties = try {
                    deviceManager.getDeviceProperties(device.serial)
                } catch (e: Exception) {
                    emptyMap()
                }
                
                DeviceInfo(
                    serial = device.serial,
                    state = device.state.toString(),
                    model = properties["ro.product.model"],
                    product = properties["ro.product.name"],
                    device = properties["ro.product.device"],
                    transportId = null
                )
            }
            call.respond(deviceInfos)
        }
        
        get("/{serial}/properties") {
            val serial = call.parameters["serial"]
            if (serial.isNullOrBlank()) {
                call.respond(mapOf("error" to "Missing serial parameter"))
                return@get
            }

            if (!InputValidator.isValidDeviceSerial(serial)) {
                call.respond(mapOf("error" to "Invalid device serial"))
                return@get
            }
            val properties = deviceManager.getDeviceProperties(serial)
            call.respond(properties)
        }
    }
}
