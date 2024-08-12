package no.nav.k9brukerdialogprosessering.api.ytelse.omsorgsdageraleneomsorg.domene

import no.nav.k9.søknad.Søknad
import no.nav.k9brukerdialogprosessering.api.innsending.KomplettInnsending
import no.nav.k9brukerdialogprosessering.oppslag.soker.Søker
import java.time.ZonedDateTime

class OmsorgsdagerAleneOmOmsorgenKomplettSøknad(
    private val søknadId: String,
    private val mottatt: ZonedDateTime,
    private val søker: Søker,
    private val språk: String,
    private val barn: Barn,
    private val k9Søknad: Søknad,
    private val harForståttRettigheterOgPlikter: Boolean,
    private val harBekreftetOpplysninger: Boolean
): KomplettInnsending
