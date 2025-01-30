package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.inntektsrapportering

import no.nav.brukerdialog.domenetjenester.innsending.KomplettInnsending
import no.nav.brukerdialog.oppslag.soker.Søker
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.OppgittInntektForPeriode
import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.ytelse.ung.v1.UngSøknadstype
import java.time.LocalDate
import java.time.ZonedDateTime

class UngdomsytelseKomplettInntektsrapportering(
    private val søknadId: String,
    private val søker: Søker,
    private val inntektForPeriode: OppgittInntektForPeriode? = null,
    private val mottatt: ZonedDateTime,
    private val harBekreftetInntekt: Boolean,
    private val k9Format: Søknad,
) : KomplettInnsending
