package no.nav.brukerdialog.ytelse.omsorgspengerkronisksyktbarn.api.domene

import no.nav.k9.søknad.Søknad
import no.nav.brukerdialog.domenetjenester.innsending.KomplettInnsending
import no.nav.brukerdialog.ytelse.fellesdomene.Barn
import no.nav.brukerdialog.oppslag.soker.Søker
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

class OmsorgspengerKroniskSyktBarnKomplettSøknad(
    private val språk: String,
    private val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    private val søknadId: String = UUID.randomUUID().toString(),
    private var barn: Barn,
    private val søker: Søker,
    private val sammeAdresse: BarnSammeAdresse?,
    private var legeerklæringVedleggId: List<String>,
    private var samværsavtaleVedleggId: List<String>,
    private val relasjonTilBarnet: SøkerBarnRelasjon? = null,
    private val kroniskEllerFunksjonshemming: Boolean,
    private val k9FormatSøknad: Søknad,
    private val harForståttRettigheterOgPlikter: Boolean,
    private val harBekreftetOpplysninger: Boolean,
    private val høyereRisikoForFravær: Boolean?,
    private val høyereRisikoForFraværBeskrivelse: String?
): KomplettInnsending
