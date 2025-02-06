package no.nav.brukerdialog.ytelse.omsorgpengerutbetalingsnf.api.domene

import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.brukerdialog.domenetjenester.innsending.KomplettInnsending
import no.nav.brukerdialog.ytelse.fellesdomene.Bekreftelser
import no.nav.brukerdialog.ytelse.fellesdomene.Bosted
import no.nav.brukerdialog.ytelse.fellesdomene.Opphold
import no.nav.brukerdialog.ytelse.fellesdomene.Utbetalingsperiode
import no.nav.brukerdialog.ytelse.fellesdomene.Virksomhet
import no.nav.brukerdialog.oppslag.soker.Søker
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

data class OmsorgspengerutbetalingSnfKomplettSøknad(
    internal val søknadId: SøknadId = SøknadId(UUID.randomUUID().toString()),
    private val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    private val språk: String,
    private val søker: Søker,
    private val bosteder: List<Bosted>,
    private val opphold: List<Opphold>,
    private val spørsmål: List<SpørsmålOgSvar>,
    private val harDekketTiFørsteDagerSelv: Boolean? = null,
    private val harSyktBarn: Boolean? = null,
    private val harAleneomsorg: Boolean? = null,
    private val bekreftelser: Bekreftelser,
    private val utbetalingsperioder: List<Utbetalingsperiode>,
    private val erArbeidstakerOgså: Boolean,
    private val barn: List<Barn>,
    private val frilans: Frilans? = null,
    private val selvstendigNæringsdrivende: Virksomhet? = null,
    private val vedleggId: List<String> = listOf(),
    private val titler: List<String>,
    private val k9FormatSøknad: Søknad,
) : KomplettInnsending
