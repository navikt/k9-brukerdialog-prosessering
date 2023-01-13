package no.nav.k9brukerdialogprosessering.innsending

import no.nav.k9.søknad.Søknad
import org.springframework.stereotype.Service

@Service
class PreprosesseringsService {
    companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(PreprosesseringsService::class.java)
    }

    fun preprosesser(søknad: PreprosesseringsData): PreprosesseringsResultat {
        logger.info("Preprosesserer søknad: $søknad")

        // søknad.søker.fødselsnummer
        søknad.søkerFødselsnummer

        // søknad.k9FormatSøknad
        søknad.k9FormatSøknad

        // søknad.vedleggId
        søknad.vedleggId

        // søknad.fødselsattestVedleggId
        søknad.fødselsattestVedleggId

        return PreprosesseringsResultat(
            dokumenter = listOf(listOf("123456789", "987654321"))
        )
    }
}

data class PreprosesseringsData(
    val søkerFødselsnummer: String,
    val k9FormatSøknad: Søknad,
    val vedleggId: List<String>,
    val fødselsattestVedleggId: List<String>?,
    val pdfData: Map<String, Any>
)
data class PreprosesseringsResultat(
    val dokumenter: List<List<String>>
)
