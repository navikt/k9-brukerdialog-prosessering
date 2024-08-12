package no.nav.brukerdialog.config

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.context.annotation.Configuration

@EnableJwtTokenValidation(ignore = ["org.springframework", "org.springdoc"])
@Configuration
internal class SecurityConfiguration

object Issuers {
    const val TOKEN_X = "tokenx"
}