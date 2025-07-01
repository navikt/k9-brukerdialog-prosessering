package no.nav.brukerdialog.ytelse.omsorgspengeraleneomsorg.kafka

import io.mockk.coEvery
import io.mockk.coVerify
import no.nav.brukerdialog.AbstractIntegrationTest
import no.nav.brukerdialog.ytelse.omsorgspengeraleneomsorg.utils.SøknadUtils
import no.nav.brukerdialog.ytelse.omsorgspengeraleneomsorg.api.domene.Barn
import no.nav.brukerdialog.ytelse.omsorgspengeraleneomsorg.api.domene.TidspunktForAleneomsorg
import no.nav.brukerdialog.ytelse.omsorgspengeraleneomsorg.api.domene.TypeBarn
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.config.JacksonConfiguration
import no.nav.brukerdialog.dittnavvarsel.DittnavVarselTopologyConfiguration
import no.nav.brukerdialog.dittnavvarsel.K9Beskjed
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.utils.KafkaUtils.leggPåTopic
import no.nav.brukerdialog.utils.KafkaUtils.lesMelding
import no.nav.brukerdialog.utils.MockMvcUtils.sendInnSøknad
import no.nav.brukerdialog.utils.TokenTestUtils.hentToken
import no.nav.brukerdialog.ytelse.omsorgspengeraleneomsorg.utils.OMPAleneomsorgSoknadUtils
import org.intellij.lang.annotations.Language
import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.net.URI
import java.time.ZonedDateTime
import java.util.*

class OMPAleneomsorgSoknadKonsumentTest : AbstractIntegrationTest() {

    override val consumerGroupPrefix = "omsorgspenger-aleneomsorg"
    override val consumerGroupTopics = listOf(
        OMPAleneomsorgTopologyConfiguration.OMP_AO_MOTTATT_TOPIC,
        OMPAleneomsorgTopologyConfiguration.OMP_AO_PREPROSESSERT_TOPIC,
        OMPAleneomsorgTopologyConfiguration.OMP_AO_CLEANUP_TOPIC
    )

    @Test
    fun `forvent at melding konsumeres riktig og dokumenter blir slettet`() {
        val søker = mockSøker()
        mockBarn()
        mockLagreDokument()
        mockJournalføring()

        val søknadId = UUID.randomUUID().toString()
        mockMvc.sendInnSøknad(
            SøknadUtils.defaultSøknad.copy(
                søknadId = søknadId,
                barn = listOf(
                    Barn(
                        navn = "Barn1",
                        type = TypeBarn.FRA_OPPSLAG,
                        aktørId = "123",
                        identitetsnummer = "25058118020",
                        tidspunktForAleneomsorg = TidspunktForAleneomsorg.TIDLIGERE
                    )
                )
            ), mockOAuth2Server.hentToken()
        )

        coVerify(exactly = 1, timeout = 120 * 1000) {
            dokumentService.slettDokumenter(any(), any())
        }

        k9DittnavVarselConsumer.lesMelding(
            key = søknadId,
            topic = DittnavVarselTopologyConfiguration.K9_DITTNAV_VARSEL_TOPIC
        ).value().assertDittnavVarsel(
            K9Beskjed(
                metadata = no.nav.brukerdialog.utils.SøknadUtils.metadata,
                grupperingsId = søknadId,
                tekst = "Vi har mottatt søknad fra deg om ekstra omsorgsdager ved aleneomsorg.",
                link = null,
                dagerSynlig = 7,
                søkerFødselsnummer = søker.fødselsnummer,
                eventId = "testes ikke",
                ytelse = "OMSORGSDAGER_ALENEOMSORG",
            )
        )
    }

    @Test
    fun `Forvent at melding bli prosessert på 5 forsøk etter 4 feil`() {
        val søknadId = UUID.randomUUID().toString()
        val mottattString = "2020-01-01T10:30:15Z"
        val mottatt = ZonedDateTime.parse(mottattString, JacksonConfiguration.zonedDateTimeFormatter)
        val søknadMottatt = OMPAleneomsorgSoknadUtils.defaultSøknad(
            søknadId = søknadId,
            mottatt = mottatt
        )
        val correlationId = UUID.randomUUID().toString()
        val metadata = MetaInfo(version = 1, correlationId = correlationId)
        val topicEntry = TopicEntry(metadata, søknadMottatt)
        val topicEntryJson = objectMapper.writeValueAsString(topicEntry)

        coEvery { dokumentService.lagreDokument(any(), any(), any(), any()) }
            .throws(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenMany(listOf("123456789", "987654321"))

        producer.leggPåTopic(
            key = søknadId,
            value = topicEntryJson,
            topic = OMPAleneomsorgTopologyConfiguration.OMP_AO_MOTTATT_TOPIC
        )
        val lesMelding =
            consumer.lesMelding(key = søknadId, topic = OMPAleneomsorgTopologyConfiguration.OMP_AO_PREPROSESSERT_TOPIC)
                .value()

        val preprosessertSøknadJson = JSONObject(lesMelding).getJSONObject("data").toString()
        JSONAssert.assertEquals(preprosessertSøknadSomJson(søknadId, mottattString), preprosessertSøknadJson, true)
    }

    @Language("JSON")
    private fun preprosessertSøknadSomJson(søknadId: String, mottatt: String) = """
       {
          "søknadId": "$søknadId",
          "mottatt": "$mottatt",
          "språk": "nb",
          "barn": {
            "identitetsnummer": "29076523302",
            "dato": "2020-08-07",
            "aktørId": "12345",
            "tidspunktForAleneomsorg": "SISTE_2_ÅRENE",
            "fødselsdato": null,
            "navn": "Ole Dole",
            "type": "FRA_OPPSLAG"
          },
          "søker": {
            "etternavn": "Nordmann",
            "mellomnavn": "Mellomnavn",
            "aktørId": "123456",
            "fødselsdato": "1993-01-04",
            "fornavn": "Ola",
            "fødselsnummer": "02119970078"
          },
          "harForståttRettigheterOgPlikter": true,
          "dokumentId": [
            [
              "123456789",
              "987654321"
            ]
          ],
          "harBekreftetOpplysninger": true,
          "k9Søknad": {
            "språk": "nb",
            "kildesystem": null,
            "mottattDato": "$mottatt",
            "søknadId": "$søknadId",
            "søker": {
              "norskIdentitetsnummer": "02119970078"
            },
            "ytelse": {
              "barn": {
                "fødselsdato": null,
                "norskIdentitetsnummer": "29076523302"
              },
              "type": "OMP_UTV_AO",
              "periode": "2020-01-01\/..",
              "dataBruktTilUtledning": null
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
