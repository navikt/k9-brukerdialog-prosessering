package no.nav.k9brukerdialogprosessering.api.ytelse.omsorgspengerutvidetrett.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9.søknad.Søknad
import no.nav.k9brukerdialogprosessering.api.innsending.KomplettInnsending
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Barn
import no.nav.k9brukerdialogprosessering.oppslag.soker.Søker
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

class OmsorgspengerKroniskSyktBarnKomplettSøknad(
    private val språk: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
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
