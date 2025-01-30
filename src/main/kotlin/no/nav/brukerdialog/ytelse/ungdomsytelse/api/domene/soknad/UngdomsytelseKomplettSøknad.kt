package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.soknad

import no.nav.brukerdialog.domenetjenester.innsending.KomplettInnsending
import no.nav.brukerdialog.oppslag.soker.Søker
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.OppgittInntektForPeriode
import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.ytelse.ung.v1.UngSøknadstype
import java.time.LocalDate
import java.time.ZonedDateTime

class UngdomsytelseKomplettSøknad(
    private val søknadId: String,
    private val søker: Søker,
    private val språk: String,
    private val startdato: LocalDate,
    private val søknadstype: UngSøknadstype,
    private val inntektForPeriode: OppgittInntektForPeriode? = null,
    private val mottatt: ZonedDateTime,
    private val harForståttRettigheterOgPlikter: Boolean,
    private val harBekreftetOpplysninger: Boolean,
    private val k9Format: Søknad,
) : KomplettInnsending
