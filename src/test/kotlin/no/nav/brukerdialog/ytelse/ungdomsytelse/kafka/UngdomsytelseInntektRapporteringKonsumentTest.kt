package no.nav.brukerdialog.ytelse.ungdomsytelse.kafka

import io.mockk.coEvery
import io.mockk.coVerify
import no.nav.brukerdialog.AbstractIntegrationTest
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.config.JacksonConfiguration
import no.nav.brukerdialog.dittnavvarsel.DittnavVarselTopologyConfiguration
import no.nav.brukerdialog.dittnavvarsel.K9Beskjed
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.utils.KafkaUtils.leggPåTopic
import no.nav.brukerdialog.utils.KafkaUtils.lesMelding
import no.nav.brukerdialog.utils.MockMvcUtils.sendInnSøknad
import no.nav.brukerdialog.utils.TokenTestUtils.hentToken
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.inntektsrapportering.UngdomsytelseInntektsrapporteringTopologyConfiguration
import no.nav.brukerdialog.ytelse.ungdomsytelse.utils.InntektrapporteringUtils
import org.intellij.lang.annotations.Language
import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.net.URI
import java.time.ZonedDateTime
import java.util.*

class UngdomsytelseInntektRapporteringKonsumentTest : AbstractIntegrationTest() {

    override val consumerGroupPrefix = "ungdomsytelse-inntektsrapportering"
    override val consumerGroupTopics = listOf(
        UngdomsytelseInntektsrapporteringTopologyConfiguration.UNGDOMSYTELSE_INNTEKTSRAPPORTERING_MOTTATT_TOPIC,
        UngdomsytelseInntektsrapporteringTopologyConfiguration.UNGDOMSYTELSE_INNTEKTSRAPPORTERING_PREPROSESSERT_TOPIC,
        UngdomsytelseInntektsrapporteringTopologyConfiguration.UNGDOMSYTELSE_INNTEKTSRAPPORTERING_CLEANUP_TOPIC
    )

    @Test
    fun `forvent at melding konsumeres riktig og dokumenter blir slettet`() {
        val søker = mockSøker()
        mockBarn()
        mockLagreDokument()
        mockJournalføring()

        val søknadId = UUID.randomUUID().toString()
        val inntektsrapportering = InntektrapporteringUtils.defaultInntektsrapportering.copy(søknadId = søknadId)

        mockMvc.sendInnSøknad(inntektsrapportering, mockOAuth2Server.hentToken())

        coVerify(exactly = 1, timeout = 120 * 1000) {
            k9DokumentMellomlagringService.slettDokumenter(any(), any())
        }

        k9DittnavVarselConsumer.lesMelding(
            key = søknadId,
            topic = DittnavVarselTopologyConfiguration.K9_DITTNAV_VARSEL_TOPIC
        ).value().assertDittnavVarsel(
            K9Beskjed(
                metadata = no.nav.brukerdialog.utils.SøknadUtils.metadata,
                grupperingsId = søknadId,
                tekst = "Rapportert inntenkt for ungdomsytelsen er mottatt",
                link = null,
                dagerSynlig = 7,
                søkerFødselsnummer = søker.fødselsnummer,
                eventId = "testes ikke",
                ytelse = "UNGDOMSYTELSE",
            )
        )
    }

    @Test
    fun `Forvent at melding bli prosessert på 5 forsøk etter 4 feil`() {
        val søknadId = UUID.randomUUID().toString()
        val mottattString = "2020-01-01T10:30:15Z"
        val mottatt = ZonedDateTime.parse(mottattString, JacksonConfiguration.zonedDateTimeFormatter)
        val inntektsrapportering =
            InntektrapporteringUtils.gyldigInntektsrapportering(søknadId = søknadId, mottatt = mottatt)
        val correlationId = UUID.randomUUID().toString()
        val metadata = MetaInfo(version = 1, correlationId = correlationId)
        val topicEntry = TopicEntry(metadata, inntektsrapportering)
        val topicEntryJson = objectMapper.writeValueAsString(topicEntry)

        coEvery { k9DokumentMellomlagringService.lagreDokument(any()) }
            .throws(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenMany(listOf("123456789", "987654321").map { URI("http://localhost:8080/dokument/$it") })

        producer.leggPåTopic(
            key = søknadId,
            value = topicEntryJson,
            topic = UngdomsytelseInntektsrapporteringTopologyConfiguration.UNGDOMSYTELSE_INNTEKTSRAPPORTERING_MOTTATT_TOPIC
        )
        val lesMelding =
            consumer.lesMelding(
                key = søknadId,
                topic = UngdomsytelseInntektsrapporteringTopologyConfiguration.UNGDOMSYTELSE_INNTEKTSRAPPORTERING_PREPROSESSERT_TOPIC,
                maxWaitInSeconds = 120
            ).value()

        val preprosessertSøknadJson = JSONObject(lesMelding).getJSONObject("data").toString()
        JSONAssert.assertEquals(preprosessertSøknadSomJson(søknadId, mottattString), preprosessertSøknadJson, true)
    }

    @Language("JSON")
    private fun preprosessertSøknadSomJson(søknadId: String, mottatt: String) = """
        {
          "søknadId": "$søknadId",
          "mottatt": "$mottatt",
          "søker": {
            "etternavn": "Nordmann",
            "mellomnavn": "Mellomnavn",
            "aktørId": "123456",
            "fødselsdato": "2000-01-01",
            "fornavn": "Ola",
            "fødselsnummer": "02119970078"
          },
          "oppgittInntektForPeriode": {
                "arbeidstakerOgFrilansInntekt": 6000,
                "næringsinntekt": 4000,
                "inntektFraYtelse": 2000,
                "periodeForInntekt": {
                    "fraOgMed": "2025-01-01",
                    "tilOgMed": "2025-01-31"
                }
            },
          "harBekreftetInntekt": true,
          "dokumentId": [
            [
              "123456789",
              "987654321"
            ]
          ],
          "k9Format": {
            "språk": "nb",
            "kildesystem": "søknadsdialog",
            "mottattDato": "$mottatt",
            "søknadId": "$søknadId",
            "søker": {
              "norskIdentitetsnummer": "02119970078"
            },
            "ytelse": {
              "type": "UNGDOMSYTELSE",
              "søknadType": "RAPPORTERING_SØKNAD",
              "søktFraDatoer": [],
              "inntekter": {
                "oppgittePeriodeinntekter": [
                  {
                    "arbeidstakerOgFrilansInntekt": "6000",
                    "næringsinntekt": "4000",
                    "ytelse": "2000",
                    "periode": "2025-01-01/2025-01-31"
                  }
                ]
              } 
            },
            "journalposter": [],
            "begrunnelseForInnsending": {
              "tekst": null
            },
            "versjon": "1.0.0"
          }
        }
        """.trimIndent()


    private fun String.assertDittnavVarsel(k9Beskjed: K9Beskjed) {
        val k9BeskjedJson = JSONObject(this)
        Assertions.assertEquals(k9Beskjed.grupperingsId, k9BeskjedJson.getString("grupperingsId"))
        Assertions.assertEquals(k9Beskjed.tekst, k9BeskjedJson.getString("tekst"))
        Assertions.assertEquals(k9Beskjed.ytelse, k9BeskjedJson.getString("ytelse"))
        Assertions.assertEquals(k9Beskjed.dagerSynlig, k9BeskjedJson.getLong("dagerSynlig"))
    }
}
