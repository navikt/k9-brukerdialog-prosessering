package no.nav.brukerdialog.ytelse.opplæringspenger.api.domene

import jakarta.validation.constraints.AssertTrue
import org.jetbrains.annotations.NotNull

data class EttersendingAvVedlegg(
    @field:NotNull val skalEttersendeVedlegg: Boolean,
    val vedleggSomSkalEttersendes: List<VedleggType>?
) {

    @AssertTrue(message = "Vedlegg som skal ettersendes kan ikke være tomt når skalEttersendeVedlegg er true.")
    fun isValid(): Boolean {
        if (skalEttersendeVedlegg) {
            return !vedleggSomSkalEttersendes.isNullOrEmpty()
        }
        return true
    }

}

 enum class VedleggType(val beskrivelse: String) {
     LEGEERKLÆRING("Signert legeerklæring"),
     KURSINFORMASJON("Informasjon om kurs"),
     ANNET("Annet"),
}
