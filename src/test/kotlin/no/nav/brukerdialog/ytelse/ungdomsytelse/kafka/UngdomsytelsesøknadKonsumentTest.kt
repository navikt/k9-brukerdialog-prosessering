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
import no.nav.brukerdialog.utils.NavHeaders
import no.nav.brukerdialog.utils.TokenTestUtils.hentToken
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.soknad.UngdomsytelsesøknadTopologyConfiguration
import no.nav.brukerdialog.ytelse.ungdomsytelse.utils.SøknadUtils
import no.nav.brukerdialog.ytelse.ungdomsytelse.utils.UngdomsytelsesøknadUtils
import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.felles.Oppgavetype
import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.felles.SøkYtelseOppgavetypeDataDTO
import org.intellij.lang.annotations.Language
import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import java.net.URI
import java.time.ZonedDateTime
import java.util.*

class UngdomsytelsesøknadKonsumentTest : AbstractIntegrationTest() {

    override val consumerGroupPrefix = "ungdomsytelse-soknad"
    override val consumerGroupTopics = listOf(
        UngdomsytelsesøknadTopologyConfiguration.UNGDOMSYTELSE_SØKNAD_MOTTATT_TOPIC,
        UngdomsytelsesøknadTopologyConfiguration.UNGDOMSYTELSE_SØKNAD_PREPROSESSERT_TOPIC,
        UngdomsytelsesøknadTopologyConfiguration.UNGDOMSYTELSE_SØKNAD_CLEANUP_TOPIC
    )

    @Test
    fun `forvent at melding konsumeres riktig og dokumenter blir slettet`() {
        val søker = mockSøker()
        mockBarn()
        mockLagreDokument()
        mockJournalføring()

        val oppgaveReferanse = UUID.randomUUID().toString()
        val søknad = SøknadUtils.defaultSøknad.copy(oppgaveReferanse = oppgaveReferanse)

        mockHentingAvOppgave(
            oppgavetype = Oppgavetype.SØK_YTELSE,
            oppgavetypeData = SøkYtelseOppgavetypeDataDTO(
                fomDato = søknad.startdato,
            )
        )
        mockMarkerDeltakeleSomSøkt()
        mockMarkerOppgaveSomLøst()

        val token = mockOAuth2Server.hentToken()

        mockMvc.post("/ungdomsytelse/soknad/innsending") {
            headers {
                set(NavHeaders.BRUKERDIALOG_GIT_SHA, UUID.randomUUID().toString())
                setBearerAuth(token.serialize())
            }
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
            content = JacksonConfiguration.configureObjectMapper().writeValueAsString(søknad)
        }.andExpect {
            status {
                isAccepted()
                header { exists(NavHeaders.X_CORRELATION_ID) }
            }
        }

        coVerify(exactly = 1, timeout = 120 * 1000) {
            dokumentService.slettDokumenter(any(), any())
        }

        k9DittnavVarselConsumer.lesMelding(
            key = oppgaveReferanse,
            topic = DittnavVarselTopologyConfiguration.K9_DITTNAV_VARSEL_TOPIC
        ).value().assertDittnavVarsel(
            K9Beskjed(
                metadata = no.nav.brukerdialog.utils.SøknadUtils.metadata,
                grupperingsId = oppgaveReferanse,
                tekst = "Søknad om ungdomsytelse er mottatt",
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
        val deltakelseId = UUID.randomUUID()
        val mottattString = "2020-01-01T10:30:15Z"
        val mottatt = ZonedDateTime.parse(mottattString, JacksonConfiguration.zonedDateTimeFormatter)
        val søknadMottatt = UngdomsytelsesøknadUtils.gyldigSøknad(søknadId = søknadId, deltakelseId = deltakelseId, mottatt = mottatt)
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
            topic = UngdomsytelsesøknadTopologyConfiguration.UNGDOMSYTELSE_SØKNAD_MOTTATT_TOPIC
        )
        val lesMelding =
            consumer.lesMelding(
                key = søknadId,
                topic = UngdomsytelsesøknadTopologyConfiguration.UNGDOMSYTELSE_SØKNAD_PREPROSESSERT_TOPIC,
                maxWaitInSeconds = 120
            ).value()

        val preprosessertSøknadJson = JSONObject(lesMelding).getJSONObject("data").toString()
        JSONAssert.assertEquals(preprosessertSøknadSomJson(søknadId, deltakelseId.toString(), mottattString), preprosessertSøknadJson, true)
    }
    @Language("JSON")
    private fun preprosessertSøknadSomJson(søknadId: String, deltakelseId: String, mottatt: String) = """
        {
          "oppgaveReferanse": "$søknadId",
          "mottatt": "$mottatt",
          "søker": {
            "etternavn": "Nordmann",
            "mellomnavn": "Mellomnavn",
            "aktørId": "123456",
            "fødselsdato": "2000-01-01",
            "fornavn": "Ola",
            "fødselsnummer": "02119970078"
          },
          "startdato": "2022-01-01",
          "barn": [
            {
              "navn": "Ola Nordmann"
            }
          ],
          "barnErRiktig": true,
          "kontonummerFraRegister": "12345678901",
          "kontonummerErRiktig": true,
          "språk": "nb",
          "harForståttRettigheterOgPlikter": true,
          "dokumentId": [
            [
              "123456789",
              "987654321"
            ]
          ],
          "harBekreftetOpplysninger": true,
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
              "søknadType": "DELTAKELSE_SØKNAD",
              "deltakelseId": "$deltakelseId",
              "søktFraDatoer": ["2022-01-01"],
              "inntekter": null
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
