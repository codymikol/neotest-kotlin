package org.example

import io.kotest.core.spec.style.FunSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureWebTestClient
class SpringFunSpec(
    private val webTestClient: WebTestClient,
) : FunSpec({
        test("GET /actuator/health") {
            webTestClient
                .get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo("UP")
        }
    })
