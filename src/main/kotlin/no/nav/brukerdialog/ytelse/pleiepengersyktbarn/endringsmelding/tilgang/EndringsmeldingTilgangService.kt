package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.tilgang

import no.nav.brukerdialog.integrasjon.k9sakinnsynapi.K9SakInnsynApiService
import no.nav.brukerdialog.integrasjon.k9selvbetjeningoppslag.ArbeidsgivereOppslagsService
import no.nav.brukerdialog.metrikk.MetrikkService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDate

/**
 * Tilgangskontroll for endringsmelding pleiepenger. Avgjør om innlogget bruker kan bruke endringsmeldingen,
 * og gir årsakene når svaret er nei. Reglene er portert fra `tilgang/` i
 * frontend-appen `endringsmelding-pleiepenger`, og skal gi samme svar.
 *
 * ## Flyt
 *
 * hent saker → vurder saker → hent arbeidsgivere for oppslagsperioden → vurder arbeidsforhold
 *
 * Reglene er rene funksjoner. All I/O skjer her.
 *
 * ## Regler
 *
 * Saker der alle søknadsperiodene slutter før tillatt endringsperiode, regnes som *eldre*. De blokkerer ikke,
 * og telles ikke med når vi ser etter flere saker. Se [tillattEndringsperiode].
 *
 * **Vurder saker** ([vurderSaker]): stopper ved første treff og gir én årsak.
 *
 * | Regel | Årsak |
 * |---|---|
 * | Ingen saker | [IngenTilgangÅrsak.HAR_INGEN_SAK] |
 * | Bare eldre saker | [IngenTilgangÅrsak.SØKNADSPERIODER_UTENFOR_TILLATT_ENDRINGSPERIODE] |
 * | Mer enn én sak som ikke er eldre | [IngenTilgangÅrsak.HAR_MER_ENN_EN_SAK] |
 * | Saken har ingen søknadsperioder | [IngenTilgangÅrsak.HAR_INGEN_PERIODER] |
 * | Saken ligger etter tillatt endringsperiode | [IngenTilgangÅrsak.SØKNADSPERIODER_UTENFOR_TILLATT_ENDRINGSPERIODE] |
 *
 * Ved tilgang gir den saken og oppslagsperioden som arbeidsgiverne hentes for.
 *
 * **Vurder arbeidsforhold** ([vurderArbeidsforhold]): alle reglene evalueres, og årsakene samles i denne
 * rekkefølgen. Frontend viser melding for første årsak.
 *
 * | Regel | Årsak |
 * |---|---|
 * | Saken har perioder som selvstendig næringsdrivende | [IngenTilgangÅrsak.HAR_ARBEIDSTID_SOM_SELVSTENDIG_NÆRINGSDRIVENDE] |
 * | Arbeidsgiver i saken har to ansettelser i samme uke med opphold | [IngenTilgangÅrsak.EN_ARBEIDSGIVER_TO_ANSETTELSER_SAMME_UKE_MED_OPPHOLD] |
 * | Arbeidsgiver som ikke er i saken har flere ansettelser i søknadsperiodene | [IngenTilgangÅrsak.HAR_FLERE_ANSETTELSESFORHOLD_HOS_UKJENT_ARBEIDSGIVER] |
 *
 * ## Feil
 *
 * Feil fra innsyn eller AA-reg går videre og gir 5xx. Vi bruker derfor ArbeidsgivereOppslagsService direkte
 * og ikke ArbeidsgiverService, som gjør feil om til en tom liste. En tom liste ville gitt tilgang på feil
 * grunnlag, fordi reglene om arbeidsgivere da aldri slår til.
 */
@Service
class EndringsmeldingTilgangService(
    private val k9SakInnsynApiService: K9SakInnsynApiService,
    private val arbeidsgivereOppslagsService: ArbeidsgivereOppslagsService,
    private val metrikkService: MetrikkService,
    private val clock: Clock,
) {
    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(EndringsmeldingTilgangService::class.java)
    }

    fun vurderTilgang(): TilgangResultat {
        val endringsperiode = tillattEndringsperiode(LocalDate.now(clock))
        val saker = k9SakInnsynApiService.hentSøknadsopplysninger().map { it.tilSakForTilgang() }

        val resultat = when (val sakVurdering = vurderSaker(saker, endringsperiode)) {
            is SakVurdering.IngenTilgang -> TilgangResultat.ingenTilgang(listOf(sakVurdering.årsak))
            is SakVurdering.Tilgang -> {
                val arbeidsgivere = hentArbeidsgivere(sakVurdering.oppslagsperiode)
                val årsaker = vurderArbeidsforhold(sakVurdering.sak, arbeidsgivere, endringsperiode)
                if (årsaker.isEmpty()) TilgangResultat.tilgang() else TilgangResultat.ingenTilgang(årsaker)
            }
        }

        logger.info(
            "Vurderte tilgang til endringsmelding. kanBruke={}, årsaker={}",
            resultat.kanBruke,
            resultat.årsaker.map { it.kode }
        )
        metrikkService.registrerEndringsmeldingTilgangVurdering(resultat.årsaker.firstOrNull())
        return resultat
    }

    private fun hentArbeidsgivere(oppslagsperiode: Datoperiode): List<ArbeidsgiverForTilgang> =
        arbeidsgivereOppslagsService.hentArbeidsgivere(
            fraOgMed = oppslagsperiode.fom,
            tilOgMed = oppslagsperiode.tom,
            // Alle ansettelsesperioder trengs i vurderingen av arbeidsforhold, ikke bare den siste per arbeidsgiver.
            inkluderAlleAnsettelsesperioder = true,
            // Reglene bruker bare organisasjoner. Private arbeidsgivere inneholder fnr, så de hentes ikke.
            skalHentePrivateArbeidsgivere = false,
            skalHenteFrilansoppdrag = false,
        ).arbeidsgivere.organisasjoner.grupperPerArbeidsgiver()
}
