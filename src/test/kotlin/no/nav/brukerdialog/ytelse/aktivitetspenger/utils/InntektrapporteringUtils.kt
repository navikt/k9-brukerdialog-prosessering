package no.nav.brukerdialog.ytelse.aktivitetspenger.utils

import no.nav.brukerdialog.config.JacksonConfiguration
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.inntektsrapportering.AktivitetspengerInntektsrapportering
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.inntektsrapportering.OppgittInntekt
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.inntektsrapportering.OppgittInntektForPeriode
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.inntektsrapportering.UngPeriode
import no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.inntektsrapportering.domene.AktivitetspengerInntektsrapporteringMottatt
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.k9.søknad.felles.Kildesystem
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.aktivitetspenger.v1.Aktivitetspenger
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as k9FormatSøknad
import no.nav.k9.søknad.felles.personopplysninger.Søker as K9Søker
import no.nav.k9.søknad.ytelse.ung.v1.inntekt.OppgittInntekt as UngOppgittInntekt

object InntektrapporteringUtils {
    internal val defaultInntektsrapportering = AktivitetspengerInntektsrapportering(
        oppgaveReferanse = "4e62f8de-1ff6-40e9-bdcd-10485c789094",
        mottatt = ZonedDateTime.parse("2025-01-01T03:04:05Z", JacksonConfiguration.zonedDateTimeFormatter),
        oppgittInntekt = OppgittInntekt(arbeidstakerOgFrilansInntekt = 3000),
    )

    fun gyldigInntektsrapportering(
        søkerFødselsnummer: String = "23500180528",
        søknadId: String = UUID.randomUUID().toString(),
        mottatt: ZonedDateTime = ZonedDateTime.of(2018, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC")),
        oppgittInntektForPeriode: OppgittInntektForPeriode = OppgittInntektForPeriode(
            arbeidstakerOgFrilansInntekt = 6000,
            periodeForInntekt = UngPeriode(
                fraOgMed = LocalDate.parse("2025-01-01"),
                tilOgMed = LocalDate.parse("2025-01-31")
            )
        ),
    ): AktivitetspengerInntektsrapporteringMottatt {

        return AktivitetspengerInntektsrapporteringMottatt(
            oppgaveReferanse = søknadId,
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
            k9Format = gyldigK9Format(søknadId, mottatt, oppgittInntektForPeriode)
        )
    }

    fun gyldigK9Format(
        søknadId: String = UUID.randomUUID().toString(),
        mottatt: ZonedDateTime,
        oppgittInntektForPeriode: OppgittInntektForPeriode,
    ): k9FormatSøknad {
        val ytelse = Aktivitetspenger()
            .medSøknadsperiode(oppgittInntektForPeriode.periodeForInntekt.somUngPeriode())
            .medInntekter(UngOppgittInntekt(setOf(oppgittInntektForPeriode.somUngOppgittInntektForPeriode())))

        val søknad = k9FormatSøknad(
            SøknadId(søknadId),
            Versjon("1.0.0"),
            mottatt,
            K9Søker(NorskIdentitetsnummer.of("23500180528")),
            ytelse

        ).medKildesystem(Kildesystem.SØKNADSDIALOG)
        return søknad
    }
}
