package no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene

import no.nav.k9brukerdialogprosessering.utils.krever

class Bekreftelser(
    val harBekreftetOpplysninger: Boolean? = null,
    val harForståttRettigheterOgPlikter: Boolean? = null
){
    internal fun valider(felt: String) = mutableListOf<String>().apply {
        krever(harBekreftetOpplysninger, "$felt.harBekreftetOpplysninger må være true")
        krever(harForståttRettigheterOgPlikter, "$felt.harForståttRettigheterOgPlikter må være true")
    }
}
