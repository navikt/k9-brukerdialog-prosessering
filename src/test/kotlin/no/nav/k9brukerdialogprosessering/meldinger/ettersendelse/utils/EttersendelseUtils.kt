package no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.utils

import no.nav.k9.ettersendelse.Ytelse
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.domene.Ettersendelse
import no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.domene.Søknadstype
import no.nav.k9brukerdialogprosessering.meldinger.felles.domene.Søker
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

internal object EttersendingUtils {

    internal fun defaultEttersendelse(
        søknadId: String = UUID.randomUUID().toString(),
        mottatt: ZonedDateTime = ZonedDateTime.now()
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
        k9Format = no.nav.k9.ettersendelse.Ettersendelse.builder()
            .søknadId(SøknadId(søknadId))
            .søker(no.nav.k9.søknad.felles.personopplysninger.Søker(NorskIdentitetsnummer.of("29099012345")))
            .mottattDato(mottatt)
            .ytelse(Ytelse.PLEIEPENGER_SYKT_BARN)
            .build()
    )
}
