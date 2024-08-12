package no.nav.brukerdialog.api.ytelse.ettersending

import no.nav.k9.ettersendelse.EttersendelseType
import no.nav.brukerdialog.api.ytelse.ettersending.domene.Ettersendelse
import no.nav.brukerdialog.api.ytelse.ettersending.domene.Pleietrengende
import no.nav.brukerdialog.api.ytelse.ettersending.domene.Søknadstype
import no.nav.brukerdialog.config.JacksonConfiguration
import java.net.URI
import java.time.ZonedDateTime

object EttersendelseUtils {
    val defaultEttersendelse = Ettersendelse(
        språk = "nb",
        mottatt = ZonedDateTime.parse("2020-01-02T03:04:05Z", JacksonConfiguration.zonedDateTimeFormatter),
        vedlegg = listOf(URI.create("http://localhost:8080/vedlegg/1").toURL()),
        søknadstype = Søknadstype.PLEIEPENGER_LIVETS_SLUTTFASE,
        beskrivelse = "Pleiepenger .....",
        ettersendelsesType = EttersendelseType.LEGEERKLÆRING,
        pleietrengende = Pleietrengende(norskIdentitetsnummer = "02119970078"),
        harBekreftetOpplysninger = true,
        harForståttRettigheterOgPlikter = true
    )
}
