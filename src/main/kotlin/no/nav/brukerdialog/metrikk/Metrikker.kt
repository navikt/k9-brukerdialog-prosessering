package no.nav.brukerdialog.metrikk

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import no.nav.brukerdialog.ytelse.Ytelse
import org.springframework.stereotype.Service

@Service
class MetrikkService(meterRegistry: MeterRegistry) {

    private val antallMottatteSøknaderPerYtelseCounter = Counter.builder("antall_mottatte_soknader_per_ytelse_counter")
        .description("Teller antall mottatte innsendinger per ytelse")
        .tag("ytelse", "ytelse")
        .register(meterRegistry)

    fun registrerMottattInnsending(ytelse: Ytelse) = antallMottatteSøknaderPerYtelseCounter.increment()
}
