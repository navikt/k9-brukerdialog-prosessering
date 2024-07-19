package no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9brukerdialogprosessering.api.innsending.KomplettInnsending
import no.nav.k9brukerdialogprosessering.oppslag.soker.Søker
import java.time.LocalDate
import java.time.ZonedDateTime
import no.nav.k9.søknad.Søknad as K9Søknad

class PilsKomplettSøknad(
    private val søknadId: String,
    private val søker: Søker,
    private val språk: String,
    private val fraOgMed: LocalDate,
    private val tilOgMed: LocalDate,
    private val skalJobbeOgPleieSammeDag: Boolean,
    private val dagerMedPleie: List<LocalDate>,
    private val mottatt: ZonedDateTime,
    private val vedleggId: List<String>,
    private val opplastetIdVedleggId: List<String>,
    private val medlemskap: Medlemskap,
    private val pleietrengende: Pleietrengende,
    private val utenlandsoppholdIPerioden: UtenlandsoppholdIPerioden,
    private val frilans: Frilans?,
    private val arbeidsgivere: List<Arbeidsgiver>,
    private val opptjeningIUtlandet: List<OpptjeningIUtlandet>,
    private val utenlandskNæring: List<UtenlandskNæring>,
    private val selvstendigNæringsdrivende: SelvstendigNæringsdrivende?,
    private val harVærtEllerErVernepliktig: Boolean?,
    private val pleierDuDenSykeHjemme: Boolean? = null,
    private val harForståttRettigheterOgPlikter: Boolean,
    private val harBekreftetOpplysninger: Boolean,
    private val flereSokere: FlereSokereSvar? = null,
    private val k9Format: K9Søknad
): KomplettInnsending
