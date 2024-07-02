package no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.endringsmelding

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarn
import no.nav.k9brukerdialogapi.ENDRINGSMELDING_URL
import no.nav.k9brukerdialogapi.INNSENDING_URL
import no.nav.k9brukerdialogapi.PLEIEPENGER_SYKT_BARN_URL
import no.nav.k9brukerdialogapi.general.formaterStatuslogging
import no.nav.k9brukerdialogapi.general.getCallId
import no.nav.k9brukerdialogapi.innsyn.InnsynService
import no.nav.k9brukerdialogapi.kafka.getMetadata
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.endringsmelding.domene.Endringsmelding
import no.nav.k9brukerdialogapi.ytelse.ytelseFraHeader
import no.nav.k9brukerdialogprosessering.api.innsending.InnsendingCache
import no.nav.k9brukerdialogprosessering.api.innsending.InnsendingService
import no.nav.k9brukerdialogprosessering.api.ytelse.registrerMottattSøknad
import no.nav.k9brukerdialogprosessering.oppslag.barn.BarnOppslag
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.endringsmelding.EndringsmeldingApisKt")

fun Route.endringsmeldingApis(
    innsendingService: InnsendingService,
    innsendingCache: InnsendingCache,
    innsynService: InnsynService,
    idTokenProvider: IdTokenProvider,
) {

    route(PLEIEPENGER_SYKT_BARN_URL + ENDRINGSMELDING_URL) {
        post(INNSENDING_URL) {
            val endringsmelding = call.receive<Endringsmelding>()
            val callId = call.getCallId()
            val idToken = idTokenProvider.getIdToken(call)
            val metadata = call.getMetadata()
            val cacheKey = "${idToken.getNorskIdentifikasjonsnummer()}_${endringsmelding.ytelse()}"
            val ytelseFraHeader = call.ytelseFraHeader()

            logger.info(formaterStatuslogging(endringsmelding.ytelse(), endringsmelding.søknadId, "mottatt."))

            val søknadsopplysninger = innsynService.hentSøknadsopplysningerForBarn(idToken, callId, endringsmelding)

            val ytelse = søknadsopplysninger.søknad.getYtelse<PleiepengerSyktBarn>()
            endringsmelding.gyldigeEndringsPerioder = ytelse.søknadsperiodeList
            endringsmelding.pleietrengendeNavn = søknadsopplysninger.barn.navn()

            innsendingService.registrer(endringsmelding, callId, idToken, metadata, ytelseFraHeader)
            innsendingCache.put(cacheKey)
            registrerMottattSøknad(endringsmelding.ytelse())
            call.respond(HttpStatusCode.Accepted)
        }
    }
}

private fun barnFraEndringsmelding(
    barnListe: List<BarnOppslag>,
    endringsmelding: Endringsmelding,
) = (barnListe.firstOrNull {
    it.identitetsnummer == endringsmelding.ytelse.barn.personIdent.verdi
} ?: throw IllegalStateException("Oppgitt barn er ikke en barn som kan sendes endringsmelding på"))
