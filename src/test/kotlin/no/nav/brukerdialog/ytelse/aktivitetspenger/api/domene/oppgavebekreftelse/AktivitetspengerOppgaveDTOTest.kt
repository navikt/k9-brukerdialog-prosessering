package no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.oppgavebekreftelse

import no.nav.brukerdialog.config.JacksonConfiguration
import no.nav.k9.oppgave.bekreftelse.Bekreftelse
import no.nav.ung.brukerdialog.kontrakt.oppgaver.BrukerdialogOppgaveDto
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveStatus
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.aktivitet.AktivitetsavklaringKildeType
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.aktivitet.AktivitetsvilkåretIkkeOppfyltÅrsak
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.aktivitet.BekreftAktivitetOppgavetypeDataDto
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.aktivitet.BekreftAktivitetOpphørOppgavetypeDataDto
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bistand.BekreftBistandOppgavetypeDataDto
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bistand.BekreftBistandOpphørOppgavetypeDataDto
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bistand.BistandsavklaringKildeType
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bistand.BistandsvilkårIkkeOppfyltÅrsak
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BekreftBostedOppgavetypeDataDto
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BekreftBostedOpphørOppgavetypeDataDto
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BostedsavklaringKildeType
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BostedsvilkårIkkeOppfyltÅrsak
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.livsopphold.AndreLivsoppholdsytelserAvklaringKildeType
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.livsopphold.AndreLivsoppholdsytelserIkkeOppfyltÅrsak
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.livsopphold.BekreftAndreLivsoppholdsytelserOppgavetypeDataDto
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.livsopphold.BekreftAndreLivsoppholdsytelserOpphørOppgavetypeDataDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.reflect.KClass

class AktivitetspengerOppgaveDTOTest {

    private val objectMapper = JacksonConfiguration.configureObjectMapper()

    @ParameterizedTest
    @MethodSource("oppgavetypeDataOgForventetResultat")
    fun `somKomplettOppgave velger riktig Komplett-DTO og overlever en serialiseringsrunde`(
        oppgavetypeData: OppgavetypeDataDto,
        oppgavetype: OppgaveType,
        forventetKomplettType: KClass<out KomplettAktivitetspengerOppgaveDTO>,
        forventetBekreftelseType: Bekreftelse.Type,
    ) {
        val oppgaveReferanse = UUID.randomUUID().toString()
        val dto = AktivitetspengerOppgaveDTO(
            oppgaveReferanse = oppgaveReferanse,
            uttalelse = AktivitetspengerOppgaveUttalelseDTO(harUttalelse = false),
        )
        val oppgaveDTO = BrukerdialogOppgaveDto(
            UUID.fromString(oppgaveReferanse),
            oppgavetype,
            oppgavetypeData,
            OppgaveYtelsetype.AKTIVITETSPENGER,
            null,
            OppgaveStatus.ULØST,
            ZonedDateTime.now(),
            null,
            null,
        )

        val komplettOppgave = dto.somKomplettOppgave(oppgaveDTO)

        assertEquals(forventetKomplettType, komplettOppgave::class)
        assertEquals(forventetBekreftelseType, komplettOppgave.somK9Format().type)

        val json = objectMapper.writeValueAsString(komplettOppgave)
        val gjenopprettet = objectMapper.readValue(json, KomplettAktivitetspengerOppgaveDTO::class.java)
        assertEquals(komplettOppgave, gjenopprettet)
    }

    @Test
    fun `melding med type AVP_BOSTED_AVKLARING kan leses`() {
        val json = """{"type":"AVP_BOSTED_AVKLARING","oppgaveReferanse":"${UUID.randomUUID()}","uttalelse":{"harUttalelse":false}}"""

        val gjenopprettet = objectMapper.readValue(json, KomplettAktivitetspengerOppgaveDTO::class.java)

        assertEquals(KomplettBekreftBostedOppgaveDTO::class, gjenopprettet::class)
    }

    private companion object {
        @JvmStatic
        fun oppgavetypeDataOgForventetResultat(): List<Arguments> = listOf(
            Arguments.of(
                BekreftBostedOppgavetypeDataDto(
                    LocalDate.parse("2025-06-01"),
                    LocalDate.parse("2025-06-30"),
                    false,
                    "fordi",
                    BostedsvilkårIkkeOppfyltÅrsak.IKKE_BOSTEDSADRESSE_OG_IKKE_FOLKEREGISTRERT_I_TRONDHEIM,
                    BostedsavklaringKildeType.FOLKEREGISTER,
                    null,
                ),
                OppgaveType.BEKREFT_BOSTED,
                KomplettBekreftBostedOppgaveDTO::class,
                Bekreftelse.Type.AVP_BOSTED_AVKLARING,
            ),
            Arguments.of(
                BekreftBostedOpphørOppgavetypeDataDto(
                    LocalDate.parse("2025-06-01"),
                    false,
                    "fordi",
                    BostedsvilkårIkkeOppfyltÅrsak.IKKE_BOSTEDSADRESSE_OG_IKKE_FOLKEREGISTRERT_I_TRONDHEIM,
                    BostedsavklaringKildeType.FOLKEREGISTER,
                    null,
                ),
                OppgaveType.BEKREFT_BOSTED,
                KomplettBekreftBostedOppgaveDTO::class,
                Bekreftelse.Type.AVP_BOSTED_AVKLARING,
            ),
            Arguments.of(
                BekreftBistandOppgavetypeDataDto(
                    LocalDate.parse("2025-06-01"),
                    LocalDate.parse("2025-06-30"),
                    BistandsvilkårIkkeOppfyltÅrsak.KOMMET_I_ARBEID,
                    null,
                    BistandsavklaringKildeType.NAV,
                    null,
                ),
                OppgaveType.BEKREFT_BISTAND,
                KomplettBekreftBistandOppgaveDTO::class,
                Bekreftelse.Type.AVP_BISTANDSBEHOV_AVKLARING,
            ),
            Arguments.of(
                BekreftBistandOpphørOppgavetypeDataDto(
                    LocalDate.parse("2025-06-01"),
                    BistandsvilkårIkkeOppfyltÅrsak.KOMMET_I_ARBEID,
                    null,
                    BistandsavklaringKildeType.NAV,
                    null,
                ),
                OppgaveType.BEKREFT_BISTAND,
                KomplettBekreftBistandOppgaveDTO::class,
                Bekreftelse.Type.AVP_BISTANDSBEHOV_AVKLARING,
            ),
            Arguments.of(
                BekreftAndreLivsoppholdsytelserOppgavetypeDataDto(
                    LocalDate.parse("2025-06-01"),
                    LocalDate.parse("2025-06-30"),
                    AndreLivsoppholdsytelserIkkeOppfyltÅrsak.MOTTAR_DAGPENGER,
                    null,
                    AndreLivsoppholdsytelserAvklaringKildeType.NAV,
                    null,
                ),
                OppgaveType.BEKREFT_ANDRE_LIVSOPPHOLDSYTELSER,
                KomplettBekreftAndreLivsoppholdsytelserOppgaveDTO::class,
                Bekreftelse.Type.AVP_ANDRE_LIVSOPPHOLDSYTELSER_AVKLARING,
            ),
            Arguments.of(
                BekreftAndreLivsoppholdsytelserOpphørOppgavetypeDataDto(
                    LocalDate.parse("2025-06-01"),
                    AndreLivsoppholdsytelserIkkeOppfyltÅrsak.MOTTAR_DAGPENGER,
                    null,
                    AndreLivsoppholdsytelserAvklaringKildeType.NAV,
                    null,
                ),
                OppgaveType.BEKREFT_ANDRE_LIVSOPPHOLDSYTELSER,
                KomplettBekreftAndreLivsoppholdsytelserOppgaveDTO::class,
                Bekreftelse.Type.AVP_ANDRE_LIVSOPPHOLDSYTELSER_AVKLARING,
            ),
            Arguments.of(
                BekreftAktivitetOppgavetypeDataDto(
                    LocalDate.parse("2025-06-01"),
                    LocalDate.parse("2025-06-30"),
                    AktivitetsvilkåretIkkeOppfyltÅrsak.ANNET,
                    "fordi",
                    AktivitetsavklaringKildeType.NAV,
                    null,
                ),
                OppgaveType.BEKREFT_AKTIVITET,
                KomplettBekreftAktivitetOppgaveDTO::class,
                Bekreftelse.Type.AVP_AKTIVITET_AVKLARING,
            ),
            Arguments.of(
                BekreftAktivitetOpphørOppgavetypeDataDto(
                    LocalDate.parse("2025-06-01"),
                    AktivitetsvilkåretIkkeOppfyltÅrsak.ANNET,
                    "fordi",
                    AktivitetsavklaringKildeType.NAV,
                    null,
                ),
                OppgaveType.BEKREFT_AKTIVITET,
                KomplettBekreftAktivitetOppgaveDTO::class,
                Bekreftelse.Type.AVP_AKTIVITET_AVKLARING,
            ),
        )
    }
}
