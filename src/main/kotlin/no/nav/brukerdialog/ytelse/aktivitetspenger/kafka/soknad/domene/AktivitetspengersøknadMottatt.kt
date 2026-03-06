package no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.soknad.domene

import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.domenetjenester.mottak.MottattMelding
import no.nav.brukerdialog.domenetjenester.mottak.PreprosesseringsData
import no.nav.brukerdialog.pdf.PdfData
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.soknad.Barn
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.soknad.KontonummerInfo
import no.nav.brukerdialog.ytelse.aktivitetspenger.pdf.AktivitetspengersøknadPdfData
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.k9.søknad.Søknad
import java.time.LocalDate
import java.time.ZonedDateTime

data class AktivitetspengersøknadMottatt(
    val søknadId: String,
    val mottatt: ZonedDateTime,
    val språk: String? = "nb",
    val søker: Søker,
    val startdato: LocalDate? = null,
    val barn: List<Barn>,
    val barnErRiktig: Boolean,
    val kontonummerInfo: KontonummerInfo,
    val k9Format: Søknad,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
) : MottattMelding {
    override fun ytelse(): Ytelse = Ytelse.AKTIVITETSPENGER

    override fun søkerFødselsnummer(): String = søker.fødselsnummer

    override fun k9FormatSøknad(): Søknad = k9Format

    override fun vedleggId(): List<String> = listOf()

    override fun fødselsattestVedleggId(): List<String> = listOf()

    override fun mapTilPreprosessert(dokumentId: List<List<String>>) = AktivitetspengersøknadPreprosessertSøknad(
        aktivitetspengerSøknadMottatt = this,
        dokumentId = dokumentId
    )

    override fun pdfData(): PdfData = AktivitetspengersøknadPdfData(this)

    override fun mapTilPreprosesseringsData(): PreprosesseringsData = PreprosesseringsData(
        søkerFødselsnummer = søkerFødselsnummer(),
        k9FormatSøknad = k9FormatSøknad(),
        vedleggId = vedleggId(),
        fødselsattestVedleggId = fødselsattestVedleggId(),
        pdfData = pdfData(),
        pdfJournalføringsTittel = ytelse().tittel,
        jsonJournalføringsTittel = "${ytelse().tittel}(JSON)",
    )
}
