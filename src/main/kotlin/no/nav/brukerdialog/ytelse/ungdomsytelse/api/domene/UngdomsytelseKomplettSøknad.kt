package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene

import no.nav.brukerdialog.domenetjenester.innsending.KomplettInnsending
import no.nav.brukerdialog.oppslag.soker.Søker
import java.time.LocalDate
import java.time.ZonedDateTime
import no.nav.k9.søknad.Søknad as K9Søknad

class UngdomsytelseKomplettSøknad(
    private val søknadId: String,
    private val søker: Søker,
    private val språk: String,
    private val fraOgMed: LocalDate,
    private val tilOgMed: LocalDate,
    private val mottatt: ZonedDateTime,
    private val harForståttRettigheterOgPlikter: Boolean,
    private val harBekreftetOpplysninger: Boolean,
    private val k9Format: K9Søknad,
) : KomplettInnsending
