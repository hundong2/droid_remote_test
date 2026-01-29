package com.droid.remote

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        application {
            configureRouting()
        }
        
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Droid Remote Test Service is running!", bodyAsText())
        }
    }
    
    @Test
    fun testHealth() = testApplication {
        application {
            configureRouting()
        }
        
        client.get("/health").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("OK", bodyAsText())
        }
    }
}
