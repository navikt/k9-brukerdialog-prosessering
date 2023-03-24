package no.nav.k9brukerdialogprosessering.journalforing

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.k9brukerdialogprosessering.common.Ytelse
import no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.domene.Søknadstype
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Navn
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.time.ZonedDateTime

@Service
class K9JoarkService(
    private val k9JoarkRestTemplate: RestTemplate,
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(K9JoarkService::class.java)
    }

    suspend fun journalfør(
        journalføringsRequest: JournalføringsRequest,
    ): JournalføringsResponse = kotlin.runCatching {
        val resolveJournalføringsUrl =
            resolveJournalføringsUrl(journalføringsRequest.ytelse, journalføringsRequest.søknadstype)

        k9JoarkRestTemplate.exchange(
            resolveJournalføringsUrl.toString(),
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

    private fun resolveJournalføringsUrl(ytelse: Ytelse, søknadstype: Søknadstype?): URI = when (ytelse) {
        Ytelse.PLEIEPENGER_SYKT_BARN -> UriComponentsBuilder
            .fromPath("/v1/pleiepenge/journalforing")
            .build()
            .toUri()

        Ytelse.PLEIEPENGER_SYKT_BARN_ENDRINGSMELDING -> UriComponentsBuilder
            .fromPath("/v1/pleiepenge/endringsmelding/journalforing")
            .build()
            .toUri()

        Ytelse.ETTERSENDELSE -> {
            requireNotNull(søknadstype) { "Søknadstype må være satt for ettersendelse" }
            søknadstype.toUri()
        }

        Ytelse.OMSORGSPENGER_UTVIDET_RETT -> TODO()
        Ytelse.OMSORGSPENGER_MIDLERTIDIG_ALENE -> TODO()
        Ytelse.OMSORGSDAGER_ALENEOMSORG -> TODO()
        Ytelse.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER -> TODO()
        Ytelse.OMSORGSPENGER_UTBETALING_SNF -> TODO()
        Ytelse.PLEIEPENGER_LIVETS_SLUTTFASE -> TODO()
    }
}

fun Søknadstype.toUri(): URI {
    return when (this) {
        Søknadstype.PLEIEPENGER_SYKT_BARN -> UriComponentsBuilder
            .fromPath("/v1/pleiepenge/ettersending/journalforing")
            .build()
            .toUri()

        Søknadstype.PLEIEPENGER_LIVETS_SLUTTFASE -> UriComponentsBuilder
            .fromPath("/v1/pleiepenge/livets-sluttfase/ettersending/journalforing")
            .build()
            .toUri()

        Søknadstype.OMP_UTV_KS -> UriComponentsBuilder
            .fromPath("/v1/omsorgspenge/ettersending/journalforing")
            .build()
            .toUri()

        Søknadstype.OMP_UT_SNF -> UriComponentsBuilder
            .fromPath("/v1/omsorgspengeutbetaling/ettersending/journalforing")
            .queryParam("arbeidstype", listOf("frilanser", "selvstendig-naeringsdrivende"))
            .build()
            .toUri()

        Søknadstype.OMP_UT_ARBEIDSTAKER -> UriComponentsBuilder
            .fromPath("/v1/omsorgspengeutbetaling/ettersending/journalforing")
            .queryParam("arbeidstype", listOf("arbeidstaker"))
            .build()
            .toUri()


        Søknadstype.OMP_UTV_MA -> UriComponentsBuilder
            .fromPath("/v1/omsorgspenger/midlertidig-alene/ettersending/journalforing")
            .build()
            .toUri()

        Søknadstype.OMP_DELE_DAGER -> UriComponentsBuilder
            .fromPath("/v1/omsorgsdagerdeling/ettersending/journalforing")
            .build()
            .toUri()
    }
}

data class JournalføringsRequest(
    @JsonIgnore val ytelse: Ytelse,
    @JsonIgnore val søknadstype: Søknadstype? = null,
    @JsonProperty("norsk_ident") val norskIdent: String,
    @JsonProperty("soker_navn") val sokerNavn: Navn,
    @JsonProperty("mottatt") val mottatt: ZonedDateTime,
    @JsonProperty("dokument_id") val dokumentId: List<List<String>>
)

data class JournalføringsResponse(
    @JsonProperty("journal_post_id") val journalPostId: String,
)
