package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.inntektsrapportering

import no.nav.brukerdialog.domenetjenester.innsending.KomplettInnsending
import no.nav.brukerdialog.oppslag.soker.Søker
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.OppgittInntektForPeriode
import no.nav.k9.søknad.Søknad
import java.time.ZonedDateTime

class UngdomsytelseKomplettInntektsrapportering(
    private val søknadId: String,
    private val søker: Søker,
    private val oppgittInntektForPeriode: OppgittInntektForPeriode,
    private val mottatt: ZonedDateTime,
    private val harBekreftetInntekt: Boolean,
    private val k9Format: Søknad,
) : KomplettInnsending
