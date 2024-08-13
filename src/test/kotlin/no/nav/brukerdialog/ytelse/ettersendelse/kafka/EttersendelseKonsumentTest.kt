package no.nav.brukerdialog.ytelse.ettersendelse.kafka

import io.mockk.coEvery
import io.mockk.coVerify
import no.nav.brukerdialog.AbstractIntegrationTest
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.config.JacksonConfiguration.Companion.zonedDateTimeFormatter
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.ytelse.ettersendelse.kafka.EttersendelseTopologyConfiguration.Companion.ETTERSENDELSE_CLEANUP_TOPIC
import no.nav.brukerdialog.ytelse.ettersendelse.kafka.EttersendelseTopologyConfiguration.Companion.ETTERSENDELSE_MOTTATT_TOPIC
import no.nav.brukerdialog.ytelse.ettersendelse.kafka.EttersendelseTopologyConfiguration.Companion.ETTERSENDELSE_PREPROSESSERT_TOPIC
import no.nav.brukerdialog.ytelse.ettersendelse.utils.EttersendingUtils
import no.nav.brukerdialog.utils.KafkaUtils.leggPåTopic
import no.nav.brukerdialog.utils.KafkaUtils.lesMelding
import no.nav.brukerdialog.utils.MockMvcUtils.sendInnSøknad
import no.nav.brukerdialog.utils.TokenTestUtils.hentToken
import no.nav.brukerdialog.ytelse.ettersendelse.utils.EttersendingUtils.defaultEttersendelse
import org.intellij.lang.annotations.Language
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.net.URI
import java.time.ZonedDateTime
import java.util.*

class EttersendelseKonsumentTest : AbstractIntegrationTest() {
    override val consumerGroupPrefix = "ettersendelse"
    override val consumerGroupTopics = listOf(
        ETTERSENDELSE_MOTTATT_TOPIC, ETTERSENDELSE_PREPROSESSERT_TOPIC, ETTERSENDELSE_CLEANUP_TOPIC
    )

    @Test
    fun `forvent at melding konsumeres riktig og dokumenter blir slettet`() {
        mockSøker()
        mockBarn()
        mockHentDokumenter()
        mockLagreDokument()
        mockJournalføring()

        mockMvc.sendInnSøknad(defaultEttersendelse, mockOAuth2Server.hentToken())

        coVerify(exactly = 1, timeout = 120 * 1000) {
            k9DokumentMellomlagringService.slettDokumenter(any(), any())
        }
    }

    @Test
    fun `Forvent at melding bli prosessert på 5 forsøk etter 4 feil`() {
        val søknadId = UUID.randomUUID().toString()
        val mottattString = "2020-01-01T10:30:15Z"
        val mottatt = ZonedDateTime.parse(mottattString, zonedDateTimeFormatter)
        val ettersendelseMottatt = EttersendingUtils.defaultEttersendelse(søknadId = søknadId, mottatt = mottatt)
        val correlationId = UUID.randomUUID().toString()
        val metadata = MetaInfo(version = 1, correlationId = correlationId)
        val topicEntry = TopicEntry(metadata, ettersendelseMottatt)
        val topicEntryJson = objectMapper.writeValueAsString(topicEntry)

        coEvery { k9DokumentMellomlagringService.lagreDokument(any()) }
            .throws(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenMany(listOf("123456789", "987654321").map { URI("http://localhost:8080/dokument/$it") })

        producer.leggPåTopic(key = søknadId, value = topicEntryJson, topic = ETTERSENDELSE_MOTTATT_TOPIC)
        val lesMelding =
            consumer.lesMelding(key = søknadId, topic = ETTERSENDELSE_PREPROSESSERT_TOPIC)
                .value()

        val preprosessertSøknadJson = JSONObject(lesMelding).getJSONObject("data").toString()
        JSONAssert.assertEquals(
            preprosessertEttersendelseSomJson(søknadId, mottattString),
            preprosessertSøknadJson,
            true
        )
    }

    @Language("JSON")
    private fun preprosessertEttersendelseSomJson(søknadId: String, mottatt: String) = """
        {
          "harForstattRettigheterOgPlikter": true,
          "k9Format": {
            "mottattDato": "$mottatt",
            "søknadId": "$søknadId",
            "søker": {
              "norskIdentitetsnummer": "29099012345"
            },
            "ytelse": "PLEIEPENGER_SYKT_BARN",
            "versjon": "0.0.1",
            "type": "LEGEERKLÆRING",
            "pleietrengende": {
              "norskIdentitetsnummer": "29099012345"
            }
          },
          "soknadId": "$søknadId",
          "mottatt": "$mottatt",
          "søker": {
            "etternavn": "Ånsnes",
            "mellomnavn": "Øverbø",
            "aktørId": "123456",
            "fødselsdato": "2003-03-21",
            "fornavn": "Ærling",
            "fødselsnummer": "29099012345"
          },
         
          "vedleggId": [
            [
              "123456789",
              "987654321"
            ],
            [
              "vedlegg1"
            ],
            [
              "vedlegg2"
            ],
            [
              "vedlegg3"
            ]
          ],
          "sprak": "nb",
          "titler": [
            "Vedlegg 1",
            "Vedlegg 2",
            "Vedlegg 3"
          ],
          "søknadstype": "PLEIEPENGER_SYKT_BARN",
          "ettersendelsesType": "LEGEERKLÆRING",
          "pleietrengende": {
            "fødselsdato": "2003-03-21",
            "navn": "Ola Nordmann",
            "norskIdentitetsnummer": "29099012345"
          },
          "harBekreftetOpplysninger": true,
          "beskrivelse": "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed accumsan erat cursus enim aliquet, ac auctor orci consequat. Etiam nec tellus sapien. Nam gravida massa id sagittis ultrices."
        }
        """.trimIndent()
}
