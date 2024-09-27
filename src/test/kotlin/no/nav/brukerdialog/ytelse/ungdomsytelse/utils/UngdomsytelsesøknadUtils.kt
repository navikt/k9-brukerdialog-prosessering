package no.nav.brukerdialog.ytelse.ungdomsytelse.utils

import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.domene.UngdomsytelsesøknadMottatt
import no.nav.k9.søknad.felles.Kildesystem
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.SøknadId
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
        mottatt: ZonedDateTime = ZonedDateTime.of(2018, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC"))
    ) = UngdomsytelsesøknadMottatt(
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
        fraOgMed = LocalDate.parse("2022-01-01"),
        tilOgMed = LocalDate.parse("2022-02-01"),
        k9Format = gyldigK9Format(søknadId, mottatt),
        harBekreftetOpplysninger = true,
        harForståttRettigheterOgPlikter = true
    )

    fun gyldigK9Format(søknadId: String = UUID.randomUUID().toString(), mottatt: ZonedDateTime) = k9FormatSøknad(
        SøknadId(søknadId),
        Versjon("1.0.0"),
        mottatt,
        K9Søker(NorskIdentitetsnummer.of("02119970078")),
        Ungdomsytelse()
    ).medKildesystem(Kildesystem.SØKNADSDIALOG)
}
