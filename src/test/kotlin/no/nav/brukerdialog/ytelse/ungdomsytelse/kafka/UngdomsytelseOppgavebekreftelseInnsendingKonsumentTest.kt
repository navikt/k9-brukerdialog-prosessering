package no.nav.brukerdialog.ytelse.ungdomsytelse.kafka

import com.ninjasquad.springmockk.MockkBean
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
import no.nav.brukerdialog.utils.NavHeaders
import no.nav.brukerdialog.utils.TokenTestUtils.hentToken
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.BekreftelseSvar
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.EndretStartdatoUngdomsytelseOppgaveDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.oppgavebekreftelse.UngdomsytelseOppgavebekreftelseTopologyConfiguration
import no.nav.brukerdialog.ytelse.ungdomsytelse.utils.SøknadUtils
import no.nav.brukerdialog.ytelse.ungdomsytelse.utils.UngdomsytelseOppgavebekreftelseUtils
import org.intellij.lang.annotations.Language
import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import java.net.URI
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

class UngdomsytelseOppgavebekreftelseInnsendingKonsumentTest : AbstractIntegrationTest() {

    override val consumerGroupPrefix = "ungdomsytelse-oppgavebekreftelse"
    override val consumerGroupTopics = listOf(
        UngdomsytelseOppgavebekreftelseTopologyConfiguration.UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_MOTTATT_TOPIC,
        UngdomsytelseOppgavebekreftelseTopologyConfiguration.UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_PREPROSESSERT_TOPIC,
        UngdomsytelseOppgavebekreftelseTopologyConfiguration.UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_CLEANUP_TOPIC
    )


    @Test
    fun `forvent at melding konsumeres riktig og dokumenter blir slettet`() {
        val søker = mockSøker()
        mockBarn()
        mockLagreDokument()
        mockJournalføring()
        mockHentingAvOppgave()

        val oppgaveId = UUID.randomUUID()
        val oppgavebekreftelse = SøknadUtils.defaultOppgavebekreftelse.copy(
            oppgave = EndretStartdatoUngdomsytelseOppgaveDTO(
                oppgaveId = oppgaveId.toString(),
                bekreftelseSvar = BekreftelseSvar.GODTAR
            )
        )

        val token = mockOAuth2Server.hentToken()
        mockMvc.post("/ungdomsytelse/oppgavebekreftelse/innsending") {
            headers {
                set(NavHeaders.BRUKERDIALOG_GIT_SHA, UUID.randomUUID().toString())
                setBearerAuth(token.serialize())
            }
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
            content = JacksonConfiguration.configureObjectMapper().writeValueAsString(oppgavebekreftelse)
        }.andExpect {
            status {
                isAccepted()
                header { exists(NavHeaders.X_CORRELATION_ID) }
            }
        }

        coVerify(exactly = 1, timeout = 120 * 1000) {
            k9DokumentMellomlagringService.slettDokumenter(any(), any())
        }

        k9DittnavVarselConsumer.lesMelding(
            key = oppgaveId.toString(),
            topic = DittnavVarselTopologyConfiguration.K9_DITTNAV_VARSEL_TOPIC
        ).value().assertDittnavVarsel(
            K9Beskjed(
                metadata = no.nav.brukerdialog.utils.SøknadUtils.metadata,
                grupperingsId = oppgaveId.toString(),
                tekst = "Bekreftelse om endring av ungdomsprogramopplysninger er mottatt",
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
        val deltakelseId = UUID.randomUUID().toString()
        val oppgaveId = UUID.randomUUID().toString()
        val mottattString = "2020-01-01T10:30:15Z"
        val mottatt = ZonedDateTime.parse(mottattString, JacksonConfiguration.zonedDateTimeFormatter)
        val oppgavebekreftelseMottatt = UngdomsytelseOppgavebekreftelseUtils.oppgavebekreftelseMottatt(
            deltakelseId = deltakelseId,
            oppgaveId = oppgaveId,
            mottatt = mottatt
        )
        val correlationId = UUID.randomUUID().toString()
        val metadata = MetaInfo(version = 1, correlationId = correlationId)
        val topicEntry = TopicEntry(metadata, oppgavebekreftelseMottatt)
        val topicEntryJson = objectMapper.writeValueAsString(topicEntry)

        coEvery { k9DokumentMellomlagringService.lagreDokument(any()) }
            .throws(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenMany(listOf("123456789", "987654321").map { URI("http://localhost:8080/dokument/$it") })

        producer.leggPåTopic(
            key = oppgaveId,
            value = topicEntryJson,
            topic = UngdomsytelseOppgavebekreftelseTopologyConfiguration.UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_MOTTATT_TOPIC
        )
        val lesMelding =
            consumer.lesMelding(
                key = oppgaveId,
                topic = UngdomsytelseOppgavebekreftelseTopologyConfiguration.UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_PREPROSESSERT_TOPIC,
                maxWaitInSeconds = 120
            ).value()

        val preprosessertSøknadJson = JSONObject(lesMelding).getJSONObject("data").toString()
        JSONAssert.assertEquals(
            preprosessertSøknadSomJson(deltakelseId, oppgaveId, mottattString),
            preprosessertSøknadJson,
            true
        )
    }

    @Language("JSON")
    private fun preprosessertSøknadSomJson(deltakelseId: String, oppgaveId: String, mottatt: String) = """
        {
          "deltakelseId": "$deltakelseId",
          "oppgave": {
            "type": "BEKREFT_ENDRET_STARTDATO",
            "oppgaveId": "$oppgaveId",
            "veilederRef": "veilder-123",
            "meldingFraVeileder": "Hei, jeg har endret startdatoen som vi avtalte i møtet. Fra: Pål Hønesen.",
            "nyStartdato": "2025-01-01",
            "bekreftelseSvar": "GODTAR",
            "ikkeGodkjentResponse": null
          },
          "mottatt": "$mottatt",
          "søker": {
            "etternavn": "Nordmann",
            "mellomnavn": "Mellomnavn",
            "aktørId": "123456",
            "fødselsdato": "2000-01-01",
            "fornavn": "Ola",
            "fødselsnummer": "02119970078"
          },
          "språk": "nb",
          "dokumentId": [
            [
              "123456789",
              "987654321"
            ]
          ],
          "k9Format": {
            "søknadId": "$oppgaveId",
            "versjon": "1.0.0",
            "språk": "nb",
            "mottattDato": "$mottatt",
            "søker": {
              "norskIdentitetsnummer": "02119970078"
            },
            "bekreftelse": {
              "type": "UNG_ENDRET_FOM_DATO",
              "nyFomDato": "2025-01-01",
              "oppgaveId": "$oppgaveId",
              "uttalelseFraBruker": null,
              "harBrukerGodtattEndringen": true,
              "dataBruktTilUtledning": null
            },
            "kildesystem": "søknadsdialog"
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
