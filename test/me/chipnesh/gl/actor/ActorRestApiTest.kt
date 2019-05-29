package me.chipnesh.gl.actor

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
import me.chipnesh.gl.actorModule
import me.chipnesh.gl.serializationModule
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class ActorRestApiTest {
    val module: Application.() -> Unit = {
        serializationModule()
        actorModule()
    }

    @Nested
    inner class GetBalanceRequestShould {
        @Test
        fun returnBadRequestWhenWrongIdPassed() {
            withTestApplication(module) {
                runBlocking {
                    handleRequest(HttpMethod.Get, "/account").apply {
                        assertEquals(HttpStatusCode.BadRequest, response.status())
                        assertEquals("Missing 'id' parameter", response.content)
                    }
                }
            }
        }

        @Test
        fun returnNotFoundWhenUnexistentIdPassed() {
            withTestApplication(module) {
                runBlocking {
                    handleRequest(HttpMethod.Get, "/account/1").apply {
                        assertEquals(HttpStatusCode.NotFound, response.status())
                        assertEquals("Account '1' not found", response.content)
                    }
                }
            }
        }

        @Test
        fun returnBalanceWhenAccountExists() {
            withTestApplication(module) {
                runBlocking {
                    val id = UUID.randomUUID().toString()

                    handleRequest(HttpMethod.Post, "/account") {
                        setBody("""{ "id": "$id"}""")
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }

                    handleRequest(HttpMethod.Get, "/account/$id").apply {
                        assertEquals(HttpStatusCode.OK, response.status())
                        assertEquals(
                            """
                            |{
                            |  "id" : "$id",
                            |  "balance" : 0
                            |}
                            """.trimMargin(), response.content
                        )
                    }
                }
            }
        }
    }

    @Nested
    inner class DepositRequestShould {
        @Test
        fun returnBadRequestWhenRequestBodyIsMissing() {
            withTestApplication(module) {
                runBlocking {
                    handleRequest(HttpMethod.Put, "/account/id/deposit").apply {
                        assertEquals(HttpStatusCode.BadRequest, response.status())
                        assertEquals("Missing deposit cash request body", response.content)
                    }
                }
            }
        }

        @Test
        fun returnNotFoundWhenUnexistentIdPassed() {
            withTestApplication(module) {
                runBlocking {
                    handleRequest(HttpMethod.Put, "/account/wrongId/deposit") {
                        setBody("""{ "amount": "50"}""")
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }.apply {
                        assertEquals(HttpStatusCode.NotFound, response.status())
                        assertEquals("Account 'wrongId' not found", response.content)
                    }
                }
            }
        }


        @Test
        fun depositMoneyToAccountWhenRequestIsOk() {
            withTestApplication(module) {
                runBlocking {
                    val id = UUID.randomUUID().toString()

                    handleRequest(HttpMethod.Post, "/account") {
                        setBody("""{ "id": "$id"}""")
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }

                    handleRequest(HttpMethod.Put, "/account/$id/deposit") {
                        setBody("""{ "amount": "50"}""")
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }

                    handleRequest(HttpMethod.Get, "/account/$id").apply {
                        assertEquals(HttpStatusCode.OK, response.status())
                        assertEquals(
                            """
                            |{
                            |  "id" : "$id",
                            |  "balance" : 50
                            |}
                            """.trimMargin(), response.content
                        )
                    }
                }
            }
        }
    }

    @Nested
    inner class WithdrawRequestShould {
        @Test
        fun returnBadRequestWhenRequestBodyIsMissing() {
            withTestApplication(module) {
                runBlocking {
                    handleRequest(HttpMethod.Put, "/account/id/withdraw").apply {
                        assertEquals(HttpStatusCode.BadRequest, response.status())
                        assertEquals("Missing withdraw cash request body", response.content)
                    }
                }
            }
        }

        @Test
        fun returnNotFoundWhenUnexistentIdPassed() {
            withTestApplication(module) {
                runBlocking {
                    handleRequest(HttpMethod.Put, "/account/wrongId/withdraw") {
                        setBody("""{ "amount": "50"}""")
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }.apply {
                        assertEquals(HttpStatusCode.NotFound, response.status())
                        assertEquals("Account 'wrongId' not found", response.content)
                    }
                }
            }
        }

        @Test
        fun withdrawMoneyFromAccountWhenRequestIsOk() {
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

                    handleRequest(HttpMethod.Get, "/account/$id").apply {
                        assertEquals(HttpStatusCode.OK, response.status())
                        assertEquals(
                            """
                            |{
                            |  "id" : "$id",
                            |  "balance" : 20
                            |}
                            """.trimMargin(), response.content
                        )
                    }
                }
            }
        }
    }

    @Nested
    inner class TransferRequestShould {
        @Test
        fun returnBadRequestWhenWrongIdPassed() {
            withTestApplication(module) {
                runBlocking {
                    handleRequest(HttpMethod.Get, "/transfer/").apply {
                        assertEquals(HttpStatusCode.BadRequest, response.status())
                        assertEquals("Missing 'id' parameter", response.content)
                    }
                }
            }
        }

        @Test
        fun returnNotFoundWhenUnexistentIdPassed() {
            withTestApplication(module) {
                runBlocking {
                    handleRequest(HttpMethod.Get, "/transfer/wrongId").apply {
                        assertEquals(HttpStatusCode.NotFound, response.status())
                        assertEquals("Transfer 'wrongId' not found", response.content)
                    }
                }
            }
        }

        @Test
        fun createTransferWhenRequestIsCorrect() {
            withTestApplication(module) {
                runBlocking {
                    val id = UUID.randomUUID().toString()
                    val from = UUID.randomUUID().toString()
                    val to = UUID.randomUUID().toString()

                    handleRequest(HttpMethod.Post, "/transfer") {
                        setBody(
                            """
                            |{
                            |  "id" : "$id",
                            |  "from" : "$from",
                            |  "to" : "$to",
                            |  "amount" : 10
                            |}
                            """.trimMargin()
                        )
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }.apply {
                        assertEquals(HttpStatusCode.Created, response.status())
                    }
                }
            }
        }

        @Test
        fun returnTransferWhenTransferExists() {
            withTestApplication(module) {
                runBlocking {
                    val id = UUID.randomUUID().toString()
                    val from = UUID.randomUUID().toString()
                    val to = UUID.randomUUID().toString()

                    handleRequest(HttpMethod.Post, "/transfer") {
                        setBody(
                            """
                            |{
                            |  "id" : "$id",
                            |  "from" : "$from",
                            |  "to" : "$to",
                            |  "amount" : 10
                            |}
                            """.trimMargin()
                        )
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }.apply {
                        assertEquals(HttpStatusCode.Created, response.status())
                    }

                    delay(100)

                    handleRequest(HttpMethod.Get, "/transfer/$id").apply {
                        assertEquals(HttpStatusCode.OK, response.status())
                        assertEquals(
                            """
                            |{
                            |  "transferId" : "$id",
                            |  "status" : "FAILED"
                            |}
                            """.trimMargin(),
                            response.content
                        )
                    }
                }
            }
        }
    }
}