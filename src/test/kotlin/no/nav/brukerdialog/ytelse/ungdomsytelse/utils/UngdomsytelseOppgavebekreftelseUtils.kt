package no.nav.brukerdialog.ytelse.ungdomsytelse.utils

import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.BekreftelseSvar
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.EndretStartdatoUngdomsytelseOppgaveDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.oppgavebekreftelse.domene.UngdomsytelseOppgavebekreftelseMottatt
import no.nav.k9.søknad.felles.Kildesystem
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.ung.v1.UngSøknadstype
import no.nav.k9.søknad.ytelse.ung.v1.Ungdomsytelse
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as k9FormatSøknad
import no.nav.k9.søknad.felles.personopplysninger.Søker as K9Søker

object UngdomsytelseOppgavebekreftelseUtils {

    fun oppgavebekreftelseMottatt(
        søkerFødselsnummer: String = "02119970078",
        deltakelseId: String = UUID.randomUUID().toString(),
        oppgaveId: String = UUID.randomUUID().toString(),
        mottatt: ZonedDateTime = ZonedDateTime.of(2018, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC")),
    ): UngdomsytelseOppgavebekreftelseMottatt {
        val startdato = LocalDate.parse("2022-01-01")

        return UngdomsytelseOppgavebekreftelseMottatt(
            språk = "nb",
            deltakelseId = deltakelseId,
            oppgave = EndretStartdatoUngdomsytelseOppgaveDTO(
                oppgaveId = oppgaveId,
                veilederRef = "veilder-123",
                meldingFraVeileder = """Hei, jeg har endret startdatoen som vi avtalte i møtet. Fra: Pål Hønesen.
                """.trimMargin(),
                nyStartdato = LocalDate.parse("2025-01-01"),
                bekreftelseSvar = BekreftelseSvar.GODTAR
            ),
            mottatt = mottatt,
            søker = Søker(
                aktørId = "123456",
                fødselsnummer = søkerFødselsnummer,
                fødselsdato = LocalDate.parse("2000-01-01"),
                etternavn = "Nordmann",
                mellomnavn = "Mellomnavn",
                fornavn = "Ola"
            ),
            k9Format = gyldigK9Format(oppgaveId.toString(), mottatt, startdato),
        )
    }

    fun gyldigK9Format(
        søknadId: String = UUID.randomUUID().toString(),
        mottatt: ZonedDateTime,
        fraOgMed: LocalDate,
    ): k9FormatSøknad {
        val ytelse = Ungdomsytelse()
            .medSøknadType(UngSøknadstype.DELTAKELSE_SØKNAD)
            .medStartdato(fraOgMed)

        val søknad = k9FormatSøknad(
            SøknadId(søknadId),
            Versjon("1.0.0"),
            mottatt,
            K9Søker(NorskIdentitetsnummer.of("02119970078")),
            ytelse

        ).medKildesystem(Kildesystem.SØKNADSDIALOG)
        return søknad
    }
}
