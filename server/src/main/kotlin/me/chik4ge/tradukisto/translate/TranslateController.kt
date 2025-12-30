package me.chik4ge.tradukisto.translate

import jakarta.validation.Valid
import me.chik4ge.tradukisto.openapi.TranslateApi
import me.chik4ge.tradukisto.openapi.model.TranslateTextRequest
import me.chik4ge.tradukisto.openapi.model.TranslateTextResponse
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@RestController
class TranslateController(
    private val translateService: TranslateService,
) : TranslateApi {
    override fun translateText(
        translateTextRequest: Mono<TranslateTextRequest>,
        exchange: ServerWebExchange,
    ): Mono<TranslateTextResponse> {
        return translateTextRequest.flatMap { request ->
            Mono.fromCallable { translateService.translate(request) }
                .subscribeOn(Schedulers.boundedElastic())
                .map { translatedText -> TranslateTextResponse(translatedText) }
        }
    }

    override fun translateTextStream(
        translateTextRequest: Mono<TranslateTextRequest>,
        exchange: ServerWebExchange,
    ): Flux<String> {
        return translateTextRequest.flatMapMany { request ->
            translateService.translateStream(request)
        }
    }
}
