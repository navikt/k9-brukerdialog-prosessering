package no.nav.k9brukerdialogprosessering.api.ytelse.omsorgspengermidlertidigalene.domene

import no.nav.k9.søknad.Søknad
import no.nav.k9brukerdialogprosessering.api.innsending.KomplettInnsending
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Barn
import no.nav.k9brukerdialogprosessering.oppslag.soker.Søker
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
