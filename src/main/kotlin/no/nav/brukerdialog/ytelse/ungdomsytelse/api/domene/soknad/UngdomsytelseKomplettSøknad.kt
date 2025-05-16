package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.soknad

import no.nav.brukerdialog.domenetjenester.innsending.KomplettInnsending
import no.nav.brukerdialog.oppslag.soker.Søker
import no.nav.k9.søknad.Søknad
import java.time.LocalDate
import java.time.ZonedDateTime

data class UngdomsytelseKomplettSøknad(
    val søknadId: String,
    val søker: Søker,
    val språk: String,
    val startdato: LocalDate,
    val mottatt: ZonedDateTime,
    val barn: List<Barn>,
    val barnErRiktig: Boolean,
    val kontonummerFraRegister: String?,
    val kontonummerErRiktig: Boolean?,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val k9Format: Søknad,
) : KomplettInnsending {
    override fun innsendingId(): String = søknadId
}
