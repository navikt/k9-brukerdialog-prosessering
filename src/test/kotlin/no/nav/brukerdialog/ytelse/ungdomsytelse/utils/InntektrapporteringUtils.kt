package no.nav.brukerdialog.ytelse.ungdomsytelse.utils

import no.nav.brukerdialog.config.JacksonConfiguration
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.OppgittInntektForPeriode
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.UngPeriode
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.inntektsrapportering.UngdomsytelseInntektsrapportering
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.inntektsrapportering.domene.UngdomsytelseInntektsrapporteringMottatt
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

object InntektrapporteringUtils {
    internal val defaultInntektsrapportering = UngdomsytelseInntektsrapportering(
        søknadId = "4e62f8de-1ff6-40e9-bdcd-10485c789094",
        mottatt = ZonedDateTime.parse("2025-01-01T03:04:05Z", JacksonConfiguration.zonedDateTimeFormatter),
        oppgittInntektForPeriode = OppgittInntektForPeriode(
            arbeidstakerOgFrilansInntekt = 3000,
            næringsinntekt = null,
            inntektFraYtelse = 0,
            periodeForInntekt = UngPeriode(
                fraOgMed = LocalDate.parse("2025-01-01"),
                tilOgMed = LocalDate.parse("2025-01-31")
            )
        ),
        harBekreftetInntekt = true,
    )

    fun gyldigInntektsrapportering(
        søkerFødselsnummer: String = "02119970078",
        søknadId: String = UUID.randomUUID().toString(),
        mottatt: ZonedDateTime = ZonedDateTime.of(2018, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC")),
        oppgittInntektForPeriode: OppgittInntektForPeriode,
        ): UngdomsytelseInntektsrapporteringMottatt {

        return UngdomsytelseInntektsrapporteringMottatt(
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
            oppgittInntektForPeriode = oppgittInntektForPeriode,
            harBekreftetInntekt = true,
            k9Format = gyldigK9Format(søknadId, mottatt, oppgittInntektForPeriode)
        )
    }

    fun gyldigK9Format(
        søknadId: String = UUID.randomUUID().toString(),
        mottatt: ZonedDateTime,
        oppgittInntektForPeriode: OppgittInntektForPeriode,
    ): k9FormatSøknad {
        val ytelse = Ungdomsytelse()
            .medSøknadType(UngSøknadstype.RAPPORTERING_SØKNAD)
            .medInntekter(OppgittInntekt(setOf(oppgittInntektForPeriode.somUngOppgittInntektForPeriode())))

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
