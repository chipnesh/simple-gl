package me.chipnesh.gl

import io.ktor.application.Application
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApiTest {

    private val modules = arrayOf<Application.() -> Unit>(
        {
            serializationModule()
            actorModule()
        }
        ,
        {
            serializationModule()
            axonModule()
        }
    )

    @Test
    fun testGetBalanceUseCase() {
        modules.forEach { module ->
            withTestApplication(module) {
                runBlocking {
                    val id = UUID.randomUUID().toString()

                    // check getting an account without id specified
                    handleRequest(HttpMethod.Get, "/account").apply {
                        assertEquals(HttpStatusCode.BadRequest, response.status())
                        assertEquals("Missing 'id' parameter", response.content)
                    }

                    // check getting an account which does not exist
                    handleRequest(HttpMethod.Get, "/account/1").apply {
                        assertEquals(HttpStatusCode.NotFound, response.status())
                        assertEquals("Account '1' not found", response.content)
                    }

                    // creating an account
                    handleRequest(HttpMethod.Post, "/account") {
                        setBody("""{ "id": "$id"}""")
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }

                    delay(500) // cq(R)s infrastructure penalty

                    // ensure we can get account by id
                    handleRequest(HttpMethod.Get, "/account/$id").apply {
                        assertEquals(HttpStatusCode.OK, response.status())
                        assertEquals(
                            """{
  "id" : "$id",
  "balance" : 0
}""", response.content
                        )
                    }
                }
            }
        }
    }

    @Test
    fun testDepositUseCase() {
        modules.forEach { module ->
            withTestApplication(module) {
                runBlocking {
                    val id = UUID.randomUUID().toString()

                    // creating an account
                    handleRequest(HttpMethod.Post, "/account") {
                        setBody("""{ "id": "$id"}""")
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }

                    // depositing an account
                    handleRequest(HttpMethod.Put, "/account/$id/deposit") {
                        setBody("""{ "amount": "50"}""")
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }

                    delay(500) // cq(R)s infrastructure penalty

                    // ensure we can get account by id
                    handleRequest(HttpMethod.Get, "/account/$id").apply {
                        assertEquals(HttpStatusCode.OK, response.status())
                        assertEquals(
                            """{
  "id" : "$id",
  "balance" : 50
}""", response.content
                        )
                    }
                }
            }
        }
    }

    @Test
    fun testWithdrawUseCase() {
        modules.forEach { module ->
            withTestApplication(module) {
                runBlocking {
                    val id = UUID.randomUUID().toString()

                    // creating an account
                    handleRequest(HttpMethod.Post, "/account") {
                        setBody("""{ "id": "$id"}""")
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }

                    // depositing an account
                    handleRequest(HttpMethod.Put, "/account/$id/deposit") {
                        setBody("""{ "amount": "50"}""")
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }

                    // withdrawing an account
                    handleRequest(HttpMethod.Put, "/account/$id/withdraw") {
                        setBody("""{ "amount": "30"}""")
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }

                    delay(500) // cq(R)s infrastructure penalty

                    // ensure we can get account by id
                    handleRequest(HttpMethod.Get, "/account/$id").apply {
                        assertEquals(HttpStatusCode.OK, response.status())
                        assertEquals(
                            """{
  "id" : "$id",
  "balance" : 20
}""", response.content
                        )
                    }
                }
            }
        }
    }

    @Test
    fun testTransferUseCase() {
        modules.forEach { module ->
            withTestApplication(module) {
                runBlocking {
                    val id = UUID.randomUUID().toString()
                    val from = UUID.randomUUID().toString()
                    val to = UUID.randomUUID().toString()

                    // check getting a transfer without id specified
                    handleRequest(HttpMethod.Get, "/transfer/$id").apply {
                        assertEquals(HttpStatusCode.NotFound, response.status())
                        assertEquals("Transfer '$id' not found", response.content)
                    }

                    // ensure account created
                    handleRequest(HttpMethod.Post, "/transfer") {
                        setBody("""{"id":"$id","from": "$from","to": "$to","amount": 10}""")
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }.apply {
                        assertEquals(HttpStatusCode.Created, response.status())
                    }

                    // issue with axon's in memory event storage (duplicates check)
                    // so we check from cq(R)s projection
                    delay(500) // cq(R)s infrastructure penalty

                    // creation of transfer with existent id must be prohibited
                    handleRequest(HttpMethod.Post, "/transfer") {
                        setBody("""{"id":"$id","from": "$from","to": "$to","amount": 10}""")
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }.apply {
                        assertEquals(HttpStatusCode.Conflict, response.status())
                        assertEquals("Transfer '$id' already exists", response.content)
                    }
                }
            }
        }
    }
}
