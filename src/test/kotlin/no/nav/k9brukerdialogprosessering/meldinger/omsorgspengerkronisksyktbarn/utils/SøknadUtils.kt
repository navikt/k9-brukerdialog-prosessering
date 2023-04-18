package no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn.utils

import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.OmsorgspengerKroniskSyktBarn
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn.domene.Barn
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn.domene.OMPUTVKroniskSyktBarnSøknadMottatt
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn.domene.Søker
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn.domene.SøkerBarnRelasjon
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

object SøknadUtils {
    val søker = Søker(
        aktørId = "12345",
        fødselsdato = LocalDate.parse("2000-01-01"),
        fornavn = "Kjell",
        mellomnavn = null,
        etternavn = "Kjeller",
        fødselsnummer = "26104500284"
    )

    val barn = Barn(
        norskIdentifikator = "02119970078",
        navn = "Ole Dole Doffen",
        aktørId = "123456",
        fødselsdato = LocalDate.parse("2020-01-01")
    )
    fun defaultK9Format(søknadId: String, mottatt: ZonedDateTime) = Søknad(
        SøknadId.of(søknadId),
        Versjon.of("1.0.0"),
        mottatt,
        no.nav.k9.søknad.felles.personopplysninger.Søker(
            NorskIdentitetsnummer.of("26104500284")
        ),
        OmsorgspengerKroniskSyktBarn(
            no.nav.k9.søknad.felles.personopplysninger.Barn()
                .medNorskIdentitetsnummer(NorskIdentitetsnummer.of("02119970078")),
            true
        )
    )

    fun defaultSøknad(søknadId: String, mottatt: ZonedDateTime) = OMPUTVKroniskSyktBarnSøknadMottatt(
        nyVersjon = false,
        søknadId = søknadId,
        mottatt = mottatt,
        språk = "nb",
        søker = søker,
        kroniskEllerFunksjonshemming = false,
        barn = barn,
        sammeAdresse = true,
        relasjonTilBarnet = SøkerBarnRelasjon.FAR,
        samværsavtaleVedleggId = listOf("1234"),
        legeerklæringVedleggId = listOf("5678"),
        harForståttRettigheterOgPlikter = true,
        harBekreftetOpplysninger = true,
        k9FormatSøknad = defaultK9Format(søknadId, mottatt)
    )
}
