package no.nav.k9brukerdialogprosessering.meldinger.omsorgspengermidlertidigalene.utils

import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.felles.Kildesystem
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.OmsorgspengerMidlertidigAlene
import no.nav.k9brukerdialogprosessering.meldinger.felles.domene.Søker
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengermidlertidigalene.domene.AnnenForelder
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengermidlertidigalene.domene.Barn
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengermidlertidigalene.domene.OMPMidlertidigAleneSoknadMottatt
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengermidlertidigalene.domene.Situasjon
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

internal object OMPMidlertidigAleneSoknadUtils {

    internal fun defaultSøknad(søknadId: String, mottatt: ZonedDateTime) = OMPMidlertidigAleneSoknadMottatt(
        språk = "nb",
        søknadId = søknadId,
        mottatt = mottatt,
        søker = Søker(
            aktørId = "123456",
            fødselsnummer = "02119970078",
            fødselsdato = LocalDate.parse("2020-08-05"),
            etternavn = "Nordmann",
            mellomnavn = "Mellomnavn",
            fornavn = "Ola"
        ),
        annenForelder = AnnenForelder(
            navn = "Berit",
            fnr = "02119970078",
            situasjon = Situasjon.FENGSEL,
            situasjonBeskrivelse = "Sitter i «fengsel..»",
            periodeOver6Måneder = false,
            periodeFraOgMed = LocalDate.parse("2020-01-01"),
            periodeTilOgMed = LocalDate.parse("2020-10-01")
        ),
        barn = listOf(
            Barn(
                navn = "Ole Dole",
                norskIdentifikator = "29076523302",
                aktørId = null
            ),
            Barn(
                navn = "Ole Doffen",
                norskIdentifikator = "29076523303",
                aktørId = null
            )
        ),
        k9Format = gyldigK9Format(søknadId, mottatt),
        harBekreftetOpplysninger = true,
        harForståttRettigheterOgPlikter = true
    )

    fun gyldigK9Format(søknadId: String = UUID.randomUUID().toString(), mottatt: ZonedDateTime) = Søknad(
        SøknadId(søknadId),
        Versjon("1.0.0"),
        mottatt,
        no.nav.k9.søknad.felles.personopplysninger.Søker(NorskIdentitetsnummer.of("02119970078")),
        OmsorgspengerMidlertidigAlene(
            listOf(
                no.nav.k9.søknad.felles.personopplysninger.Barn().medNorskIdentitetsnummer(NorskIdentitetsnummer.of("29076523302"))
            ),
            no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.AnnenForelder(
                NorskIdentitetsnummer.of("25058118020"),
                no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.AnnenForelder.SituasjonType.FENGSEL,
                "Sitter i fengsel..",
                Periode(LocalDate.parse("2020-01-01"), LocalDate.parse("2030-01-01"))
            ),
            null
        )
    ).medKildesystem(Kildesystem.SØKNADSDIALOG)
}
