package me.chik4ge.tradukisto.translate

import me.chik4ge.tradukisto.openapi.model.TranslateTextRequest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@SpringBootTest(
    properties = [
        "app.auth.bearer-token=test-token",
    ],
)
@AutoConfigureMockMvc
class TranslateControllerTests {
    @Autowired
    private lateinit var mockMvc: MockMvc

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

        mockMvc
            .post("/api/v1/translate") {
                contentType = MediaType.APPLICATION_JSON
                content = body
            }.andExpect {
                status { isUnauthorized() }
            }

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

        mockMvc
            .post("/api/v1/translate") {
                header("Authorization", "Bearer test-token")
                contentType = MediaType.APPLICATION_JSON
                content = body
            }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.translatedText") { value("こんにちは、世界！") }
            }
    }
}
