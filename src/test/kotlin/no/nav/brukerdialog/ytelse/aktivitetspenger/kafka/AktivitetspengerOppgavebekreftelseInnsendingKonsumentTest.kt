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
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.aktivitet.AktivitetsavklaringKildeType
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.aktivitet.AktivitetsvilkåretIkkeOppfyltÅrsak
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.aktivitet.BekreftAktivitetOppgavetypeDataDto
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bistand.BekreftBistandOppgavetypeDataDto
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bistand.BistandsavklaringKildeType
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bistand.BistandsvilkårIkkeOppfyltÅrsak
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BekreftBostedOppgavetypeDataDto
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BostedsavklaringKildeType
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BostedsvilkårIkkeOppfyltÅrsak
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.KontrollerRegisterinntektOppgavetypeDataDto
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.livsopphold.AndreLivsoppholdsytelserAvklaringKildeType
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.livsopphold.AndreLivsoppholdsytelserIkkeOppfyltÅrsak
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.livsopphold.BekreftAndreLivsoppholdsytelserOppgavetypeDataDto
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import java.time.LocalDate
import java.util.*

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

    @Test
    fun `forvent at bekreftelse av bosted konsumeres riktig og dokumenter blir slettet`() {
        val søker = mockSøker()
        mockBarn()
        mockLagreDokument()
        mockJournalføring()
        mockHentingAvOppgave(
            oppgavetype = OppgaveType.BEKREFT_BOSTED,
            oppgavetypeData = BekreftBostedOppgavetypeDataDto(
                LocalDate.parse("2025-06-01"),
                LocalDate.parse("2025-06-30"),
                false,
                "fordi",
                BostedsvilkårIkkeOppfyltÅrsak.IKKE_BOSTEDSADRESSE_OG_IKKE_FOLKEREGISTRERT_I_TRONDHEIM,
                BostedsavklaringKildeType.FOLKEREGISTER,
                null
            )
        )
        mockMarkerOppgaveSomLøst()

        val oppgaveReferanse = UUID.randomUUID()
        val oppgavebekreftelse = AktivitetspengerOppgavebekreftelseUtils.defaultOppgavebekreftelse.copy(
            oppgave = AktivitetspengerOppgaveDTO(
                oppgaveReferanse = oppgaveReferanse.toString(),
                uttalelse = AktivitetspengerOppgaveUttalelseDTO(
                    harUttalelse = true,
                    uttalelseFraDeltaker = "Jeg er ikke lenger bosatt i Trondheim, uttalelsen skal ikke gå tapt",
                ),
            )
        )

        val token = mockOAuth2Server.hentToken(subject = "00000000000")
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

    @Test
    fun `forvent at bekreftelse av bistand konsumeres riktig og dokumenter blir slettet`() {
        val søker = mockSøker()
        mockBarn()
        mockLagreDokument()
        mockJournalføring()
        mockHentingAvOppgave(
            oppgavetype = OppgaveType.BEKREFT_BISTAND,
            oppgavetypeData = BekreftBistandOppgavetypeDataDto(
                LocalDate.parse("2025-06-01"),
                LocalDate.parse("2025-06-30"),
                BistandsvilkårIkkeOppfyltÅrsak.KOMMET_I_ARBEID,
                null,
                BistandsavklaringKildeType.NAV,
                null,
            )
        )
        mockMarkerOppgaveSomLøst()

        val oppgaveReferanse = UUID.randomUUID()
        val oppgavebekreftelse = AktivitetspengerOppgavebekreftelseUtils.defaultOppgavebekreftelse.copy(
            oppgave = AktivitetspengerOppgaveDTO(
                oppgaveReferanse = oppgaveReferanse.toString(),
                uttalelse = AktivitetspengerOppgaveUttalelseDTO(
                    harUttalelse = true,
                    uttalelseFraDeltaker = "Jeg har fortsatt behov for bistand, uttalelsen skal ikke gå tapt",
                ),
            )
        )

        val token = mockOAuth2Server.hentToken(subject = "11111111111")
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

    @Test
    fun `forvent at bekreftelse av andre livsoppholdsytelser konsumeres riktig og dokumenter blir slettet`() {
        val søker = mockSøker()
        mockBarn()
        mockLagreDokument()
        mockJournalføring()
        mockHentingAvOppgave(
            oppgavetype = OppgaveType.BEKREFT_ANDRE_LIVSOPPHOLDSYTELSER,
            oppgavetypeData = BekreftAndreLivsoppholdsytelserOppgavetypeDataDto(
                LocalDate.parse("2025-06-01"),
                LocalDate.parse("2025-06-30"),
                AndreLivsoppholdsytelserIkkeOppfyltÅrsak.MOTTAR_DAGPENGER,
                null,
                AndreLivsoppholdsytelserAvklaringKildeType.NAV,
                null,
            )
        )
        mockMarkerOppgaveSomLøst()

        val oppgaveReferanse = UUID.randomUUID()
        val oppgavebekreftelse = AktivitetspengerOppgavebekreftelseUtils.defaultOppgavebekreftelse.copy(
            oppgave = AktivitetspengerOppgaveDTO(
                oppgaveReferanse = oppgaveReferanse.toString(),
                uttalelse = AktivitetspengerOppgaveUttalelseDTO(
                    harUttalelse = true,
                    uttalelseFraDeltaker = "Jeg mottar ikke lenger dagpenger, uttalelsen skal ikke gå tapt",
                ),
            )
        )

        val token = mockOAuth2Server.hentToken(subject = "22222222222")
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

    @Test
    fun `forvent at bekreftelse av aktivitet konsumeres riktig og dokumenter blir slettet`() {
        val søker = mockSøker()
        mockBarn()
        mockLagreDokument()
        mockJournalføring()
        mockHentingAvOppgave(
            oppgavetype = OppgaveType.BEKREFT_AKTIVITET,
            oppgavetypeData = BekreftAktivitetOppgavetypeDataDto(
                LocalDate.parse("2025-06-01"),
                LocalDate.parse("2025-06-30"),
                AktivitetsvilkåretIkkeOppfyltÅrsak.ANNET,
                "fordi",
                AktivitetsavklaringKildeType.NAV,
                null,
            )
        )
        mockMarkerOppgaveSomLøst()

        val oppgaveReferanse = UUID.randomUUID()
        val oppgavebekreftelse = AktivitetspengerOppgavebekreftelseUtils.defaultOppgavebekreftelse.copy(
            oppgave = AktivitetspengerOppgaveDTO(
                oppgaveReferanse = oppgaveReferanse.toString(),
                uttalelse = AktivitetspengerOppgaveUttalelseDTO(
                    harUttalelse = true,
                    uttalelseFraDeltaker = "Jeg oppfyller fortsatt kravet til aktivitet, uttalelsen skal ikke gå tapt",
                ),
            )
        )

        val token = mockOAuth2Server.hentToken(subject = "33333333333")
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
