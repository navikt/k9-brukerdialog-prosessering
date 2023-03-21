package no.nav.k9brukerdialogprosessering.journalforing

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.k9brukerdialogprosessering.common.Ytelse
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Navn
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.time.ZonedDateTime

@Service
class K9JoarkService(
    private val k9JoarkRestTemplate: RestTemplate
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(K9JoarkService::class.java)
    }

    suspend fun journalfør(
        journalføringsRequest: JournalføringsRequest,
    ): JournalføringsResponse = kotlin.runCatching {
        val resolveJournalføringsUrl = resolveJournalføringsUrl(journalføringsRequest.ytelse)

            k9JoarkRestTemplate.exchange(
                resolveJournalføringsUrl.path,
                HttpMethod.POST,
                HttpEntity(journalføringsRequest),
                JournalføringsResponse::class.java
            )
    }.fold(
        onSuccess = { response: ResponseEntity<JournalføringsResponse> ->
            val journalføringsResponse = response.body!!
            logger.info("Dokumenter journalført på journalpost med id: ${journalføringsResponse.journalPostId}")
            journalføringsResponse
        },
        { error: Throwable ->
            if (error is RestClientException) {
                logger.error("Feil ved journalføring. Feilmelding: ${error.message}")
            }
            throw error
        }
    )

    private fun resolveJournalføringsUrl(ytelse: Ytelse) = when (ytelse) {
        Ytelse.PLEIEPENGER_SYKT_BARN, -> UriComponentsBuilder
            .fromPath("/v1/pleiepenge/journalforing")
            .build()
            .toUri()

        Ytelse.PLEIEPENGER_SYKT_BARN_ENDRINGSMELDING -> UriComponentsBuilder
            .fromPath("/v1/pleiepenge/endringsmelding/journalforing")
            .build()
            .toUri()

        Ytelse.OMSORGSPENGER_UTVIDET_RETT -> TODO()
        Ytelse.OMSORGSPENGER_MIDLERTIDIG_ALENE -> TODO()
        Ytelse.ETTERSENDING -> TODO()
        Ytelse.OMSORGSDAGER_ALENEOMSORG -> TODO()
        Ytelse.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER -> TODO()
        Ytelse.OMSORGSPENGER_UTBETALING_SNF -> TODO()
        Ytelse.PLEIEPENGER_LIVETS_SLUTTFASE -> TODO()
        Ytelse.ETTERSENDING_PLEIEPENGER_SYKT_BARN -> TODO()
        Ytelse.ETTERSENDING_PLEIEPENGER_LIVETS_SLUTTFASE -> TODO()
        Ytelse.ETTERSENDING_OMP -> TODO()
    }
}

data class JournalføringsRequest(
    @JsonIgnore val ytelse: Ytelse,
    @JsonProperty("norsk_ident") val norskIdent: String,
    @JsonProperty("soker_navn") val sokerNavn: Navn,
    @JsonProperty("mottatt") val mottatt: ZonedDateTime,
    @JsonProperty("dokument_id") val dokumentId: List<List<String>>
)

data class JournalføringsResponse(
    @JsonProperty("journal_post_id") val journalPostId: String,
)
