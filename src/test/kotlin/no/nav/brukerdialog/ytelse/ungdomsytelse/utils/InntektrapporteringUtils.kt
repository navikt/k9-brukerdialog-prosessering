package no.nav.brukerdialog.ytelse.ungdomsytelse.utils

import no.nav.brukerdialog.config.JacksonConfiguration
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.OppgittInntektForPeriode
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.UngPeriode
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.inntektsrapportering.UngdomsytelseInntektsrapportering
import java.time.LocalDate
import java.time.ZonedDateTime

object InntektrapporteringUtils {
    internal val defaultInntektsrapportering = UngdomsytelseInntektsrapportering(
        søknadId = "4e62f8de-1ff6-40e9-bdcd-10485c789094",
        mottatt = ZonedDateTime.parse("2025-01-01T03:04:05Z", JacksonConfiguration.zonedDateTimeFormatter),
        oppgittInntektForPeriode = OppgittInntektForPeriode(
            arbeidstakerOgFrilansInntekt = 3000.0,
            næringsinntekt = 0.0,
            inntektFraYtelse = 0.0,
            periodeForInntekt = UngPeriode(
                fraOgMed = LocalDate.parse("2025-01-01"),
                tilOgMed = LocalDate.parse("2025-01-31")
            )
        ),
        harBekreftetInntekt = true,
    )
}
