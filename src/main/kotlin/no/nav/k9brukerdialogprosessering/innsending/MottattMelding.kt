package no.nav.k9brukerdialogprosessering.innsending

import no.nav.k9.søknad.Søknad
import no.nav.k9brukerdialogprosessering.common.Ytelse
import no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.domene.felles.Navn
import java.time.ZonedDateTime

interface MottattMelding {
    fun ytelse(): Ytelse
    fun søkerFødselsnummer(): String

    // søknad.k9FormatSøknad
    fun k9FormatSøknad(): Søknad

    // søknad.vedleggId
    fun vedleggId(): List<String>

    // søknad.fødselsattestVedleggId
    fun fødselsattestVedleggId(): List<String>
    fun mapTilPreprosessert(dokumentId: List<List<String>>): Preprosessert
    fun pdfData(): Map<String, Any>

    fun mapTilPreprosesseringsData(): PreprosesseringsData
}

interface Preprosessert {
    fun ytelse(): Ytelse
    fun mottattDato(): ZonedDateTime
    fun søkerNavn(): Navn
    fun søkerFødselsnummer(): String
    fun k9FormatSøknad(): Søknad
    fun dokumenter(): List<List<String>>

}

