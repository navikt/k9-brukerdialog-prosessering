package no.nav.brukerdialog.meldinger.omsorgspengeraleneomsorg.utils

import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.OmsorgspengerAleneOmsorg
import no.nav.brukerdialog.meldinger.felles.domene.Søker
import no.nav.brukerdialog.meldinger.omsorgspengeraleneomsorg.domene.Barn
import no.nav.brukerdialog.meldinger.omsorgspengeraleneomsorg.domene.OMPAleneomsorgSoknadMottatt
import no.nav.brukerdialog.meldinger.omsorgspengeraleneomsorg.domene.TidspunktForAleneomsorg
import no.nav.brukerdialog.meldinger.omsorgspengeraleneomsorg.domene.TypeBarn
import java.time.LocalDate
import java.time.ZonedDateTime

internal object OMPAleneomsorgSoknadUtils {

    internal fun defaultSøknad(søknadId: String, mottatt: ZonedDateTime): OMPAleneomsorgSoknadMottatt {
        val søkerFødselsnummer = "02119970078"
        return OMPAleneomsorgSoknadMottatt(
            språk = "nb",
            søknadId = søknadId,
            mottatt = mottatt,
            søker = Søker(
                aktørId = "123456",
                fødselsnummer = søkerFødselsnummer,
                fødselsdato = LocalDate.parse("1993-01-04"),
                etternavn = "Nordmann",
                mellomnavn = "Mellomnavn",
                fornavn = "Ola"
            ),
            barn = Barn(
                navn = "Ole Dole",
                type = TypeBarn.FRA_OPPSLAG,
                identitetsnummer = "29076523302",
                aktørId = "12345",
                tidspunktForAleneomsorg = TidspunktForAleneomsorg.SISTE_2_ÅRENE,
                dato = LocalDate.parse("2020-08-07")
            ),
            k9Søknad = Søknad(
                SøknadId(søknadId),
                Versjon.of("1.0.0"),
                mottatt,
                no.nav.k9.søknad.felles.personopplysninger.Søker(NorskIdentitetsnummer.of(søkerFødselsnummer)),
                OmsorgspengerAleneOmsorg(
                    no.nav.k9.søknad.felles.personopplysninger.Barn()
                        .medNorskIdentitetsnummer(NorskIdentitetsnummer.of("29076523302")),
                    Periode(mottatt.toLocalDate(), null)
                )

            ),
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true
        )
    }
}
