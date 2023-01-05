package no.nav.k9brukerdialogprosessering.pleiepengersyktbarn

import no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.domene.PSBMottattSøknad
import no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.domene.PSBPreprosessertSøknad
import org.springframework.stereotype.Service

@Service
class PSBPreprosesseringsService {
    companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(PSBPreprosesseringsService::class.java)
    }

    fun preprosesser(søknad: PSBMottattSøknad): PSBPreprosessertSøknad {
        logger.info("Preprosesserer søknad: $søknad")

        return PSBPreprosessertSøknad(
            melding = søknad,
            dokumentId = listOf(listOf("123456789", "987654321"))
        )
    }
}
