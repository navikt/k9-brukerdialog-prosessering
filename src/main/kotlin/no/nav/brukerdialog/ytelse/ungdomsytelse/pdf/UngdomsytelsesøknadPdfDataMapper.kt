package no.nav.brukerdialog.ytelse.ungdomsytelse.pdf

import no.nav.brukerdialog.common.Constants.DATE_FORMATTER
import no.nav.brukerdialog.common.Constants.DATE_TIME_FORMATTER
import no.nav.brukerdialog.common.Constants.OSLO_ZONE_ID
import no.nav.brukerdialog.common.FeltMap
import no.nav.brukerdialog.common.PdfConfig
import no.nav.brukerdialog.common.VerdilisteElement
import no.nav.brukerdialog.utils.DateUtils.somNorskDag
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.domene.UngdomsytelsesøknadMottatt

object UngdomsytelsesøknadPdfDataMapper {
    fun mapUngdomsytelsesøknadPdfData(
        ytelseTittel: String,
        ungdomsytelse: UngdomsytelsesøknadMottatt,
    ): FeltMap {
        val innsendingsdetaljer =
            mapInnsendingsdetaljer(
                ungdomsytelse.mottatt.withZoneSameInstant(OSLO_ZONE_ID).somNorskDag() + DATE_TIME_FORMATTER.format(ungdomsytelse.mottatt),
                ungdomsytelse.språk,
            )
        val søker =
            mapSøker(
                ungdomsytelse.søker.formatertNavn(),
                ungdomsytelse.søker.fødselsnummer,
            )
        val periode =
            mapPeriode(
                DATE_FORMATTER.format(ungdomsytelse.fraOgMed),
                DATE_FORMATTER.format(ungdomsytelse.tilOgMed),
                ungdomsytelse.inntekt.toString(),
            )
        val samtykke =
            mapSamtykke(
                ungdomsytelse.harForståttRettigheterOgPlikter.toString(),
                ungdomsytelse.harBekreftetOpplysninger.toString(),
            )

        return FeltMap(
            label = ytelseTittel,
            verdiliste = listOf(innsendingsdetaljer, søker, periode, samtykke),
            pdfConfig = PdfConfig(true, "nb"),
        )
    }

    fun mapInnsendingsdetaljer(
        tidspunkt: String,
        språk: String? = "nb",
    ): VerdilisteElement =
        VerdilisteElement(
            label = "Innsendingsdetaljer",
            verdiliste =
                listOf(
                    VerdilisteElement(
                        label = "Sendt til Nav",
                        verdi = tidspunkt,
                    ),
                    VerdilisteElement(label = "Språk", verdi = språk),
                ),
        )

    fun mapSøker(
        navnSøker: String,
        fødselsnummerSøker: String,
    ) = VerdilisteElement(
        label = "Søker",
        verdiliste =
            listOf(
                VerdilisteElement(label = "Navn", verdi = navnSøker),
                VerdilisteElement(label = "Fødselsnummer", verdi = fødselsnummerSøker),
            ),
    )

    fun mapPeriode(
        fraOgMed: String,
        tilOgMed: String,
        rapportertInntektIPerioden: String,
    ) = VerdilisteElement(
        label = "Periode med deltakelse i ungdomsprogrammet",
        verdiliste =
            listOf(
                VerdilisteElement(label = "Fra og med", verdi = fraOgMed),
                VerdilisteElement(label = "Til og med", verdi = tilOgMed),
                VerdilisteElement(label = "Rapportert inntekt i perioden:", verdi = rapportertInntektIPerioden),
            ),
    )

    fun mapSamtykke(
        harForståttRettigheterOgPlikter: String,
        harBekreftetOpplysninger: String,
    ) = VerdilisteElement(
        label = "Samtykke fra deg",
        verdiliste =
            listOf(
                VerdilisteElement(
                    label = "Har du forstått dine rettigheter og plikter?",
                    verdi = harForståttRettigheterOgPlikter,
                ),
                VerdilisteElement(
                    label = "Har du bekreftet at opplysninger du har gitt er riktige?",
                    verdi = harBekreftetOpplysninger,
                ),
            ),
    )
}
