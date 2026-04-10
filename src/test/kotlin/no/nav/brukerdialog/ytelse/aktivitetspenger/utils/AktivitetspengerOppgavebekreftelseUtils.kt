package no.nav.brukerdialog.ytelse.aktivitetspenger.utils

import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.oppgavebekreftelse.AktivitetspengerOppgaveDTO
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.oppgavebekreftelse.AktivitetspengerOppgavebekreftelse
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.oppgavebekreftelse.AktivitetspengerOppgaveUttalelseDTO
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.oppgavebekreftelse.KomplettAktivitetspengerOppgaveDTO
import no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.oppgavebekreftelse.domene.AktivitetspengerOppgavebekreftelseMottatt
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.k9.oppgave.OppgaveBekreftelse
import no.nav.k9.oppgave.bekreftelse.Bekreftelse
import no.nav.k9.søknad.felles.Kildesystem
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.ArbeidOgFrilansRegisterInntektDTO
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.RegisterinntektDTO
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.YtelseRegisterInntektDTO
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.YtelseType
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID
import no.nav.k9.søknad.felles.personopplysninger.Søker as K9Søker

object AktivitetspengerOppgavebekreftelseUtils {

    val defaultRegisterinntekt = RegisterinntektDTO(
        listOf(
            ArbeidOgFrilansRegisterInntektDTO(50000, "123456789", "Testarbeidsgiver AS")
        ),
        listOf(
            YtelseRegisterInntektDTO(10000, YtelseType.SYKEPENGER)
        ),
    )

    val defaultKomplettOppgave = KomplettAktivitetspengerOppgaveDTO(
        oppgaveReferanse = UUID.randomUUID().toString(),
        uttalelse = AktivitetspengerOppgaveUttalelseDTO(harUttalelse = false),
        fraOgMed = LocalDate.parse("2025-06-01"),
        tilOgMed = LocalDate.parse("2025-06-30"),
        registerinntekt = defaultRegisterinntekt,
    )

    val defaultOppgavebekreftelse = AktivitetspengerOppgavebekreftelse(
        oppgave = AktivitetspengerOppgaveDTO(
            oppgaveReferanse = UUID.randomUUID().toString(),
            uttalelse = AktivitetspengerOppgaveUttalelseDTO(harUttalelse = false),
        )
    )

    fun oppgavebekreftelseMottatt(
        søkerFødselsnummer: String = "02119970078",
        oppgaveReferanse: String = UUID.randomUUID().toString(),
        oppgave: KomplettAktivitetspengerOppgaveDTO = defaultKomplettOppgave.copy(
            oppgaveReferanse = oppgaveReferanse
        ),
        mottatt: ZonedDateTime = ZonedDateTime.of(2018, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC")),
    ): AktivitetspengerOppgavebekreftelseMottatt {
        return AktivitetspengerOppgavebekreftelseMottatt(
            språk = "nb",
            oppgave = oppgave,
            mottatt = mottatt,
            søker = Søker(
                aktørId = "123456",
                fødselsnummer = søkerFødselsnummer,
                fødselsdato = LocalDate.parse("2000-01-01"),
                etternavn = "Nordmann",
                mellomnavn = "Mellomnavn",
                fornavn = "Ola",
            ),
            k9Format = gyldigK9FormatOppgave(oppgaveReferanse, mottatt, oppgave.somK9Format()),
        )
    }

    fun gyldigK9FormatOppgave(
        søknadId: String = UUID.randomUUID().toString(),
        mottatt: ZonedDateTime,
        bekreftelse: Bekreftelse,
    ): OppgaveBekreftelse {
        return OppgaveBekreftelse()
            .medSøknadId(søknadId)
            .medVersjon("1.0.0")
            .medMottattDato(mottatt)
            .medSøker(K9Søker(NorskIdentitetsnummer.of("02119970078")))
            .medBekreftelse(bekreftelse)
            .medKildesystem(Kildesystem.SØKNADSDIALOG)
    }
}
