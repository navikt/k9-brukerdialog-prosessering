package no.nav.k9brukerdialogprosessering.config

import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder

@EnableOAuth2Client(cacheEnabled = true)
@Configuration
class TokenClientConfig {

    @Bean
    fun tokenValidationContextHolder() = SpringTokenValidationContextHolder()
}

class SpringTokenValidationContextHolder : TokenValidationContextHolder {

    private val TOKEN_VALIDATION_CONTEXT_ATTRIBUTE = SpringTokenValidationContextHolder::class.java.name
    override fun getTokenValidationContext() = getRequestAttribute(TOKEN_VALIDATION_CONTEXT_ATTRIBUTE)?.let { it as TokenValidationContext } ?: TokenValidationContext(emptyMap())
    override fun setTokenValidationContext(ctx: TokenValidationContext?) = setRequestAttribute(TOKEN_VALIDATION_CONTEXT_ATTRIBUTE, ctx)
    private fun getRequestAttribute(name: String) = RequestContextHolder.currentRequestAttributes()
        .getAttribute(name, RequestAttributes.SCOPE_REQUEST)
    private fun setRequestAttribute(name: String, value: Any?) = value?.let { RequestContextHolder.currentRequestAttributes()
        .setAttribute(name, it, RequestAttributes.SCOPE_REQUEST) } ?:  RequestContextHolder.currentRequestAttributes()
        .removeAttribute(name, RequestAttributes.SCOPE_REQUEST)
}
