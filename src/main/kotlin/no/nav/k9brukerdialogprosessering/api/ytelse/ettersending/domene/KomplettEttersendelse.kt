package no.nav.k9brukerdialogprosessering.api.ytelse.ettersending.domene

import no.nav.k9.ettersendelse.Ettersendelse
import no.nav.k9.ettersendelse.EttersendelseType
import no.nav.k9brukerdialogprosessering.api.innsending.KomplettInnsending
import no.nav.k9brukerdialogprosessering.oppslag.soker.Søker
import java.time.ZonedDateTime

class KomplettEttersendelse(
    private val søknadId: String,
    private val søker: Søker,
    private val språk: String,
    private val mottatt: ZonedDateTime,
    private val vedleggId: List<String>,
    private val harForståttRettigheterOgPlikter: Boolean,
    private val harBekreftetOpplysninger: Boolean,
    private val beskrivelse: String?,
    private val søknadstype: Søknadstype,
    private val titler: List<String>,
    private val k9Format: Ettersendelse,
    private val ettersendelsesType: EttersendelseType,
    private val pleietrengende: Pleietrengende? = null
): KomplettInnsending {

    override fun equals(other: Any?) = this === other || (other is KomplettEttersendelse && this.equals(other))

    private fun equals(other: KomplettEttersendelse) = this.søknadId == other.søknadId
            && this.k9Format.søknadId == other.k9Format.søknadId
            && this.titler == other.titler
}
