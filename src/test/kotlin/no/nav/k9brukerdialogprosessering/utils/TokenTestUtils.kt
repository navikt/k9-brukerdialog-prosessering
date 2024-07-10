package no.nav.k9brukerdialogprosessering.utils

import com.nimbusds.jwt.SignedJWT
import io.mockk.every
import no.nav.k9brukerdialogprosessering.config.Issuers
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders

object TokenTestUtils {
    /*
       Header:
       {
         "alg": "HS256",
         "typ": "JWT"
       }

       Payload:
       {
         "sub": "1234567890",
         "name": "John Doe",
         "iat": 1516239022
       }
        */
    const val MOCK_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"

    fun MockOAuth2Server.hentToken(
        subject: String = "12345678910",
        issuerId: String = Issuers.TOKEN_X,
        claims: Map<String, String> = mapOf("acr" to "Level4"),
        audience: String = "aud-localhost",
        expiry: Long = 3600
    ): SignedJWT =
        issueToken(issuerId = issuerId, subject = subject, claims = claims, audience = audience, expiry = expiry)

    fun SignedJWT.tokenTilHttpEntity(body: String? = null): HttpEntity<Any> {
        val headers = this.tokenTilHeader()
        return HttpEntity<Any>(body, headers)
    }

    fun SignedJWT.tokenTilHeader(): HttpHeaders {
        val token = serialize()
        val headers = HttpHeaders()
        headers.setBearerAuth(token)
        return headers
    }

    fun SpringTokenValidationContextHolder.mockContext(encodedToken: String = MOCK_TOKEN) {
        every { getTokenValidationContext() } returns TokenValidationContext(mapOf("1234567890" to JwtToken(encodedToken)))
    }
}
