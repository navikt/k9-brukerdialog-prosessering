package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene

import no.nav.brukerdialog.domenetjenester.innsending.KomplettInnsending
import no.nav.brukerdialog.oppslag.soker.Søker
import no.nav.k9.søknad.Søknad
import java.time.LocalDate
import java.time.ZonedDateTime

class UngdomsytelseKomplettSøknad(
    private val søknadId: String,
    private val søker: Søker,
    private val språk: String,
    private val fraOgMed: LocalDate,
    private val tilOgMed: LocalDate? = null,
    inntekt: Double,
    private val mottatt: ZonedDateTime,
    private val harForståttRettigheterOgPlikter: Boolean,
    private val harBekreftetOpplysninger: Boolean,
    private val k9Format: Søknad,
) : KomplettInnsending
