package no.nav.k9brukerdialogprosessering.api.ytelse

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Service

@Service
class MetrikkService(meterRegistry: MeterRegistry) {

    private val antallMottatteSøknaderPerYtelseCounter = Counter.builder("antall_mottatte_soknader_per_ytelse_counter")
        .description("Teller antall mottatte søknader per ytelse")
        .tag("ytelse", "ytelse")
        .register(meterRegistry)

    fun registrerMottattSøknad(ytelse: Ytelse) = antallMottatteSøknaderPerYtelseCounter.increment()
}
