package no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9.søknad.Søknad
import no.nav.k9brukerdialogprosessering.common.Ytelse
import no.nav.k9brukerdialogprosessering.innsending.MottattMelding
import no.nav.k9brukerdialogprosessering.innsending.PreprosesseringsData
import no.nav.k9brukerdialogprosessering.meldinger.felles.domene.Søker
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.PilsSøknadPdfData
import no.nav.k9brukerdialogprosessering.pdf.PdfData
import java.time.LocalDate
import java.time.ZonedDateTime

data class PilsSøknadMottatt(
    val søknadId: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    val mottatt: ZonedDateTime,
    val språk: String? = "nb",
    val søker: Søker,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val skalJobbeOgPleieSammeDag: Boolean,
    val dagerMedPleie: List<LocalDate>,
    val pleierDuDenSykeHjemme: Boolean,
    val vedleggId: List<String> = listOf(),
    val opplastetIdVedleggId: List<String> = listOf(),
    val pleietrengende: Pleietrengende,
    val arbeidsgivere: List<Arbeidsgiver>,
    val medlemskap: Medlemskap,
    val utenlandsoppholdIPerioden: UtenlandsoppholdIPerioden,
    val frilans: Frilans?,
    val selvstendigNæringsdrivende: SelvstendigNæringsdrivende?,
    val opptjeningIUtlandet: List<OpptjeningIUtlandet>,
    val utenlandskNæring: List<UtenlandskNæring>,
    val harVærtEllerErVernepliktig: Boolean? = null,
    val k9Format: Søknad,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val flereSokere: FlereSokereSvar? = null
): MottattMelding {
    override fun ytelse(): Ytelse = Ytelse.PLEIEPENGER_LIVETS_SLUTTFASE

    override fun søkerFødselsnummer(): String = søker.fødselsnummer

    override fun k9FormatSøknad(): Søknad = k9Format

    override fun vedleggId(): List<String> = vedleggId

    override fun fødselsattestVedleggId(): List<String> = opplastetIdVedleggId

    override fun mapTilPreprosessert(dokumentId: List<List<String>>) = PilsPreprosessertSøknad(
        pilsSøknadMottatt = this,
        dokumentId = dokumentId
    )

    override fun pdfData(): PdfData = PilsSøknadPdfData(this)

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

fun String.capitalizeName(): String = split(" ").joinToString(" ") { s ->
    s.lowercase()
        .replaceFirstChar {
            it.titlecase()
        }
}
