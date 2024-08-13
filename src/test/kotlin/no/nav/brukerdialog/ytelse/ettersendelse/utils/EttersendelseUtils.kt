package no.nav.brukerdialog.ytelse.ettersendelse.utils

import no.nav.brukerdialog.config.JacksonConfiguration
import no.nav.brukerdialog.meldinger.ettersendelse.domene.Ettersendelse
import no.nav.brukerdialog.meldinger.ettersendelse.domene.Pleietrengende
import no.nav.brukerdialog.meldinger.ettersendelse.domene.Søknadstype
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.k9.ettersendelse.EttersendelseType
import no.nav.k9.ettersendelse.Ytelse
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.SøknadId
import java.net.URI
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

internal object EttersendingUtils {

    val defaultEttersendelse = no.nav.brukerdialog.ytelse.ettersendelse.api.domene.Ettersendelse(
        språk = "nb",
        mottatt = ZonedDateTime.parse("2020-01-02T03:04:05Z", JacksonConfiguration.zonedDateTimeFormatter),
        vedlegg = listOf(URI.create("http://localhost:8080/vedlegg/1").toURL()),
        søknadstype = no.nav.brukerdialog.ytelse.ettersendelse.api.domene.Søknadstype.PLEIEPENGER_LIVETS_SLUTTFASE,
        beskrivelse = "Pleiepenger .....",
        ettersendelsesType = EttersendelseType.LEGEERKLÆRING,
        pleietrengende = no.nav.brukerdialog.ytelse.ettersendelse.api.domene.Pleietrengende(norskIdentitetsnummer = "02119970078"),
        harBekreftetOpplysninger = true,
        harForståttRettigheterOgPlikter = true
    )

    internal fun defaultEttersendelse(
        søknadId: String = UUID.randomUUID().toString(),
        mottatt: ZonedDateTime = ZonedDateTime.now(),
    ) = Ettersendelse(
        språk = "nb",
        mottatt = mottatt,
        harBekreftetOpplysninger = true,
        harForståttRettigheterOgPlikter = true,
        søknadId = søknadId,
        søker = Søker(
            aktørId = "123456",
            fornavn = "Ærling",
            mellomnavn = "Øverbø",
            etternavn = "Ånsnes",
            fødselsnummer = "29099012345",
            fødselsdato = LocalDate.parse("2003-03-21")
        ),
        beskrivelse = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                "Sed accumsan erat cursus enim aliquet, ac auctor orci consequat. " +
                "Etiam nec tellus sapien. Nam gravida massa id sagittis ultrices.",
        søknadstype = Søknadstype.PLEIEPENGER_SYKT_BARN,
        vedleggId = listOf("vedlegg1", "vedlegg2", "vedlegg3"),
        titler = listOf("Vedlegg 1", "Vedlegg 2", "Vedlegg 3"),
        ettersendelsesType = EttersendelseType.LEGEERKLÆRING,
        pleietrengende = Pleietrengende(
            norskIdentitetsnummer = "29099012345",
            navn = "Ola Nordmann",
            fødselsdato = LocalDate.parse("2003-03-21")
        ),
        k9Format = no.nav.k9.ettersendelse.Ettersendelse.builder()
            .søknadId(SøknadId(søknadId))
            .søker(no.nav.k9.søknad.felles.personopplysninger.Søker(NorskIdentitetsnummer.of("29099012345")))
            .mottattDato(mottatt)
            .ytelse(Ytelse.PLEIEPENGER_SYKT_BARN)
            .type(EttersendelseType.LEGEERKLÆRING)
            .pleietrengende(no.nav.k9.ettersendelse.Pleietrengende(NorskIdentitetsnummer.of("29099012345")))
            .build()
    )
}
