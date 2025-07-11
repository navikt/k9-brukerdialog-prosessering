package no.nav.brukerdialog.ytelse.omsorgspengeraleneomsorg.api.domene

import no.nav.k9.søknad.Søknad
import no.nav.brukerdialog.domenetjenester.innsending.KomplettInnsending
import no.nav.brukerdialog.oppslag.soker.Søker
import java.time.ZonedDateTime

data class OmsorgsdagerAleneOmOmsorgenKomplettSøknad(
    private val søknadId: String,
    private val mottatt: ZonedDateTime,
    private val søker: Søker,
    private val språk: String,
    private val barn: Barn,
    private val k9Søknad: Søknad,
    private val harForståttRettigheterOgPlikter: Boolean,
    private val harBekreftetOpplysninger: Boolean
): KomplettInnsending {
    override fun innsendingId(): String = søknadId
}
