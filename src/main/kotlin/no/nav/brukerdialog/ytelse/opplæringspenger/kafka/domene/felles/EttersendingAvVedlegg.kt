package no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene.felles

data class EttersendingAvVedlegg(
    val skalEttersendeVedlegg: Boolean,
    val vedleggSomSkalEttersendes: List<VedleggType>?
)

 enum class VedleggType(val beskrivelse: String) {
     LEGEERKLÆRING("Signert legeerklæring"),
     KURSINFORMASJON("Informasjon om kurs"),
     ANNET("Annet"),
}
