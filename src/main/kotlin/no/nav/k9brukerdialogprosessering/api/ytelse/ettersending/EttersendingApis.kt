package no.nav.k9brukerdialogapi.ytelse.ettersending

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.ETTERSENDING_URL
import no.nav.k9brukerdialogapi.INNSENDING_URL
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.general.getCallId
import no.nav.k9brukerdialogapi.kafka.getMetadata
import no.nav.k9brukerdialogapi.ytelse.ettersending.domene.Ettersendelse
import no.nav.k9brukerdialogapi.ytelse.registrerMottattSøknad
import no.nav.k9brukerdialogapi.ytelse.ytelseFraHeader
import no.nav.k9brukerdialogprosessering.api.innsending.InnsendingCache
import no.nav.k9brukerdialogprosessering.api.innsending.InnsendingService
import no.nav.k9brukerdialogprosessering.oppslag.barn.BarnService
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("ytelse.ettersending.ettersendingApis.kt")

fun Route.ettersendingApis(
    innsendingService: InnsendingService,
    barnService: BarnService,
    innsendingCache: InnsendingCache,
    idTokenProvider: IdTokenProvider,
){
    route(ETTERSENDING_URL){
        post(INNSENDING_URL){
            val ettersendelse =  call.receive<Ettersendelse>()
            val idToken = idTokenProvider.getIdToken(call)
            val cacheKey = "${idToken.getNorskIdentifikasjonsnummer()}_${ettersendelse.ytelse()}"
            val callId = call.getCallId()
            val ytelse = call.ytelseFraHeader()

            logger.info(formaterStatuslogging(ettersendelse.ytelse(), ettersendelse.søknadId, "mottatt."))
            logger.info("Ettersending for ytelse ${ettersendelse.søknadstype}")

            ettersendelse.leggTilIdentifikatorPåBarnHvisMangler(barnService.hentBarn(idToken, callId, ytelse))

            innsendingCache.put(cacheKey)
            innsendingService.registrer(ettersendelse, callId, idToken, call.getMetadata(), ytelse)
            registrerMottattSøknad(ettersendelse.ytelse())
            call.respond(HttpStatusCode.Accepted)
        }
    }
}
