package no.nav.brukerdialog.mellomlagring.soknad

import no.nav.brukerdialog.integrasjon.k9brukerdialogcache.K9BrukerdialogCacheService
import no.nav.brukerdialog.ytelse.Ytelse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.ZoneOffset
import java.time.ZonedDateTime

@Service
class MellomlagringService(
    @Value("\${no.nav.mellomlagring.søknad_tid_timer}") private val mellomlagretTidTimer: String,
    private val k9BrukerdialogCacheService: K9BrukerdialogCacheService
) {
    private fun genererNøkkelPrefix(ytelse: Ytelse) = "mellomlagring_$ytelse"

    suspend fun hentMellomlagring(ytelse: Ytelse) = k9BrukerdialogCacheService.hentMellomlagretSøknad(
        nøkkelPrefiks = genererNøkkelPrefix(ytelse)
    )?.verdi

    suspend fun settMellomlagring(
        ytelse: Ytelse,
        mellomlagring: String,
    ) = k9BrukerdialogCacheService.mellomlagreSøknad(
        cacheRequest = CacheRequest(
            nøkkelPrefiks = genererNøkkelPrefix(ytelse),
            verdi = mellomlagring,
            ytelse = ytelse,
            utløpsdato = ZonedDateTime.now(ZoneOffset.UTC).plusHours(mellomlagretTidTimer.toLong()),
            opprettet = ZonedDateTime.now(ZoneOffset.UTC),
            endret = null
        )
    )

    suspend fun slettMellomlagring(ytelse: Ytelse): Boolean {
        return k9BrukerdialogCacheService.slettMellomlagretSøknad(genererNøkkelPrefix(ytelse))
    }

    suspend fun oppdaterMellomlagring(
        ytelse: Ytelse,
        mellomlagring: String,
    ): CacheResponse {
        val eksisterendeMellomlagring = k9BrukerdialogCacheService.hentMellomlagretSøknad(
            nøkkelPrefiks = genererNøkkelPrefix(ytelse)
        )

        return if(eksisterendeMellomlagring != null){
            k9BrukerdialogCacheService.oppdaterMellomlagretSøknad(
                cacheRequest = CacheRequest(
                    nøkkelPrefiks = genererNøkkelPrefix(ytelse),
                    verdi = mellomlagring,
                    ytelse = ytelse,
                    utløpsdato = eksisterendeMellomlagring.utløpsdato,
                    opprettet = eksisterendeMellomlagring.opprettet,
                    endret = ZonedDateTime.now()

                )

            )
        } else settMellomlagring(ytelse, mellomlagring)
    }
}
