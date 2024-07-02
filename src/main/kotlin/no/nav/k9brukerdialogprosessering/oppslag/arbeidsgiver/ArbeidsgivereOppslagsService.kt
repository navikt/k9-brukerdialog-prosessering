package no.nav.k9brukerdialogprosessering.oppslag.arbeidsgiver

import no.nav.k9brukerdialogapi.oppslag.arbeidsgiver.ArbeidsgivereOppslagRespons
import no.nav.k9brukerdialogprosessering.api.ytelse.Ytelse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponents
import org.springframework.web.util.UriComponentsBuilder
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
@Retryable(
    exclude = [HttpClientErrorException.Unauthorized::class, HttpClientErrorException.Forbidden::class, ResourceAccessException::class],
    backoff = Backoff(
        delayExpression = "\${spring.rest.retry.initialDelay}",
        multiplierExpression = "\${spring.rest.retry.multiplier}",
        maxDelayExpression = "\${spring.rest.retry.maxDelay}"
    ),
    maxAttemptsExpression = "\${spring.rest.retry.maxAttempts}"
)
class ArbeidsgivereOppslagsService(
    @Qualifier("k9OppslagsKlient")
    private val k9OppslagsKlient: RestTemplate,
) {
    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(ArbeidsgivereOppslagsService::class.java)

        private val frilansoppdragAttributter = listOf("frilansoppdrag[]")
        private val arbeidsgivereAttributter = listOf(
            "arbeidsgivere[].organisasjoner[].organisasjonsnummer",
            "arbeidsgivere[].organisasjoner[].navn",
            "arbeidsgivere[].organisasjoner[].ansettelsesperiode"
        )
        private val privateArbeidsgivereAttributter = listOf(
            "private_arbeidsgivere[].ansettelsesperiode",
            "private_arbeidsgivere[].offentlig_ident"
        )

        val arbeidsgivereUrl: UriComponents = UriComponentsBuilder
            .fromUriString("/meg")
            .queryParam("a", arbeidsgivereAttributter)
            .build()
    }

    fun hentArbeidsgivere(
        fraOgMed: LocalDate,
        tilOgMed: LocalDate,
        skalHentePrivateArbeidsgivere: Boolean,
        skalHenteFrilansoppdrag: Boolean,
        ytelse: Ytelse,
    ): ArbeidsgivereOppslagRespons {
        val url = UriComponentsBuilder.fromUri(arbeidsgivereUrl.toUri())
            .queryParam("fom", DateTimeFormatter.ISO_LOCAL_DATE.format(fraOgMed))
            .queryParam("tom", DateTimeFormatter.ISO_LOCAL_DATE.format(tilOgMed))
            .queryParam("a", genererAttributter(skalHentePrivateArbeidsgivere, skalHenteFrilansoppdrag))
            .build()
        logger.info("Slår opp arbeidsgivere for url: {}...", url.toUriString())

        val exchange = k9OppslagsKlient.exchange(
            url.toUriString(),
            HttpMethod.GET,
            HttpEntity(null, ytelse.somHttpHeader()),
            ArbeidsgivereOppslagRespons::class.java
        )
        logger.info("Fikk response {} for oppslag av arbeidsgivere.", exchange.statusCode)

        return exchange.body!!
    }

    private fun genererAttributter(
        skalHentePrivateArbeidsgivere: Boolean,
        skalHenteFrilansoppdrag: Boolean,
    ) = mutableListOf<String>().apply {
        addAll(arbeidsgivereAttributter)
        if (skalHentePrivateArbeidsgivere) addAll(privateArbeidsgivereAttributter)
        if (skalHenteFrilansoppdrag) addAll(frilansoppdragAttributter)
    }
}
