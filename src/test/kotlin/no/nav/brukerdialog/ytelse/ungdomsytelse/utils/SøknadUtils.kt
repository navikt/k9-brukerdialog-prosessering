package no.nav.brukerdialog.ytelse.ungdomsytelse.utils

import no.nav.brukerdialog.config.JacksonConfiguration
import no.nav.brukerdialog.oppslag.barn.BarnOppslag
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.BekreftelseSvar
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.UngdomsytelseOppgaveDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.UngdomsytelseOppgaveUttalelseDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.UngdomsytelseOppgavebekreftelse
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.soknad.Barn
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.soknad.Ungdomsytelsesøknad
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

object SøknadUtils {
    internal val defaultSøknad = Ungdomsytelsesøknad(
        søknadId = "4e62f8de-1ff6-40e9-bdcd-10485c789094",
        mottatt = ZonedDateTime.parse("2022-01-02T03:04:05Z", JacksonConfiguration.zonedDateTimeFormatter),
        språk = "nb",
        søkerNorskIdent = "12345678910",
        startdato = LocalDate.parse("2021-01-01"),
        barn = listOf(
            Barn(navn = "Ola Nordmann")
        ),
        barnErRiktig = true,
        kontonummerFraRegister = "12345678901",
        kontonummerErRiktig = true,
        harForståttRettigheterOgPlikter = true,
        harBekreftetOpplysninger = true
    )

    internal val defaultOppgavebekreftelse = UngdomsytelseOppgavebekreftelse(
        oppgave = UngdomsytelseOppgaveDTO(
            oppgaveReferanse = UUID.randomUUID().toString(),
            uttalelse = UngdomsytelseOppgaveUttalelseDTO(
                bekreftelseSvar = BekreftelseSvar.GODTAR,
            )
        )
    )
}
