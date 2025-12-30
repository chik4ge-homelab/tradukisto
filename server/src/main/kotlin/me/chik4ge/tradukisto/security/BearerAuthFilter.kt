package me.chik4ge.tradukisto.security

import org.springframework.http.HttpStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class BearerAuthFilter(
    @Value("\${app.auth.bearer-token:}") private val bearerToken: String,
) : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val path = exchange.request.uri.path
        if (path.startsWith("/api/")) {
            val expected = bearerToken
            val header = exchange.request.headers.getFirst("Authorization").orEmpty()
            val prefix = "Bearer "
            val provided = if (header.startsWith(prefix)) header.substring(prefix.length) else ""
            val allowed = expected.isNotBlank() && provided == expected
            if (!allowed) {
                exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                return exchange.response.setComplete()
            }
        }

        return chain.filter(exchange)
    }
}
