package no.nav.brukerdialog.integrasjon.k9selvbetjeningoppslag

import no.nav.brukerdialog.oppslag.arbeidsgiver.ArbeidsgivereOppslagResponsDto
import no.nav.brukerdialog.ytelse.Ytelse
import no.nav.brukerdialog.oppslag.TilgangNektetException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
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
        inkluderAlleAnsettelsesperioder: Boolean,
        skalHentePrivateArbeidsgivere: Boolean,
        skalHenteFrilansoppdrag: Boolean
    ): ArbeidsgivereOppslagResponsDto {
        val ytelse = Ytelse.fraMDC()
        val url = UriComponentsBuilder.fromUri(arbeidsgivereUrl.toUri())
            .queryParam("fom", DateTimeFormatter.ISO_LOCAL_DATE.format(fraOgMed))
            .queryParam("tom", DateTimeFormatter.ISO_LOCAL_DATE.format(tilOgMed))
            .queryParam("inkluderAlleAnsettelsesperioder", inkluderAlleAnsettelsesperioder)
            .queryParam("a", genererAttributter(skalHentePrivateArbeidsgivere, skalHenteFrilansoppdrag))
            .build()
        logger.info("Slår opp arbeidsgivere for url: {}...", url.toUriString())

        val exchange = k9OppslagsKlient.exchange(
            url.toUriString(),
            HttpMethod.GET,
            HttpEntity(null, ytelse.somHttpHeader()),
            ArbeidsgivereOppslagResponsDto::class.java
        )
        logger.info("Fikk response {} for oppslag av arbeidsgivere.", exchange.statusCode)

        return exchange.body!!
    }

    @Recover
    private fun recoverArbeidsgivere(error: HttpServerErrorException): ArbeidsgivereOppslagResponsDto {
        logger.error("Error response = '${error.responseBodyAsString}' fra '${arbeidsgivereUrl.toUriString()}'")
        throw IllegalStateException("Feil ved henting av arbeidsgivere")
    }

    @Recover
    private fun recoverArbeidsgivere(error: HttpClientErrorException): ArbeidsgivereOppslagResponsDto {
        logger.error("Error response = '${error.responseBodyAsString}' fra '${arbeidsgivereUrl.toUriString()}'")
        if(error.statusCode == HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS) {
            throw TilgangNektetException("Tilgang nektet til arbeidsgivere.", HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS)
        }
        throw IllegalStateException("Feil ved henting av arbeidsgivere")
    }

    @Recover
    private fun recoverArbeidsgivere(error: ResourceAccessException): ArbeidsgivereOppslagResponsDto {
        logger.error("{}", error.message)
        throw IllegalStateException("Timeout ved henting av arbeidsgivere")
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
