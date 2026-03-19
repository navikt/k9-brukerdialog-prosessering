package no.nav.brukerdialog.ytelse.aktivitetspenger.utils

import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.soknad.*
import no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.soknad.domene.AktivitetspengersøknadMottatt
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.k9.søknad.felles.Kildesystem
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.personopplysninger.Bosteder
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.aktivitetspenger.v1.Aktivitetspenger
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as k9FormatSøknad
import no.nav.k9.søknad.felles.personopplysninger.Søker as K9Søker

object AktivitetspengersøknadUtils {

    fun gyldigSøknad(
        søkerFødselsnummer: String = "02119970078",
        søknadId: String = UUID.randomUUID().toString(),
        mottatt: ZonedDateTime = ZonedDateTime.of(2018, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC")),
    ): AktivitetspengersøknadMottatt {
        val startdato = LocalDate.parse("2022-01-01")

        val forutgåendeMedlemskap = ForutgåendeMedlemskap(
            true,
            listOf(
                Bosted(
                    fraOgMed = LocalDate.of(2023, 1, 2),
                    tilOgMed = LocalDate.of(2023, 1, 3),
                    landkode = "US",
                    landnavn = "USA"
                )
            )
        )
        return AktivitetspengersøknadMottatt(
            språk = "nb",
            søknadId = søknadId,
            forutgåendeMedlemskap = forutgåendeMedlemskap,
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
            barn = listOf(
                Barn(
                    navn = "Ola Nordmann"
                )
            ),
            barnErRiktig = true,
            kontonummerInfo = KontonummerInfo(
                harKontonummer = HarKontonummer.JA,
                kontonummerFraRegister = "12345678901",
                kontonummerErRiktig = true,
            ),
            k9Format = gyldigK9Format(søknadId, mottatt, startdato, forutgåendeMedlemskap.tilK9Bosteder()),
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true
        )
    }

    fun gyldigK9Format(
        søknadId: String = UUID.randomUUID().toString(),
        mottatt: ZonedDateTime,
        fraOgMed: LocalDate,
        bosteder: Bosteder,
    ): k9FormatSøknad {
        val ytelse = Aktivitetspenger()
            .medSøknadsperiode(Periode(fraOgMed, fraOgMed.plusMonths(12)))
            .medForutgåendeMedlemskap(bosteder)
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
