package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9brukerdialogapi.INNSENDING_URL
import no.nav.k9brukerdialogapi.OMSORGSPENGER_UTVIDET_RETT_URL
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.general.getCallId
import no.nav.k9brukerdialogapi.kafka.getMetadata
import no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.domene.OmsorgspengerKroniskSyktBarnSøknad
import no.nav.k9brukerdialogapi.ytelse.ytelseFraHeader
import no.nav.k9brukerdialogprosessering.api.innsending.InnsendingCache
import no.nav.k9brukerdialogprosessering.api.innsending.InnsendingService
import no.nav.k9brukerdialogprosessering.api.ytelse.registrerMottattSøknad
import no.nav.k9brukerdialogprosessering.oppslag.barn.BarnService
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("ytelse.omsorgspengerutvidetrett.OmsorgspengerUtvidetRettApis.kt")

fun Route.omsorgspengerUtvidetRettApis(
    innsendingService: InnsendingService,
    barnService: BarnService,
    innsendingCache: InnsendingCache,
    idTokenProvider: IdTokenProvider
){
    route(OMSORGSPENGER_UTVIDET_RETT_URL){
        post(INNSENDING_URL){
            val søknad =  call.receive<OmsorgspengerKroniskSyktBarnSøknad>()
            val callId = call.getCallId()
            val idToken = idTokenProvider.getIdToken(call)
            val metadata = call.getMetadata()
            val cacheKey = "${idToken.getNorskIdentifikasjonsnummer()}_${søknad.ytelse()}"
            val ytelse = call.ytelseFraHeader()

            logger.info(formaterStatuslogging(søknad.ytelse(), søknad.søknadId, "mottatt."))
            søknad.leggTilIdentifikatorPåBarnHvisMangler(barnService.hentBarn(idToken, callId, ytelse))

            innsendingCache.put(cacheKey)
            innsendingService.registrer(søknad, callId, idToken, metadata, ytelse)
            registrerMottattSøknad(søknad.ytelse())
            call.respond(HttpStatusCode.Accepted)
        }
    }
}
