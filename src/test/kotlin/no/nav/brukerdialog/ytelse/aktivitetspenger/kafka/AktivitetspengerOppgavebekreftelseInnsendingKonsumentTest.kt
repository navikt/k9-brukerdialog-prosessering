package no.nav.brukerdialog.ytelse.aktivitetspenger.kafka

import io.mockk.coVerify
import no.nav.brukerdialog.AbstractIntegrationTest
import no.nav.brukerdialog.config.JacksonConfiguration
import no.nav.brukerdialog.dittnavvarsel.DittnavVarselTopologyConfiguration
import no.nav.brukerdialog.dittnavvarsel.K9Beskjed
import no.nav.brukerdialog.utils.KafkaUtils.lesMelding
import no.nav.brukerdialog.utils.NavHeaders
import no.nav.brukerdialog.utils.TokenTestUtils.hentToken
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.oppgavebekreftelse.AktivitetspengerOppgaveDTO
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.oppgavebekreftelse.AktivitetspengerOppgaveUttalelseDTO
import no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.oppgavebekreftelse.AktivitetspengerOppgavebekreftelseTopologyConfiguration
import no.nav.brukerdialog.ytelse.aktivitetspenger.utils.AktivitetspengerOppgavebekreftelseUtils
import no.nav.brukerdialog.ytelse.aktivitetspenger.utils.SøknadUtils as AktivitetspengersøknadSøknadUtils
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.KontrollerRegisterinntektOppgavetypeDataDto
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import java.time.LocalDate
import java.util.UUID

class AktivitetspengerOppgavebekreftelseInnsendingKonsumentTest : AbstractIntegrationTest() {

    override val consumerGroupPrefix = "aktivitetspenger-oppgavebekreftelse"
    override val consumerGroupTopics = listOf(
        AktivitetspengerOppgavebekreftelseTopologyConfiguration.AKTIVITETSPENGER_OPPGAVEBEKREFTELSE_MOTTATT_TOPIC,
        AktivitetspengerOppgavebekreftelseTopologyConfiguration.AKTIVITETSPENGER_OPPGAVEBEKREFTELSE_PREPROSESSERT_TOPIC,
        AktivitetspengerOppgavebekreftelseTopologyConfiguration.AKTIVITETSPENGER_OPPGAVEBEKREFTELSE_CLEANUP_TOPIC,
    )

    @Test
    fun `forvent at melding konsumeres riktig og dokumenter blir slettet`() {
        val søker = mockSøker()
        mockBarn()
        mockLagreDokument()
        mockJournalføring()
        mockHentingAvOppgave(
            oppgavetype = OppgaveType.BEKREFT_AVVIK_REGISTERINNTEKT,
            oppgavetypeData = KontrollerRegisterinntektOppgavetypeDataDto(
                LocalDate.parse("2025-06-01"),
                LocalDate.parse("2025-06-30"),
                AktivitetspengerOppgavebekreftelseUtils.defaultRegisterinntekt,
                null,
            )
        )
        mockMarkerOppgaveSomLøst()

        val oppgaveReferanse = UUID.randomUUID()
        val oppgavebekreftelse = AktivitetspengerOppgavebekreftelseUtils.defaultOppgavebekreftelse.copy(
            oppgave = AktivitetspengerOppgaveDTO(
                oppgaveReferanse = oppgaveReferanse.toString(),
                uttalelse = AktivitetspengerOppgaveUttalelseDTO(harUttalelse = false),
            )
        )

        val token = mockOAuth2Server.hentToken()
        mockMvc.post("/aktivitetspenger/oppgavebekreftelse/innsending") {
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

        coVerify(exactly = 1, timeout = 60 * 1000) {
            dokumentService.slettDokumenter(any(), any())
        }

        k9DittnavVarselConsumer.lesMelding(
            key = oppgaveReferanse.toString(),
            topic = DittnavVarselTopologyConfiguration.K9_DITTNAV_VARSEL_TOPIC
        ).value().assertDittnavVarsel(
            K9Beskjed(
                metadata = no.nav.brukerdialog.utils.SøknadUtils.metadata,
                grupperingsId = oppgaveReferanse.toString(),
                tekst = "Bekreftelse om aktivitetspengeropplysninger er mottatt",
                link = null,
                dagerSynlig = 7,
                søkerFødselsnummer = søker.fødselsnummer,
                eventId = "testes ikke",
                ytelse = "AKTIVITETSPENGER",
            )
        )
    }

    private fun String.assertDittnavVarsel(k9Beskjed: K9Beskjed) {
        val k9BeskjedJson = org.json.JSONObject(this)
        Assertions.assertEquals(k9Beskjed.grupperingsId, k9BeskjedJson.getString("grupperingsId"))
        Assertions.assertEquals(k9Beskjed.tekst, k9BeskjedJson.getString("tekst"))
        Assertions.assertEquals(k9Beskjed.ytelse, k9BeskjedJson.getString("ytelse"))
        Assertions.assertEquals(k9Beskjed.dagerSynlig, k9BeskjedJson.getLong("dagerSynlig"))
    }
}
