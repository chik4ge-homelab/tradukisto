package me.chik4ge.tradukisto.translate

import me.chik4ge.tradukisto.openapi.model.TranslateTextRequest
import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class TranslateService(
    chatClientBuilder: ChatClient.Builder,
) {
    private val chatClient = chatClientBuilder.build()

    fun translate(request: TranslateTextRequest): String {
        val systemPrompt =
            """
            <|plamo:op|>dataset
            translation
            """.trimIndent()

        val userPrompt =
            """
            <|plamo:op|>input lang=${request.sourceLanguage}
            ${request.text}
            <|plamo:op|>output lang=${request.targetLanguage}
            """.trimIndent()

        return chatClient
            .prompt()
            .system(systemPrompt)
            .user(userPrompt)
            .call()
            .content()
            ?.trim()
            .orEmpty()
    }

    fun translateStream(request: TranslateTextRequest): Flux<String> {
        val systemPrompt =
            """
            <|plamo:op|>dataset
            translation
            """.trimIndent()

        val userPrompt =
            """
            <|plamo:op|>input lang=${request.sourceLanguage}
            ${request.text}
            <|plamo:op|>output lang=${request.targetLanguage}
            """.trimIndent()

        return chatClient
            .prompt()
            .system(systemPrompt)
            .user(userPrompt)
            .stream()
            .content()
            .filter { it != null }
            .map { it!! }
    }
}
