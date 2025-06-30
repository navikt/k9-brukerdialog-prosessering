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
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.UngdomsytelseOppgaveDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.UngdomsytelseOppgaveUttalelseDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.oppgavebekreftelse.UngdomsytelseOppgavebekreftelseTopologyConfiguration
import no.nav.brukerdialog.ytelse.ungdomsytelse.utils.SøknadUtils
import no.nav.brukerdialog.ytelse.ungdomsytelse.utils.UngdomsytelseOppgavebekreftelseUtils
import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.felles.EndretStartdatoDataDTO
import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.felles.Oppgavetype
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
        mockHentingAvOppgave(
            oppgavetype = Oppgavetype.BEKREFT_ENDRET_STARTDATO,
            oppgavetypeData = EndretStartdatoDataDTO(
                nyStartdato = LocalDate.now(),
                forrigeStartdato = LocalDate.now().minusMonths(1)
            )
        )
        mockMarkerOppgaveSomLøst()

        val oppgaveReferanse = UUID.randomUUID()
        val oppgavebekreftelse = SøknadUtils.defaultOppgavebekreftelse.copy(
            oppgave = UngdomsytelseOppgaveDTO(
                oppgaveReferanse = oppgaveReferanse.toString(),
                uttalelse = UngdomsytelseOppgaveUttalelseDTO(
                    harUttalelse = false
                )
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
            dokumentService.slettDokumenter(any(), any())
        }

        k9DittnavVarselConsumer.lesMelding(
            key = oppgaveReferanse.toString(),
            topic = DittnavVarselTopologyConfiguration.K9_DITTNAV_VARSEL_TOPIC
        ).value().assertDittnavVarsel(
            K9Beskjed(
                metadata = no.nav.brukerdialog.utils.SøknadUtils.metadata,
                grupperingsId = oppgaveReferanse.toString(),
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
        val oppgaveReferanse = UUID.randomUUID().toString()
        val mottattString = "2020-01-01T10:30:15Z"
        val mottatt = ZonedDateTime.parse(mottattString, JacksonConfiguration.zonedDateTimeFormatter)
        val oppgavebekreftelseMottatt = UngdomsytelseOppgavebekreftelseUtils.oppgavebekreftelseMottatt(
            oppgaveReferanse = oppgaveReferanse,
            mottatt = mottatt
        )
        val correlationId = UUID.randomUUID().toString()
        val metadata = MetaInfo(version = 1, correlationId = correlationId)
        val topicEntry = TopicEntry(metadata, oppgavebekreftelseMottatt)
        val topicEntryJson = objectMapper.writeValueAsString(topicEntry)

        coEvery { dokumentService.lagreDokument(any(), any(), any()) }
            .throws(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenMany(listOf("123456789", "987654321"))

        producer.leggPåTopic(
            key = oppgaveReferanse,
            value = topicEntryJson,
            topic = UngdomsytelseOppgavebekreftelseTopologyConfiguration.UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_MOTTATT_TOPIC
        )
        val lesMelding =
            consumer.lesMelding(
                key = oppgaveReferanse,
                topic = UngdomsytelseOppgavebekreftelseTopologyConfiguration.UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_PREPROSESSERT_TOPIC,
                maxWaitInSeconds = 120
            ).value()

        val preprosessertSøknadJson = JSONObject(lesMelding).getJSONObject("data").toString()
        JSONAssert.assertEquals(
            preprosessertSøknadSomJson(oppgaveReferanse, mottattString),
            preprosessertSøknadJson,
            true
        )
    }

    @Language("JSON")
    private fun preprosessertSøknadSomJson(oppgaveReferanse: String, mottatt: String) = """
        {
          "oppgave": {
            "type": "UNG_ENDRET_STARTDATO",
            "oppgaveReferanse": "$oppgaveReferanse",
            "uttalelse": {
                "harUttalelse": false,
                "uttalelseFraDeltaker": null
            },
            "nyStartdato": "2025-12-01"
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
            "søknadId": "$oppgaveReferanse",
            "versjon": "1.0.0",
            "språk": "nb",
            "mottattDato": "$mottatt",
            "søker": {
              "norskIdentitetsnummer": "02119970078"
            },
            "bekreftelse": {
              "type": "UNG_ENDRET_STARTDATO",
              "nyStartdato": "2025-12-01",
              "oppgaveReferanse": "$oppgaveReferanse",
              "uttalelseFraBruker": null,
              "harUttalelse": false,
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
