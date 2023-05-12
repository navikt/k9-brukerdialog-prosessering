package no.nav.k9brukerdialogprosessering.meldinger.endringsmelding.utils

import no.nav.k9brukerdialogprosessering.meldinger.endringsmelding.domene.PSBEndringsmeldingMottatt
import no.nav.k9brukerdialogprosessering.meldinger.felles.domene.Søker
import no.nav.k9brukerdialogprosessering.utils.K9FormatUtils.defaultK9FormatPSB
import java.time.LocalDate
import java.time.ZonedDateTime

internal object EndringsmeldingUtils {
    private val start = LocalDate.parse("2020-01-01")
    private const val GYLDIG_ORGNR = "917755736"

    internal fun defaultEndringsmelding(søknadsId: String, mottatt: ZonedDateTime) = PSBEndringsmeldingMottatt(
        søker = Søker(
            aktørId = "123456",
            fødselsnummer = "02119970078",
            etternavn = "Nordmann",
            mellomnavn = "Mellomnavn",
            fornavn = "Ola",
            fødselsdato = LocalDate.parse("1999-11-02"),
        ),
        pleietrengendeNavn = "Barn Barnesen",
        harBekreftetOpplysninger = true,
        harForståttRettigheterOgPlikter = true,
        k9Format = defaultK9FormatPSB(søknadsId, mottatt)
    )
}
