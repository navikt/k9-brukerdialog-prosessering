package no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.domene.k9Format

import no.nav.k9.søknad.JsonUtils
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.Periode
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.Utenlandsopphold
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.UtenlandsoppholdIPerioden
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.Årsak
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate

class K9FormatUtenlandsopphold {

    @Test
    fun `UtenlandsoppholdIPerioden med en innleggelse mappes riktig`() {
        val utenlandsopphold = UtenlandsoppholdIPerioden(
            skalOppholdeSegIUtlandetIPerioden = true,
            opphold = listOf(
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2022-01-01"),
                    tilOgMed = LocalDate.parse("2022-01-10"),
                    landkode = "DE",
                    landnavn = "Tyskland",
                    erUtenforEøs = false,
                    erBarnetInnlagt = true,
                    erSammenMedBarnet = true,
                    perioderBarnetErInnlagt = listOf(
                        Periode(
                            fraOgMed = LocalDate.parse("2022-01-05"),
                            tilOgMed = LocalDate.parse("2022-01-06")
                        )
                    ),
                    årsak = Årsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD
                )
            )
        )
        val faktisk = JsonUtils.toString(utenlandsopphold.tilK9Utenlandsopphold())
        val forventet = """
            {
              "perioder": {
                "2022-01-01/2022-01-04": {
                  "land": "DE",
                  "årsak": null,
                  "erSammenMedBarnet": true
                },
                "2022-01-05/2022-01-06": {
                  "land": "DE",
                  "årsak": "barnetInnlagtIHelseinstitusjonDekketEtterAvtaleMedEtAnnetLandOmTrygd",
                  "erSammenMedBarnet": true
                },
                "2022-01-07/2022-01-10": {
                  "land": "DE",
                  "årsak": null,
                  "erSammenMedBarnet": true
                }
              },
              "perioderSomSkalSlettes": {}
            }

        """.trimIndent()
        JSONAssert.assertEquals(forventet, faktisk, true)
    }

    @Test
    fun `UtenlandsoppholdIPerioden med en innleggelse som dekker hele oppholdet mappes riktig`() {
        val utenlandsopphold = UtenlandsoppholdIPerioden(
            skalOppholdeSegIUtlandetIPerioden = true,
            opphold = listOf(
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2022-01-01"),
                    tilOgMed = LocalDate.parse("2022-01-10"),
                    landkode = "DE",
                    landnavn = "Tyskland",
                    erUtenforEøs = false,
                    erBarnetInnlagt = true,
                    erSammenMedBarnet = false,
                    perioderBarnetErInnlagt = listOf(
                        Periode(
                            fraOgMed = LocalDate.parse("2022-01-01"),
                            tilOgMed = LocalDate.parse("2022-01-10")
                        )
                    ),
                    årsak = Årsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD
                )
            )
        )
        val faktisk = JsonUtils.toString(utenlandsopphold.tilK9Utenlandsopphold())
        val forventet = """
            {
              "perioder": {
                "2022-01-01/2022-01-10": {
                  "land": "DE",
                  "årsak": "barnetInnlagtIHelseinstitusjonDekketEtterAvtaleMedEtAnnetLandOmTrygd",
                  "erSammenMedBarnet":  false
                }
              },
              "perioderSomSkalSlettes": {}
            }

        """.trimIndent()
        JSONAssert.assertEquals(forventet, faktisk, true)
    }

    @Test
    fun `Utenlandsopphold med innleggelser i kant med start og slutt mappes riktig`(){
        val utenlandsopphold = UtenlandsoppholdIPerioden(
            skalOppholdeSegIUtlandetIPerioden = true,
            opphold = listOf(
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2022-01-01"),
                    tilOgMed = LocalDate.parse("2022-01-10"),
                    landkode = "DE",
                    landnavn = "Tyskland",
                    erUtenforEøs = false,
                    erBarnetInnlagt = true,
                    erSammenMedBarnet = true,
                    perioderBarnetErInnlagt = listOf(
                        Periode(
                            fraOgMed = LocalDate.parse("2022-01-01"),
                            tilOgMed = LocalDate.parse("2022-01-03")
                        ),
                        Periode(
                            fraOgMed = LocalDate.parse("2022-01-08"),
                            tilOgMed = LocalDate.parse("2022-01-10")
                        )
                    ),
                    årsak = Årsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD
                )
            )
        )
        val faktisk = JsonUtils.toString(utenlandsopphold.tilK9Utenlandsopphold())
        val forventet = """
            {
              "perioder": {
                "2022-01-01/2022-01-03": {
                  "land": "DE",
                  "årsak": "barnetInnlagtIHelseinstitusjonDekketEtterAvtaleMedEtAnnetLandOmTrygd",
                  "erSammenMedBarnet":  true
                },
                "2022-01-04/2022-01-07": {
                  "land": "DE",
                  "årsak": null,
                  "erSammenMedBarnet": true
                },
                "2022-01-08/2022-01-10": {
                  "land": "DE",
                  "årsak": "barnetInnlagtIHelseinstitusjonDekketEtterAvtaleMedEtAnnetLandOmTrygd",
                  "erSammenMedBarnet":  true
                }
              },
              "perioderSomSkalSlettes": {}
            }

        """.trimIndent()
        JSONAssert.assertEquals(forventet, faktisk, true)
    }

    @Test
    fun `Utenlandsopphold med flere usammenhengende innleggelser mappes riktig`(){
        val utenlandsopphold = UtenlandsoppholdIPerioden(
            skalOppholdeSegIUtlandetIPerioden = true,
            opphold = listOf(
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2022-01-01"),
                    tilOgMed = LocalDate.parse("2022-01-10"),
                    landkode = "DE",
                    landnavn = "Tyskland",
                    erUtenforEøs = false,
                    erBarnetInnlagt = true,
                    erSammenMedBarnet = true,
                    perioderBarnetErInnlagt = listOf(
                        Periode(
                            fraOgMed = LocalDate.parse("2022-01-03"),
                            tilOgMed = LocalDate.parse("2022-01-04")
                        ),
                        Periode(
                            fraOgMed = LocalDate.parse("2022-01-05"),
                            tilOgMed = LocalDate.parse("2022-01-05")
                        ),
                        Periode(
                            fraOgMed = LocalDate.parse("2022-01-07"),
                            tilOgMed = LocalDate.parse("2022-01-07")
                        )
                    ),
                    årsak = Årsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD
                )
            )
        )
        val faktisk = JsonUtils.toString(utenlandsopphold.tilK9Utenlandsopphold())
        val forventet = """
            {
              "perioder": {
                "2022-01-01/2022-01-02": {
                  "land": "DE",
                  "årsak": null,
                  "erSammenMedBarnet": true
                },
                "2022-01-03/2022-01-04": {
                  "land": "DE",
                  "årsak": "barnetInnlagtIHelseinstitusjonDekketEtterAvtaleMedEtAnnetLandOmTrygd",
                  "erSammenMedBarnet":  true
                },
                "2022-01-05/2022-01-05": {
                  "land": "DE",
                  "årsak": "barnetInnlagtIHelseinstitusjonDekketEtterAvtaleMedEtAnnetLandOmTrygd",
                  "erSammenMedBarnet":  true
                },
                "2022-01-06/2022-01-06": {
                  "land": "DE",
                  "årsak": null,
                    "erSammenMedBarnet": true
                },
                "2022-01-07/2022-01-07": {
                  "land": "DE",
                  "årsak": "barnetInnlagtIHelseinstitusjonDekketEtterAvtaleMedEtAnnetLandOmTrygd",
                  "erSammenMedBarnet":  true
                },
                "2022-01-08/2022-01-10": {
                  "land": "DE",
                  "årsak": null,
                  "erSammenMedBarnet": true
                }
              },
              "perioderSomSkalSlettes": {}
            }

        """.trimIndent()
        JSONAssert.assertEquals(forventet, faktisk, true)
    }
}
