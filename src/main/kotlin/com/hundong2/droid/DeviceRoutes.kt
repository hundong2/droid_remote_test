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
                DeviceInfo(
                    serial = device.serial,
                    state = device.state.toString(),
                    model = null,
                    product = null,
                    device = null,
                    transportId = null
                )
            }
            call.respond(deviceInfos)
        }
        
        get("/{serial}/properties") {
            val serial = call.parameters["serial"] ?: return@get call.respondText("Missing serial")
            val properties = deviceManager.getDeviceProperties(serial)
            call.respond(properties)
        }
    }
}
