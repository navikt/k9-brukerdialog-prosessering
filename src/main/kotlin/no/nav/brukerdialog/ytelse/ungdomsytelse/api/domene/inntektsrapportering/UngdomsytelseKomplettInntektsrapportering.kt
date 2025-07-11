package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.inntektsrapportering

import no.nav.brukerdialog.domenetjenester.innsending.KomplettInnsending
import no.nav.brukerdialog.oppslag.soker.Søker
import no.nav.k9.søknad.Søknad
import java.time.ZonedDateTime

data class UngdomsytelseKomplettInntektsrapportering(
    val oppgaveReferanse: String,
    val søker: Søker,
    val oppgittInntektForPeriode: OppgittInntektForPeriode,
    val mottatt: ZonedDateTime,
    val harBekreftetInntekt: Boolean,
    val k9Format: Søknad,
) : KomplettInnsending {
    override fun innsendingId(): String = oppgaveReferanse
}
