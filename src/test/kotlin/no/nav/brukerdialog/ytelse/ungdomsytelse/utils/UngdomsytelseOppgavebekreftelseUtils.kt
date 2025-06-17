package no.nav.brukerdialog.ytelse.ungdomsytelse.utils

import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.KomplettEndretStartdatoUngdomsytelseOppgaveDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.KomplettUngdomsytelseOppgaveDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.UngdomsytelseOppgaveUttalelseDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.oppgavebekreftelse.domene.UngdomsytelseOppgavebekreftelseMottatt
import no.nav.k9.oppgave.OppgaveBekreftelse
import no.nav.k9.oppgave.bekreftelse.Bekreftelse
import no.nav.k9.søknad.felles.Kildesystem
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.felles.personopplysninger.Søker as K9Søker

object UngdomsytelseOppgavebekreftelseUtils {

    fun oppgavebekreftelseMottatt(
        søkerFødselsnummer: String = "02119970078",
        oppgaveReferanse: String = UUID.randomUUID().toString(),
        oppgave: KomplettUngdomsytelseOppgaveDTO = KomplettEndretStartdatoUngdomsytelseOppgaveDTO(
            oppgaveReferanse = oppgaveReferanse,
            uttalelse = UngdomsytelseOppgaveUttalelseDTO(
                harUttalelse = false
            ),
            nyStartdato = LocalDate.parse("2025-12-01"),
        ),
        mottatt: ZonedDateTime = ZonedDateTime.of(2018, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC")),
    ): UngdomsytelseOppgavebekreftelseMottatt {

        return UngdomsytelseOppgavebekreftelseMottatt(
            språk = "nb",
            oppgave = oppgave,
            mottatt = mottatt,
            søker = Søker(
                aktørId = "123456",
                fødselsnummer = søkerFødselsnummer,
                fødselsdato = LocalDate.parse("2000-01-01"),
                etternavn = "Nordmann",
                mellomnavn = "Mellomnavn",
                fornavn = "Ola"
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
