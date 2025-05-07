package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.soknad

import no.nav.brukerdialog.domenetjenester.innsending.KomplettInnsending
import no.nav.brukerdialog.oppslag.barn.BarnOppslag
import no.nav.brukerdialog.oppslag.soker.Søker
import no.nav.k9.søknad.Søknad
import java.time.LocalDate
import java.time.ZonedDateTime

data class UngdomsytelseKomplettSøknad(
    private val søknadId: String,
    private val søker: Søker,
    private val språk: String,
    private val startdato: LocalDate,
    private val mottatt: ZonedDateTime,
    private val barn: List<Barn>,
    private val barnErRiktig: Boolean,
    private val kontonummerFraRegister: String? = null,
    private val kontonummerErRiktig: Boolean,
    private val harForståttRettigheterOgPlikter: Boolean,
    private val harBekreftetOpplysninger: Boolean,
    private val k9Format: Søknad,
) : KomplettInnsending
