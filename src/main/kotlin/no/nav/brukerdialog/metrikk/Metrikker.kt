package no.nav.brukerdialog.metrikk

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import no.nav.brukerdialog.ytelse.Ytelse
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.tilgang.IngenTilgangÅrsak
import org.springframework.stereotype.Service

@Service
class MetrikkService(private val meterRegistry: MeterRegistry) {

    private val antallMottatteSøknaderPerYtelseCounter = Counter.builder("antall_mottatte_soknader_per_ytelse_counter")
        .description("Teller antall mottatte innsendinger per ytelse")
        .tag("ytelse", "ytelse")
        .register(meterRegistry)

    fun registrerMottattInnsending(ytelse: Ytelse) = antallMottatteSøknaderPerYtelseCounter.increment()

    /**
     * Teller én gang per vurdering, tagget med første årsak eller "tilgang".
     * Taggen er årsakskoden uten æ, ø og å, slik at den er enkel å bruke i spørringer.
     * Verdiene er faste, så antallet er begrenset og ingenting kan knyttes til en bruker.
     */
    fun registrerEndringsmeldingTilgangVurdering(førsteÅrsak: IngenTilgangÅrsak?) =
        Counter.builder("endringsmelding_tilgang_vurdering")
            .description("Teller vurderinger av tilgang til endringsmelding pleiepenger, per første årsak")
            .tag("aarsak", førsteÅrsak?.kode?.utenNorskeTegn() ?: "tilgang")
            .register(meterRegistry)
            .increment()

    private fun String.utenNorskeTegn(): String = this
        .replace("æ", "ae").replace("Æ", "Ae")
        .replace("ø", "o").replace("Ø", "O")
        .replace("å", "aa").replace("Å", "Aa")
}
