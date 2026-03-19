package no.nav.brukerdialog.ytelse.aktivitetspenger.utils

import no.nav.brukerdialog.config.JacksonConfiguration
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.soknad.*
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

object SøknadUtils {
    internal val defaultSøknad = Aktivitetspengersøknad(
        søknadId = UUID.randomUUID().toString(),
        mottatt = ZonedDateTime.parse("2022-01-02T03:04:05Z", JacksonConfiguration.zonedDateTimeFormatter),
        språk = "nb",
        søkerNorskIdent = "12345678910",
        startdato = LocalDate.parse("2021-01-01"),
        barnErRiktig = true,
        kontonummerInfo = KontonummerInfo(
            harKontonummer = HarKontonummer.JA,
            kontonummerFraRegister = "12345678901",
            kontonummerErRiktig = true,
        ),
        harForståttRettigheterOgPlikter = true,
        harBekreftetOpplysninger = true,
        forutgåendeBosteder = ForutgåendeBosteder(true,
            listOf(
                Bosted(
                    fraOgMed = LocalDate.of(2023, 1, 2),
                    tilOgMed = LocalDate.of(2023, 1, 3),
                    landkode = "USA",
                    landnavn = "USA"
                )
            )
        ),
    )
}
