package no.nav.brukerdialog.ytelse.omsorgspengermidlertidigalene.api.domene

import no.nav.k9.søknad.Søknad
import no.nav.brukerdialog.domenetjenester.innsending.KomplettInnsending
import no.nav.brukerdialog.ytelse.fellesdomene.Barn
import no.nav.brukerdialog.oppslag.soker.Søker
import java.time.ZonedDateTime

class OmsorgspengerMdlertidigAleneKomplettSøknad(
    val mottatt: ZonedDateTime,
    val søker: Søker,
    val søknadId: String,
    val id: String,
    val språk: String,
    val annenForelder: AnnenForelder,
    val barn: List<Barn>,
    val k9Format: Søknad,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean
): KomplettInnsending
