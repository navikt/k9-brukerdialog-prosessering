package no.nav.brukerdialog.meldinger.ettersendelse.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9.ettersendelse.EttersendelseType
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.dittnavvarsel.K9Beskjed
import no.nav.brukerdialog.domenetjenester.mottak.JournalføringsService
import no.nav.brukerdialog.domenetjenester.mottak.Preprosessert
import no.nav.brukerdialog.integrasjon.dokarkiv.dto.YtelseType
import no.nav.brukerdialog.ytelse.ettersendelse.kafka.domene.Ettersendelse
import no.nav.brukerdialog.ytelse.ettersendelse.kafka.domene.Søknadstype
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
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

    override fun tilJournaførigsRequest(): JournalføringsService.JournalføringsRequest {
        return JournalføringsService.JournalføringsRequest(
            ytelseType = søknadstype.somYtelseType(),
            søknadstype = søknadstype,
            norskIdent = søkerFødselsnummer(),
            sokerNavn = søkerNavn(),
            mottatt = mottattDato(),
            dokumentId = dokumenter()
        )
    }

    fun Søknadstype.somYtelseType(): YtelseType = when(søknadstype) {
        Søknadstype.PLEIEPENGER_SYKT_BARN -> YtelseType.PLEIEPENGESØKNAD_ETTERSENDING
        Søknadstype.PLEIEPENGER_LIVETS_SLUTTFASE -> YtelseType.PLEIEPENGESØKNAD_LIVETS_SLUTTFASE_ETTERSENDING
        Søknadstype.OMP_UTV_KS -> YtelseType.OMSORGSPENGESØKNAD_ETTERSENDING
        Søknadstype.OMP_UT_SNF -> YtelseType.OMSORGSPENGESØKNAD_UTBETALING_FRILANSER_SELVSTENDIG_ETTERSENDING
        Søknadstype.OMP_UT_ARBEIDSTAKER -> YtelseType.OMSORGSPENGESØKNAD_UTBETALING_ARBEIDSTAKER_ETTERSENDING
        Søknadstype.OMP_UTV_MA -> YtelseType.OMSORGSPENGESØKNAD_MIDLERTIDIG_ALENE_ETTERSENDING
        Søknadstype.OMP_UTV_AO -> YtelseType.OMSORGSDAGER_ALENEOMSORG_ETTERSENDING
    }

    override fun tilK9DittnavVarsel(metadata: MetaInfo): K9Beskjed? = null
}
