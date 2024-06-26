package no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9.ettersendelse.EttersendelseType
import no.nav.k9brukerdialogprosessering.common.Ytelse
import no.nav.k9brukerdialogprosessering.dittnavvarsel.K9Beskjed
import no.nav.k9brukerdialogprosessering.innsending.Preprosessert
import no.nav.k9brukerdialogprosessering.journalforing.JournalføringsRequest
import no.nav.k9brukerdialogprosessering.kafka.types.Metadata
import no.nav.k9brukerdialogprosessering.meldinger.felles.domene.Søker
import java.time.ZonedDateTime

data class PreprosessertEttersendelse(
    val sprak: String?,
    val soknadId: String,
    val vedleggId: List<List<String>>,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    val mottatt: ZonedDateTime,
    val søker: Søker,
    val harForstattRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val beskrivelse: String?,
    val søknadstype: Søknadstype,
    val ettersendelsesType: EttersendelseType,
    val pleietrengende: Pleietrengende? = null,
    val titler: List<String>,
    val k9Format: no.nav.k9.ettersendelse.Ettersendelse,
) : Preprosessert {
    internal constructor(
        melding: Ettersendelse,
        vedleggId: List<List<String>>,
    ) : this(
        sprak = melding.språk,
        soknadId = melding.søknadId,
        vedleggId = vedleggId,
        mottatt = melding.mottatt,
        søker = melding.søker,
        beskrivelse = melding.beskrivelse,
        søknadstype = melding.søknadstype,
        ettersendelsesType = melding.ettersendelsesType,
        pleietrengende = melding.pleietrengende,
        harForstattRettigheterOgPlikter = melding.harForståttRettigheterOgPlikter,
        harBekreftetOpplysninger = melding.harBekreftetOpplysninger,
        titler = melding.titler,
        k9Format = melding.k9Format
    )

    override fun ytelse(): Ytelse = Ytelse.ETTERSENDELSE

    override fun mottattDato(): ZonedDateTime = mottatt

    override fun søkerNavn() = søker.fullnavn()

    override fun søkerFødselsnummer(): String = søker.fødselsnummer

    override fun k9FormatSøknad() = k9Format

    override fun dokumenter(): List<List<String>> = vedleggId

    override fun tilJournaførigsRequest(): JournalføringsRequest {
        return JournalføringsRequest(
            ytelse = ytelse(),
            søknadstype = søknadstype,
            norskIdent = søkerFødselsnummer(),
            sokerNavn = søkerNavn(),
            mottatt = mottattDato(),
            dokumentId = dokumenter()
        )
    }

    override fun tilK9DittnavVarsel(metadata: Metadata): K9Beskjed? = null
}
