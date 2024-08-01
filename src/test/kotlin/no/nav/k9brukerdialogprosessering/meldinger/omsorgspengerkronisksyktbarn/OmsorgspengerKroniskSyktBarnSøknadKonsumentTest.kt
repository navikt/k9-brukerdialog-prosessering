package no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn

import io.mockk.coEvery
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import no.nav.k9brukerdialogprosessering.AbstractIntegrationTest
import no.nav.k9brukerdialogprosessering.api.ytelse.omsorgspengerutvidetrett.SøknadUtils.defaultSøknad
import no.nav.k9brukerdialogprosessering.common.MetaInfo
import no.nav.k9brukerdialogprosessering.config.JacksonConfiguration.Companion.zonedDateTimeFormatter
import no.nav.k9brukerdialogprosessering.dittnavvarsel.DittnavVarselTopologyConfiguration.Companion.K9_DITTNAV_VARSEL_TOPIC
import no.nav.k9brukerdialogprosessering.dittnavvarsel.K9Beskjed
import no.nav.k9brukerdialogprosessering.kafka.types.TopicEntry
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn.OMPKSTopologyConfiguration.Companion.OMP_UTV_KS_SØKNAD_CLEANUP_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn.OMPKSTopologyConfiguration.Companion.OMP_UTV_KS_SØKNAD_MOTTATT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn.OMPKSTopologyConfiguration.Companion.OMP_UTV_KS_SØKNAD_PREPROSESSERT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn.utils.SøknadUtils
import no.nav.k9brukerdialogprosessering.utils.KafkaUtils.leggPåTopic
import no.nav.k9brukerdialogprosessering.utils.KafkaUtils.lesMelding
import no.nav.k9brukerdialogprosessering.utils.MockMvcUtils.sendInnSøknad
import no.nav.k9brukerdialogprosessering.utils.SøknadUtils.Companion.metadata
import no.nav.k9brukerdialogprosessering.utils.TokenTestUtils.hentToken
import org.intellij.lang.annotations.Language
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.net.URI
import java.time.ZonedDateTime
import java.util.*

class OmsorgspengerKroniskSyktBarnSøknadKonsumentTest : AbstractIntegrationTest() {
    override val consumerGroupPrefix = "omsorgspenger-kronisk-sykt-barn"
    override val consumerGroupTopics = listOf(
        OMP_UTV_KS_SØKNAD_MOTTATT_TOPIC, OMP_UTV_KS_SØKNAD_PREPROSESSERT_TOPIC, OMP_UTV_KS_SØKNAD_CLEANUP_TOPIC
    )

    @Test
    fun `forvent at melding konsumeres riktig og dokumenter blir slettet`() {
        val søker = mockSøker()
        mockBarn()
        mockLagreDokument()
        mockJournalføring()

        val søknadId = UUID.randomUUID().toString()
        mockMvc.sendInnSøknad(defaultSøknad.copy(søknadId = søknadId), mockOAuth2Server.hentToken())
        verify(exactly = 1, timeout = 120 * 1000) {
            runBlocking {
                k9DokumentMellomlagringService.slettDokumenter(any(), any())
            }
        }

        k9DittnavVarselConsumer.lesMelding(
            key = søknadId,
            topic = K9_DITTNAV_VARSEL_TOPIC
        ).value().assertDittnavVarsel(
            K9Beskjed(
                metadata = metadata,
                grupperingsId = søknadId,
                tekst = "Vi har mottatt søknad fra deg om ekstra omsorgsdager ved kronisk sykt eller funksjonshemmet barn.",
                link = null,
                dagerSynlig = 7,
                søkerFødselsnummer = søker.fødselsnummer,
                eventId = "testes ikke",
                ytelse = "OMSORGSPENGER_UTV_KS",
            )
        )
    }

    @Test
    fun `Forvent at melding bli prosessert på 5 forsøk etter 4 feil`() {
        val søknadId = UUID.randomUUID().toString()
        val mottattString = "2020-01-01T10:30:15Z"
        val mottatt = ZonedDateTime.parse(mottattString, zonedDateTimeFormatter)
        val søknadMottatt = SøknadUtils.defaultSøknad(søknadId = søknadId, mottatt = mottatt)
        val correlationId = UUID.randomUUID().toString()
        val metadata = MetaInfo(version = 1, correlationId = correlationId)
        val topicEntry = TopicEntry(metadata, søknadMottatt)
        val topicEntryJson = objectMapper.writeValueAsString(topicEntry)

        coEvery { k9DokumentMellomlagringService.lagreDokument(any()) }
            .throws(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenMany(listOf("123456789", "987654321").map { URI("http://localhost:8080/dokument/$it") })

        producer.leggPåTopic(key = søknadId, value = topicEntryJson, topic = OMP_UTV_KS_SØKNAD_MOTTATT_TOPIC)
        val lesMelding =
            consumer.lesMelding(key = søknadId, topic = OMP_UTV_KS_SØKNAD_PREPROSESSERT_TOPIC)
                .value()

        val preprosessertSøknadJson = JSONObject(lesMelding).getJSONObject("data").toString()
        JSONAssert.assertEquals(preprosessertSøknadSomJson(søknadId, mottattString), preprosessertSøknadJson, true)
    }

    @Language("JSON")
    private fun preprosessertSøknadSomJson(søknadId: String, mottatt: String) = """
         {
          "soknadId": "$søknadId",
          "mottatt": "$mottatt",
          "språk": "nb",
          "barn": {
            "aktørId": "123456",
            "fødselsdato": "2020-01-01",
            "navn": "Ole Dole Doffen",
            "norskIdentifikator": "02119970078"
          },
          "kroniskEllerFunksjonshemming": false,
          "søker": {
            "etternavn": "Kjeller",
            "mellomnavn": null,
            "aktørId": "12345",
            "fødselsdato": "2000-01-01",
            "fornavn": "Kjell",
            "fødselsnummer": "26104500284"
          },
          "harForståttRettigheterOgPlikter": true,
          "dokumentId": [
            [
              "123456789",
              "987654321"
            ],
            [
              "5678"
            ],
            [
              "1234"
            ]
          ],
          "relasjonTilBarnet": "FAR",
          "sammeAdresse": "JA",
          "høyereRisikoForFravær": true,
           "høyereRisikoForFraværBeskrivelse": "Beskrivelse av høyere risiko for fravær",
          "harBekreftetOpplysninger": true,
          "k9FormatSøknad": {
            "språk": "nb",
            "kildesystem": null,
            "mottattDato": "$mottatt",
            "søknadId": "$søknadId",
            "søker": {
              "norskIdentitetsnummer": "26104500284"
            },
            "ytelse": {
              "barn": {
                "fødselsdato": null,
                "norskIdentitetsnummer": "02119970078"
              },
              "kroniskEllerFunksjonshemming": true,
              "type": "OMP_UTV_KS",
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
}

private fun String.assertDittnavVarsel(k9Beskjed: K9Beskjed) {
    val k9BeskjedJson = JSONObject(this)
    assertEquals(k9Beskjed.grupperingsId, k9BeskjedJson.getString("grupperingsId"))
    assertEquals(k9Beskjed.tekst, k9BeskjedJson.getString("tekst"))
    assertEquals(k9Beskjed.ytelse, k9BeskjedJson.getString("ytelse"))
    assertEquals(k9Beskjed.dagerSynlig, k9BeskjedJson.getLong("dagerSynlig"))
}
