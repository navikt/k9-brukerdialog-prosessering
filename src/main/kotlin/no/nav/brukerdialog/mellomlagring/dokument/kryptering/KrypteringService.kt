package no.nav.brukerdialog.mellomlagring.dokument.kryptering

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import no.nav.brukerdialog.mellomlagring.dokument.DokumentEier
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class KrypteringService(
    @Value("\${no.nav.mellomlagring.kryptering.passord.krypteringsnøkkel}") private val krypteringsnøkkel: String
) {
    private companion object {
        private val logger = LoggerFactory.getLogger(KrypteringService::class.java)
    }

    fun krypter(id: String, plainText: String, dokumentEier: DokumentEier): String {
        logger.trace("Krypterer ID $id")
        val keyId = hentNøkkelId(id)
        logger.trace("Krypterer med Nøkkel ID $keyId")

        return KrypteringUtil(
            passphrase = krypteringsnøkkel,
            iv = dokumentEier.eiersFødselsnummer
        ).krypter(plainText)
    }

    fun dekrypter(id: String, encrypted: String, dokumentEier: DokumentEier): String {
        logger.trace("Dekrypterer ID $id")
        val keyId = hentNøkkelId(id)
        logger.trace("Dekrypterer med Nøkkel ID $keyId")

        return KrypteringUtil(passphrase = krypteringsnøkkel, iv = dokumentEier.eiersFødselsnummer).dekrypter(encrypted)
    }

    fun id(id: String = UUID.randomUUID().toString()): String {
        val jwt = JWT.create()
            .withKeyId(krypteringsnøkkel)
            .withJWTId(id)
            .sign(Algorithm.none())
            .removeSuffix(".")
        logger.trace("Genrerert ID er $jwt")
        if (logger.isTraceEnabled) {
            val decoded = decodeId(jwt)
            val headers = String(Base64.getDecoder().decode(decoded.header))
            val payload = String(Base64.getDecoder().decode(decoded.payload))
            logger.trace("Headers=$headers")
            logger.trace("Payload=$payload")
        }
        return jwt
    }

    private fun decodeId(id: String) = JWT.decode(if (id.endsWith(".")) id else "$id.")

    private fun hentNøkkelId(id: String) = decodeId(id).keyId.toInt()
}
