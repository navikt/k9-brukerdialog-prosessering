package no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingat.domene

import no.nav.k9.søknad.Søknad
import no.nav.k9brukerdialogprosessering.common.Ytelse
import no.nav.k9brukerdialogprosessering.dittnavvarsel.K9Beskjed
import no.nav.k9brukerdialogprosessering.innsending.Preprosessert
import no.nav.k9brukerdialogprosessering.journalforing.JournalføringsRequest
import no.nav.k9brukerdialogprosessering.kafka.types.Metadata
import no.nav.k9brukerdialogprosessering.meldinger.felles.domene.Søker
import java.time.ZonedDateTime
import java.util.*

data class OMPUtbetalingATSoknadPreprosessert(
    val soknadId: String,
    val mottatt: ZonedDateTime,
    val språk: String?,
    val søker: Søker,
    val arbeidsgivere: List<ArbeidsgiverDetaljer>,
    val fosterbarn: List<Fosterbarn>? = listOf(),
    val bosteder: List<Bosted>,
    val opphold: List<Opphold>,
    val bekreftelser: Bekreftelser,
    val dokumentId: List<List<String>>,
    val titler: List<String>,
    val hjemmePgaSmittevernhensyn: Boolean,
    val hjemmePgaStengtBhgSkole: Boolean? = null,
    val k9Format: Søknad
): Preprosessert {
    internal constructor(
        melding: OMPUtbetalingATSoknadMottatt,
        dokumentId: List<List<String>>,
    ) : this(
        soknadId = melding.søknadId,
        mottatt = melding.mottatt,
        språk = melding.språk,
        søker = melding.søker,
        arbeidsgivere = melding.arbeidsgivere,
        fosterbarn = melding.fosterbarn,
        bosteder = melding.bosteder,
        opphold = melding.opphold,
        bekreftelser = melding.bekreftelser,
        dokumentId = dokumentId,
        titler = melding.titler,
        hjemmePgaSmittevernhensyn = melding.hjemmePgaSmittevernhensyn,
        hjemmePgaStengtBhgSkole = melding.hjemmePgaStengtBhgSkole,
        k9Format = melding.k9Format
    )

    override fun ytelse(): Ytelse = Ytelse.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER

    override fun mottattDato(): ZonedDateTime = mottatt

    override fun søkerNavn() = søker.fullnavn()

    override fun søkerFødselsnummer(): String = søker.fødselsnummer

    override fun k9FormatSøknad(): Søknad = k9Format

    override fun dokumenter(): List<List<String>> = dokumentId

    override fun tilJournaførigsRequest(): JournalføringsRequest = JournalføringsRequest(
        ytelse = Ytelse.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER,
        norskIdent = søkerFødselsnummer(),
        sokerNavn = søkerNavn(),
        mottatt = mottattDato(),
        dokumentId = dokumenter()
    )

    override fun tilK9DittnavVarsel(metadata: Metadata): K9Beskjed = K9Beskjed(
        metadata = metadata,
        tekst = "Søknad om utbetaling av omsorgspenger er mottatt.",
        grupperingsId = soknadId,
        dagerSynlig = 7,
        søkerFødselsnummer = søkerFødselsnummer(),
        eventId = UUID.randomUUID().toString(),
        link = null,
        ytelse = "OMSORGSPENGER_UT_ARBEIDSTAKER" // TODO: Bytt til OMSORGSPENGER_UTBETALING_ARBEIDSTAKER når det er på plass i k9-dittnav-varsel
    )

    override fun toString(): String {
        return "PreprosessertMelding(soknadId='$soknadId', mottatt=$mottatt)"
    }
}
