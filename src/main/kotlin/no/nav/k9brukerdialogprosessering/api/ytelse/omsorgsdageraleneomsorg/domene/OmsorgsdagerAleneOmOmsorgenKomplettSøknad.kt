package no.nav.k9brukerdialogprosessering.api.ytelse.omsorgsdageraleneomsorg.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9.søknad.Søknad
import no.nav.k9brukerdialogprosessering.api.innsending.KomplettInnsending
import no.nav.k9brukerdialogprosessering.oppslag.soker.Søker
import java.time.ZonedDateTime

class OmsorgsdagerAleneOmOmsorgenKomplettSøknad(
    private val søknadId: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private val mottatt: ZonedDateTime,
    private val søker: Søker,
    private val språk: String,
    private val barn: Barn,
    private val k9Søknad: Søknad,
    private val harForståttRettigheterOgPlikter: Boolean,
    private val harBekreftetOpplysninger: Boolean
): KomplettInnsending
