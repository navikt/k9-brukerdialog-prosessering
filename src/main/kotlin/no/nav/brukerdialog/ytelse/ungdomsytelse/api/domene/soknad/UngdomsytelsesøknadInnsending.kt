package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.soknad

import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.domenetjenester.innsending.Innsending
import no.nav.brukerdialog.oppslag.soker.Søker
import no.nav.brukerdialog.ytelse.Ytelse
import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.SøknadValidator
import no.nav.k9.søknad.felles.Kildesystem
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.type.Språk
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.ung.v1.UngSøknadstype
import no.nav.k9.søknad.ytelse.ung.v1.Ungdomsytelse
import no.nav.k9.søknad.ytelse.ung.v1.UngdomsytelseSøknadValidator
import java.net.URL
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as UngSøknad

data class UngdomsytelsesøknadInnsending(
    val oppgaveReferanse: String = UUID.randomUUID().toString(),
    val deltakelseId: String,

    val språk: String,

    val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),

    val startdato: LocalDate,
    val søkerNorskIdent: String,

    var barn: List<Barn>?,
    val barnErRiktig: Boolean,

    val kontonummerFraRegister: String? = null,
    val kontonummerErRiktig: Boolean? = null,

    val harBekreftetOpplysninger: Boolean,
    val harForståttRettigheterOgPlikter: Boolean,

    ) : Innsending {
    companion object {
        private val K9_SØKNAD_VERSJON = Versjon.of("1.0.0")
        private val SØKNAD_TYPE = UngSøknadstype.DELTAKELSE_SØKNAD
    }

    override fun somKomplettSøknad(
        søker: Søker,
        k9Format: no.nav.k9.søknad.Innsending?,
        titler: List<String>,
    ): UngdomsytelseKomplettSøknad {
        requireNotNull(k9Format)
        return UngdomsytelseKomplettSøknad(
            oppgaveReferanse = oppgaveReferanse,
            mottatt = mottatt,
            søker = søker,
            språk = språk,
            startdato = startdato,
            barn = barn!!,
            barnErRiktig = barnErRiktig,
            kontonummerFraRegister = kontonummerFraRegister,
            kontonummerErRiktig = kontonummerErRiktig,
            harForståttRettigheterOgPlikter = harForståttRettigheterOgPlikter,
            harBekreftetOpplysninger = harBekreftetOpplysninger,
            k9Format = k9Format as UngSøknad
        )
    }

    override fun valider() = mutableListOf<String>()

    override fun somK9Format(søker: Søker, metadata: MetaInfo): UngSøknad {
        val ytelse = Ungdomsytelse()
            .medStartdato(startdato)
            .medSøknadType(SØKNAD_TYPE)
            .medDeltakelseId(UUID.fromString(deltakelseId))

        return UngSøknad()
            .medVersjon(K9_SØKNAD_VERSJON)
            .medMottattDato(mottatt)
            .medSpråk(Språk.of(språk))
            .medSøknadId(SøknadId(oppgaveReferanse))
            .medSøker(søker.somK9Søker())
            .medYtelse(ytelse)
            .medKildesystem(Kildesystem.SØKNADSDIALOG)
    }

    override fun søkerNorskIdent(): String? = søkerNorskIdent
    override fun ytelse(): Ytelse = Ytelse.UNGDOMSYTELSE
    override fun innsendingId(): String = oppgaveReferanse
    override fun vedlegg(): List<URL> = mutableListOf()
    override fun søknadValidator(): SøknadValidator<Søknad> = UngdomsytelseSøknadValidator()
}
