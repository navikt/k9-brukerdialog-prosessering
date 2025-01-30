package no.nav.brukerdialog.ytelse.ungdomsytelse.utils

import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.OppgittInntektForPeriode
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.domene.UngdomsytelsesøknadMottatt
import no.nav.k9.søknad.felles.Kildesystem
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.ung.v1.OppgittInntekt
import no.nav.k9.søknad.ytelse.ung.v1.UngSøknadstype
import no.nav.k9.søknad.ytelse.ung.v1.Ungdomsytelse
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as k9FormatSøknad
import no.nav.k9.søknad.felles.personopplysninger.Søker as K9Søker

object UngdomsytelsesøknadUtils {

    fun gyldigSøknad(
        søkerFødselsnummer: String = "02119970078",
        søknadId: String = UUID.randomUUID().toString(),
        søknadstype: UngSøknadstype = UngSøknadstype.DELTAKELSE_SØKNAD,
        oppgittInntekt: OppgittInntektForPeriode? = null,
        mottatt: ZonedDateTime = ZonedDateTime.of(2018, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC"))
    ): UngdomsytelsesøknadMottatt {
        val startdato = LocalDate.parse("2022-01-01")

        return UngdomsytelsesøknadMottatt(
            språk = "nb",
            søknadId = søknadId,
            mottatt = mottatt,
            søker = Søker(
                aktørId = "123456",
                fødselsnummer = søkerFødselsnummer,
                fødselsdato = LocalDate.parse("2000-01-01"),
                etternavn = "Nordmann",
                mellomnavn = "Mellomnavn",
                fornavn = "Ola"
            ),
            startdato = startdato,
            søknadstype = søknadstype,
            inntektForPeriode = oppgittInntekt,
            k9Format = gyldigK9Format(søknadId, søknadstype, mottatt, startdato, oppgittInntekt?.somUngOppgittInntektForPeriode()),
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true
        )
    }

    fun gyldigK9Format(
        søknadId: String = UUID.randomUUID().toString(),
        søknadstype: UngSøknadstype,
        mottatt: ZonedDateTime,
        fraOgMed: LocalDate,
        oppgittInntekt: OppgittInntekt? = null,
    ): k9FormatSøknad {
        val ytelse = Ungdomsytelse()
            .medSøknadType(søknadstype)
            .medStartdato(fraOgMed)

        if (oppgittInntekt != null) {
            ytelse.medInntekter(oppgittInntekt)
        }

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
