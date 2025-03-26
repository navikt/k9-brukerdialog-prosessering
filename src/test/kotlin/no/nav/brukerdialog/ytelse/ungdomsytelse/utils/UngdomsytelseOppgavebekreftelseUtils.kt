package no.nav.brukerdialog.ytelse.ungdomsytelse.utils

import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.BekreftelseSvar
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.KomplettEndretSluttdatoUngdomsytelseOppgaveDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.KomplettEndretStartdatoUngdomsytelseOppgaveDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.KomplettUngdomsytelseOppgaveDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.oppgavebekreftelse.domene.UngdomsytelseOppgavebekreftelseMottatt
import no.nav.k9.oppgave.OppgaveBekreftelse
import no.nav.k9.oppgave.bekreftelse.Bekreftelse
import no.nav.k9.oppgave.bekreftelse.ung.periodeendring.EndretFomDatoBekreftelse
import no.nav.k9.oppgave.bekreftelse.ung.periodeendring.EndretTomDatoBekreftelse
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
        deltakelseId: String = UUID.randomUUID().toString(),
        oppgaveId: String = UUID.randomUUID().toString(),
        oppgave: KomplettUngdomsytelseOppgaveDTO = KomplettEndretStartdatoUngdomsytelseOppgaveDTO(
            oppgaveId = oppgaveId,
            veilederRef = "veilder-123",
            meldingFraVeileder = """Hei, jeg har endret startdatoen som vi avtalte i møtet. Fra: Pål Hønesen.
                """.trimMargin(),
            nyStartdato = LocalDate.parse("2025-01-01"),
            bekreftelseSvar = BekreftelseSvar.GODTAR
        ),
        mottatt: ZonedDateTime = ZonedDateTime.of(2018, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC")),
    ): UngdomsytelseOppgavebekreftelseMottatt {

        return UngdomsytelseOppgavebekreftelseMottatt(
            språk = "nb",
            deltakelseId = deltakelseId,
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
            k9Format = gyldigK9FormatOppgave(oppgaveId, mottatt, oppgave.somK9Format()),
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

private fun KomplettUngdomsytelseOppgaveDTO.somK9Format(): Bekreftelse {
    return when (this) {
        is KomplettEndretStartdatoUngdomsytelseOppgaveDTO -> EndretFomDatoBekreftelse(
            UUID.fromString(oppgaveId),
            nyStartdato,
            bekreftelseSvar.somBoolean()
        )

        is KomplettEndretSluttdatoUngdomsytelseOppgaveDTO -> EndretTomDatoBekreftelse(
            UUID.fromString(oppgaveId),
            nySluttdato,
            bekreftelseSvar.somBoolean()
        )

        else -> throw IllegalArgumentException("Ukjent oppgavetype")
    }
}
