package no.nav.brukerdialog.ytelse.ungdomsytelse.utils

import no.nav.brukerdialog.config.JacksonConfiguration
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.soknad.Ungdomsytelsesøknad
import no.nav.k9.søknad.ytelse.ung.v1.UngSøknadstype
import java.time.LocalDate
import java.time.ZonedDateTime

object SøknadUtils {
    internal val defaultSøknad = Ungdomsytelsesøknad(
        søknadId = "4e62f8de-1ff6-40e9-bdcd-10485c789094",
        mottatt = ZonedDateTime.parse("2022-01-02T03:04:05Z", JacksonConfiguration.zonedDateTimeFormatter),
        språk = "nb",
        søkerNorskIdent = "12345678910",
        startdato = LocalDate.parse("2021-01-01"),
        harForståttRettigheterOgPlikter = true,
        harBekreftetOpplysninger = true
    )
}
