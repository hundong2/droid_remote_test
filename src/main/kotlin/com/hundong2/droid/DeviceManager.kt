package com.hundong2.droid

import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.adam.AndroidDebugBridgeClientFactory
import com.malinskiy.adam.request.device.Device
import com.malinskiy.adam.request.device.ListDevicesRequest
import com.malinskiy.adam.request.logcat.ChanneledLogcatRequest
import com.malinskiy.adam.request.shell.v2.ShellCommandRequest
import com.malinskiy.adam.request.shell.v2.ShellCommandResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class DeviceManager {
    private val adbClient: AndroidDebugBridgeClient = AndroidDebugBridgeClientFactory().build()
    
    /**
     * Get list of connected devices
     */
    suspend fun getDevices(): List<Device> {
        return try {
            adbClient.execute(ListDevicesRequest())
        } catch (e: Exception) {
            logger.error(e) { "Failed to get devices" }
            emptyList()
        }
    }
    
    /**
     * Get logcat stream for a specific device
     * Returns a Flow of log entries
     */
    fun getLogcatFlow(serial: String, scope: CoroutineScope): Flow<String> = flow {
        try {
            val request = ChanneledLogcatRequest()
            val channel: ReceiveChannel<String> = adbClient.execute(
                request = request,
                scope = scope,
                serial = serial
            )
            
            // Emit each log line from the channel
            for (logLine in channel) {
                emit(logLine)
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to get logcat for device $serial" }
            emit("ERROR: ${e.message}")
        }
    }
    
    /**
     * Execute shell command on device
     */
    suspend fun executeCommand(serial: String, command: String): CommandResult {
        return try {
            val request = ShellCommandRequest(command)
            val result: ShellCommandResult = adbClient.execute(request, serial)
            
            CommandResult(
                success = result.exitCode == 0,
                output = result.output,
                error = if (result.exitCode != 0) result.errorOutput else null
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to execute command on device $serial: $command" }
            CommandResult(
                success = false,
                output = "",
                error = e.message
            )
        }
    }
    
    /**
     * Execute shell command and get output as flow
     */
    suspend fun executeCommandFlow(serial: String, command: String): Flow<String> = flow {
        try {
            val request = ShellCommandRequest(command)
            val result: ShellCommandResult = adbClient.execute(request, serial)
            emit(result.output)
        } catch (e: Exception) {
            logger.error(e) { "Failed to execute command on device $serial: $command" }
            emit("ERROR: ${e.message}")
        }
    }
    
    /**
     * Get device properties
     */
    suspend fun getDeviceProperties(serial: String): Map<String, String> {
        return try {
            val result: ShellCommandResult = adbClient.execute(ShellCommandRequest("getprop"), serial)
            parseProperties(result.output)
        } catch (e: Exception) {
            logger.error(e) { "Failed to get properties for device $serial" }
            emptyMap()
        }
    }
    
    private fun parseProperties(output: String): Map<String, String> {
        val properties = mutableMapOf<String, String>()
        val regex = """\[(.+?)\]:\s*\[(.+?)\]""".toRegex()
        
        output.lines().forEach { line ->
            regex.find(line)?.let { match ->
                val key = match.groupValues[1]
                val value = match.groupValues[2]
                properties[key] = value
            }
        }
        
        return properties
    }
    
    fun close() {
        adbClient.close()
    }
}

data class CommandResult(
    val success: Boolean,
    val output: String,
    val error: String?
)
