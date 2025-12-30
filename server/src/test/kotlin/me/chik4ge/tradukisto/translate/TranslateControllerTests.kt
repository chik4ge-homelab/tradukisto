package me.chik4ge.tradukisto.translate

import me.chik4ge.tradukisto.openapi.model.TranslateTextRequest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString

@SpringBootTest(
    properties = [
        "app.auth.bearer-token=test-token",
    ],
)
@AutoConfigureWebTestClient
class TranslateControllerTests {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockitoBean
    private lateinit var translateService: TranslateService

    @Test
    fun `Bearerが無い場合は401になる`() {
        val body =
            """
            {
              "sourceLanguage": "English",
              "targetLanguage": "Japanese",
              "text": "Hello, world!"
            }
            """.trimIndent()

        webTestClient
            .post()
            .uri("/api/v1/translate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isUnauthorized

        verifyNoInteractions(translateService)
    }

    @Test
    fun `Bearerが一致すれば翻訳結果を返す`() {
        val body =
            """
            {
              "sourceLanguage": "English",
              "targetLanguage": "Japanese",
              "text": "Hello, world!"
            }
            """.trimIndent()

        `when`(translateService.translate(TranslateTextRequest("English", "Japanese", "Hello, world!")))
            .thenReturn("こんにちは、世界！")

        webTestClient
            .post()
            .uri("/api/v1/translate")
            .header("Authorization", "Bearer test-token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.translatedText")
            .isEqualTo("こんにちは、世界！")
    }

    @Test
    fun `streamエンドポイントならSSEで返す`() {
        val body =
            """
            {
              "sourceLanguage": "English",
              "targetLanguage": "Japanese",
              "text": "Hello, world!"
            }
            """.trimIndent()

        `when`(translateService.translateStream(TranslateTextRequest("English", "Japanese", "Hello, world!")))
            .thenReturn(Flux.just("こんにちは、", "世界！"))

        val responseBody =
            webTestClient
                .post()
                .uri("/api/v1/translate/stream")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isOk
                .expectHeader()
                .contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .returnResult(String::class.java)
                .responseBody
                .collectList()
                .block()
                ?.joinToString("")
                .orEmpty()

        assertThat(responseBody, containsString("こんにちは、"))
    }
}
