package no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.INNSENDING_URL
import no.nav.k9brukerdialogapi.PLEIEPENGER_LIVETS_SLUTTFASE_URL
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.general.getCallId
import no.nav.k9brukerdialogapi.kafka.getMetadata
import no.nav.k9brukerdialogapi.ytelse.pleiepengerlivetssluttfase.domene.PilsSøknad
import no.nav.k9brukerdialogapi.ytelse.registrerMottattSøknad
import no.nav.k9brukerdialogapi.ytelse.ytelseFraHeader
import no.nav.k9brukerdialogprosessering.api.innsending.InnsendingCache
import no.nav.k9brukerdialogprosessering.api.innsending.InnsendingService
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Route.pleiepengerLivetsSluttfaseApi(
    innsendingService: InnsendingService,
    innsendingCache: InnsendingCache,
    idTokenProvider: IdTokenProvider,
){
    val logger: Logger = LoggerFactory.getLogger("ytelse.pleiepengerlivetssluttfase.pleiepengerLivetsSluttfaseApi.kt")

    route(PLEIEPENGER_LIVETS_SLUTTFASE_URL){
        post(INNSENDING_URL){
            val pilsSøknad = call.receive<PilsSøknad>()
            val idToken = idTokenProvider.getIdToken(call)
            val cacheKey = "${idToken.getNorskIdentifikasjonsnummer()}_${pilsSøknad.ytelse()}"
            val ytelse = call.ytelseFraHeader()

            logger.info(formaterStatuslogging(pilsSøknad.ytelse(), pilsSøknad.søknadId, "mottatt."))
            innsendingCache.put(cacheKey)
            innsendingService.registrer(pilsSøknad, call.getCallId(), idToken, call.getMetadata(), ytelse)
            registrerMottattSøknad(pilsSøknad.ytelse())
            call.respond(HttpStatusCode.Accepted)
        }
    }
}
