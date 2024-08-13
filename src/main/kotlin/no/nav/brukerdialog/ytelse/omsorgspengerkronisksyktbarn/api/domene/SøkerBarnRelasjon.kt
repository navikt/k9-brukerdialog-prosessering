package no.nav.brukerdialog.ytelse.omsorgspengerkronisksyktbarn.api.domene

import com.fasterxml.jackson.annotation.JsonAlias

enum class SøkerBarnRelasjon() {
    @JsonAlias("mor") MOR,
    @JsonAlias("far") FAR,
    @JsonAlias("fosterforelder") FOSTERFORELDER,
    @JsonAlias("adoptivforelder") ADOPTIVFORELDER
}
