package no.nav.brukerdialog.api.innsending

import no.nav.k9.ettersendelse.Ettersendelse
import no.nav.k9.søknad.SøknadValidator
import no.nav.brukerdialog.ytelse.Ytelse
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.oppslag.soker.Søker
import java.net.URL
import no.nav.k9.søknad.felles.type.Periode as K9Periode

interface Innsending {
    fun ytelse(): Ytelse
    fun søknadId(): String
    fun inneholderVedlegg(): Boolean = vedlegg().isNotEmpty()
    fun vedlegg(): List<URL>
    fun somK9Format(søker: Søker, metadata: MetaInfo): no.nav.k9.søknad.Innsending? = null

    fun valider(): List<String>
    fun somKomplettSøknad(
        søker: Søker,
        k9Format: no.nav.k9.søknad.Innsending? = null,
        titler: List<String> = listOf()
    ): KomplettInnsending

    fun søknadValidator(): SøknadValidator<no.nav.k9.søknad.Søknad>? = null
    fun ettersendelseValidator(): SøknadValidator<Ettersendelse>? = null
    fun gyldigeEndringsPerioder(): List<K9Periode>? = null
}
