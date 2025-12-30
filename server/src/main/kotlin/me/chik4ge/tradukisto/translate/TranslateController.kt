package me.chik4ge.tradukisto.translate

import jakarta.validation.Valid
import me.chik4ge.tradukisto.openapi.TranslateApi
import me.chik4ge.tradukisto.openapi.model.TranslateTextRequest
import me.chik4ge.tradukisto.openapi.model.TranslateTextResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class TranslateController(
    private val translateService: TranslateService,
) : TranslateApi {
    override fun translateText(
        @Valid @RequestBody translateTextRequest: TranslateTextRequest,
    ): ResponseEntity<TranslateTextResponse> {
        val translatedText = translateService.translate(translateTextRequest)
        return ResponseEntity.ok(TranslateTextResponse(translatedText))
    }
}
