package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutvidetrett.domene

import no.nav.k9.søknad.Søknad
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Barn
import no.nav.k9brukerdialogprosessering.api.innsending.KomplettInnsending
import no.nav.k9brukerdialogprosessering.oppslag.soker.Søker
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
): KomplettInnsending {
    override fun equals(other: Any?) = this === other || (other is OmsorgspengerKroniskSyktBarnKomplettSøknad && this.equals(other))

    private fun equals(other: OmsorgspengerKroniskSyktBarnKomplettSøknad) =
            this.søknadId == other.søknadId &&
            this.k9FormatSøknad.søknadId == other.k9FormatSøknad.søknadId
}
