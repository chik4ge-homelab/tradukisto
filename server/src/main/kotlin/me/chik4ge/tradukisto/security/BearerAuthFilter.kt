package me.chik4ge.tradukisto.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class BearerAuthFilter(
    @Value("\${app.auth.bearer-token:}") private val bearerToken: String,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (request.requestURI.startsWith("/api/")) {
            val expected = bearerToken
            val header = request.getHeader("Authorization").orEmpty()
            val prefix = "Bearer "
            val provided = if (header.startsWith(prefix)) header.substring(prefix.length) else ""
            val allowed = expected.isNotBlank() && provided == expected
            if (!allowed) {
                response.status = HttpServletResponse.SC_UNAUTHORIZED
                return
            }
        }

        filterChain.doFilter(request, response)
    }
}
