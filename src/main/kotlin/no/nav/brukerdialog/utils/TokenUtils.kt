package no.nav.brukerdialog.utils

import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder

object TokenUtils {
    // Brukerident ligger i pid claim på tokenet for flyten idporten -> tokenx
    const val CLAIM_PID = "pid"

    // Brukerident ligger i sub claim på tokenet for flyten NAV loginservice -> tokenx
    const val CLAIM_SUB = "sub"

    fun SpringTokenValidationContextHolder.personIdent(): String {
        val jwtToken = getTokenValidationContext().firstValidToken
            ?: throw IllegalStateException("Ingen gyldige tokens i Authorization headeren")

        return jwtToken.personIdent()
    }

    fun JwtToken.personIdent(): String {
        val pid = jwtTokenClaims.getStringClaim(CLAIM_PID)
        val sub = jwtTokenClaims.getStringClaim(CLAIM_SUB)

        return when {
            !pid.isNullOrBlank() -> pid
            !sub.isNullOrBlank() -> sub
            else -> throw IllegalStateException("Ugyldig token. Token inneholdt verken sub eller pid claim")
        }
    }
}


