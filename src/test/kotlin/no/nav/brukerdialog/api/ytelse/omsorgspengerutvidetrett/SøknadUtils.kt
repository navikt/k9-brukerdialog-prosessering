package no.nav.brukerdialog.api.ytelse.omsorgspengerutvidetrett

import no.nav.brukerdialog.api.ytelse.fellesdomene.Barn
import no.nav.brukerdialog.api.ytelse.omsorgspengerutvidetrett.domene.BarnSammeAdresse
import no.nav.brukerdialog.api.ytelse.omsorgspengerutvidetrett.domene.OmsorgspengerKroniskSyktBarnSøknad
import no.nav.brukerdialog.api.ytelse.omsorgspengerutvidetrett.domene.SøkerBarnRelasjon
import no.nav.brukerdialog.config.JacksonConfiguration
import java.time.ZonedDateTime

internal object SøknadUtils {
    val defaultSøknad = OmsorgspengerKroniskSyktBarnSøknad(
        språk = "nb",
        mottatt = ZonedDateTime.parse("2020-01-02T03:04:05.000Z", JacksonConfiguration.zonedDateTimeFormatter),
        barn = Barn(
            norskIdentifikator = "02119970078",
            fødselsdato = null,
            aktørId = null,
            navn = "Barn Barnsen"
        ),
        sammeAdresse = BarnSammeAdresse.JA,
        relasjonTilBarnet = SøkerBarnRelasjon.FOSTERFORELDER,
        kroniskEllerFunksjonshemming = true,
        harForståttRettigheterOgPlikter = true,
        harBekreftetOpplysninger = true,
        dataBruktTilUtledningAnnetData = "{\"string\": \"tekst\", \"boolean\": false, \"number\": 1, \"array\": [1,2,3], \"object\": {\"key\": \"value\"}}"
    )
}
