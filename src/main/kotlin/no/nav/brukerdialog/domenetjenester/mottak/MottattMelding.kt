package no.nav.brukerdialog.domenetjenester.mottak

import no.nav.k9.søknad.Innsending
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.dittnavvarsel.K9Beskjed
import no.nav.brukerdialog.ytelse.fellesdomene.Navn
import no.nav.brukerdialog.pdf.PdfData
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

    fun tilJournaførigsRequest(): JournalføringsService.JournalføringsRequest
    fun tilK9DittnavVarsel(metadata: MetaInfo): K9Beskjed?

}

