package no.nav.brukerdialog.ytelse.omsorgpengerutbetalingat.api.domene

import no.nav.k9.søknad.Søknad
import no.nav.brukerdialog.domenetjenester.innsending.KomplettInnsending
import no.nav.brukerdialog.ytelse.fellesdomene.Bekreftelser
import no.nav.brukerdialog.ytelse.fellesdomene.Bosted
import no.nav.brukerdialog.ytelse.fellesdomene.Opphold
import no.nav.brukerdialog.oppslag.soker.Søker
import java.time.ZonedDateTime

data class OmsorgspengerutbetalingArbeidstakerKomplettSøknad(
    internal val søknadId: String,
    private val mottatt: ZonedDateTime,
    private val språk: String,
    private val søker: Søker,
    private val vedleggId: List<String>,
    private val titler: List<String>,
    private val bosteder: List<Bosted>,
    private val opphold: List<Opphold>,
    private val bekreftelser: Bekreftelser,
    private val arbeidsgivere: List<Arbeidsgiver>,
    private val dineBarn: DineBarn,
    private val hjemmePgaSmittevernhensyn: Boolean,
    private val hjemmePgaStengtBhgSkole: Boolean? = null,
    private val k9Format: Søknad
): KomplettInnsending {
    override fun innsendingId(): String = søknadId
}
