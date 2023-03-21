package no.nav.k9brukerdialogprosessering.innsending

import no.nav.k9.søknad.Innsending
import no.nav.k9brukerdialogprosessering.common.Ytelse
import no.nav.k9brukerdialogprosessering.journalforing.JournalføringsRequest
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Navn
import no.nav.k9brukerdialogprosessering.pdf.PdfData
import java.time.ZonedDateTime

interface MottattMelding {
    fun ytelse(): Ytelse
    fun søkerFødselsnummer(): String

    fun k9FormatSøknad(): Innsending

    fun vedleggId(): List<String>

    fun fødselsattestVedleggId(): List<String>
    fun mapTilPreprosessert(dokumentId: List<List<String>>): Preprosessert
    fun pdfData(): PdfData

    fun mapTilPreprosesseringsData(): PreprosesseringsData
}

interface Preprosessert {
    fun ytelse(): Ytelse
    fun mottattDato(): ZonedDateTime
    fun søkerNavn(): Navn
    fun søkerFødselsnummer(): String
    fun k9FormatSøknad(): Innsending
    fun dokumenter(): List<List<String>>

    fun tilJournaførigsRequest(): JournalføringsRequest

}

