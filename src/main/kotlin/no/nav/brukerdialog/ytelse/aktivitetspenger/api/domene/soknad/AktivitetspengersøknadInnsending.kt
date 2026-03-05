package no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.soknad

import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.domenetjenester.innsending.Innsending
import no.nav.brukerdialog.oppslag.soker.Søker
import no.nav.brukerdialog.ytelse.Ytelse
import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.SøknadValidator
import no.nav.k9.søknad.felles.Kildesystem
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.felles.type.Språk
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.aktivitetspenger.v1.Aktivitetspenger
import no.nav.k9.søknad.ytelse.aktivitetspenger.v1.AktivitetspengerSøknadValidator
import java.net.URL
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as AktivitetspengerSøknad

data class AktivitetspengersøknadInnsending(
    val søknadId: String = UUID.randomUUID().toString(),
    val språk: String,
    val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),

    val startdato: LocalDate,
    val søkerNorskIdent: String,

    val barnErRiktig: Boolean,

    val harBekreftetOpplysninger: Boolean,
    val harForståttRettigheterOgPlikter: Boolean,

    ) : Innsending {
    companion object {
        private val K9_SØKNAD_VERSJON = Versjon.of("1.0.0")
    }

    override fun somKomplettSøknad(
        søker: Søker,
        k9Format: no.nav.k9.søknad.Innsending?,
        titler: List<String>,
    ): AktivitetspengerKomplettSøknad {
        requireNotNull(k9Format)
        return AktivitetspengerKomplettSøknad(
            søknadId = søknadId,
            mottatt = mottatt,
            søker = søker,
            språk = språk,
            startdato = startdato,
            barnErRiktig = barnErRiktig,
            harForståttRettigheterOgPlikter = harForståttRettigheterOgPlikter,
            harBekreftetOpplysninger = harBekreftetOpplysninger,
            k9Format = k9Format as AktivitetspengerSøknad
        )
    }

    override fun valider() = mutableListOf<String>()

    override fun somK9Format(søker: Søker, metadata: MetaInfo): AktivitetspengerSøknad {
        val ytelse = Aktivitetspenger()
            .medSøknadsperiode(Periode(startdato, startdato.plusMonths(12))) //TODO endre til startdato eller fjerne dato

        return AktivitetspengerSøknad()
            .medVersjon(K9_SØKNAD_VERSJON)
            .medMottattDato(mottatt)
            .medSpråk(Språk.of(språk))
            .medSøknadId(SøknadId(søknadId))
            .medSøker(søker.somK9Søker())
            .medYtelse(ytelse)
            .medKildesystem(Kildesystem.SØKNADSDIALOG)
    }

    override fun søkerNorskIdent(): String? = søkerNorskIdent
    override fun ytelse(): Ytelse = Ytelse.AKTIVITETSPENGER
    override fun innsendingId(): String = søknadId
    override fun vedlegg(): List<URL> = mutableListOf()
    override fun søknadValidator(): SøknadValidator<Søknad> = AktivitetspengerSøknadValidator()
}
