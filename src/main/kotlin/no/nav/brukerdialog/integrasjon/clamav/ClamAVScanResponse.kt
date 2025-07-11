package no.nav.brukerdialog.integrasjon.clamav

import com.fasterxml.jackson.annotation.JsonAlias

data class ClamAVScanResponse(
    @JsonAlias("Filename") val fileName: String,
    @JsonAlias("Result") val result: ScanResultat,
)
