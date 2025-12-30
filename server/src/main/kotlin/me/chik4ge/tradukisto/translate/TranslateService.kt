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

    private data class Prompts(
        val system: String,
        val user: String,
    )

    fun translate(request: TranslateTextRequest): String {
        val prompts = buildPrompts(request)

        return chatClient
            .prompt()
            .system(prompts.system)
            .user(prompts.user)
            .call()
            .content()
            ?.trim()
            .orEmpty()
    }

    fun translateStream(request: TranslateTextRequest): Flux<String> {
        val prompts = buildPrompts(request)

        return chatClient
            .prompt()
            .system(prompts.system)
            .user(prompts.user)
            .stream()
            .content()
            .filter { it != null }
            .map { it!! }
            .index()
            .map { indexed ->
                if (indexed.t1 == 0L) {
                    indexed.t2.trimStart()
                } else {
                    indexed.t2
                }
            }
    }

    private fun buildPrompts(request: TranslateTextRequest): Prompts {
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

        return Prompts(
            system = systemPrompt,
            user = userPrompt,
        )
    }
}
