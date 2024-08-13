package no.nav.brukerdialog.ytelse.pleiepengerilivetssluttfase.api.domene

import no.nav.brukerdialog.ytelse.fellesdomene.Land
import no.nav.brukerdialog.ytelse.fellesdomene.Næringstype
import no.nav.brukerdialog.ytelse.fellesdomene.Virksomhet
import no.nav.brukerdialog.ytelse.pleiepengerilivetssluttfase.utils.SøknadUtils
import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.api.domene.ArbeidIPeriode
import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.api.domene.Arbeidsforhold
import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.api.domene.Arbeidsgiver
import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.api.domene.JobberIPeriodeSvar.REDUSERT
import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.api.domene.Medlemskap
import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.api.domene.OpptjeningIUtlandet
import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.api.domene.OpptjeningType
import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.api.domene.Pleietrengende
import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.api.domene.SelvstendigNæringsdrivende
import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.api.domene.UtenlandskNæring
import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.api.domene.Utenlandsopphold
import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.api.domene.UtenlandsoppholdIPerioden
import no.nav.brukerdialog.utils.SøknadUtils.Companion.metadata
import no.nav.brukerdialog.utils.SøknadUtils.Companion.somJson
import no.nav.brukerdialog.utils.SøknadUtils.Companion.søker
import no.nav.brukerdialog.utils.TestUtils.Validator
import no.nav.brukerdialog.utils.TestUtils.verifiserIngenValideringsFeil
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
import no.nav.brukerdialog.validation.ValidationErrorResponseException
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate

class PilsSøknadTest {
    @Test
    fun `Gyldig søknad gir ingen valideringsfeil`() {
        SøknadUtils.defaultSøknad.valider().verifiserIngenValideringsFeil()
    }

    @Test
    fun `Ugydlig søknad gir valideringsfeil`() {
        assertThrows<ValidationErrorResponseException> {
            SøknadUtils.defaultSøknad.copy(
                pleietrengende = Pleietrengende(norskIdentitetsnummer = "ABC", navn = "Bjarne"),
                medlemskap = Medlemskap(
                    harBoddIUtlandetSiste12Mnd = null,
                    skalBoIUtlandetNeste12Mnd = true,
                    utenlandsoppholdNeste12Mnd = listOf(
                        Utenlandsopphold(
                            fraOgMed = LocalDate.now().plusMonths(3),
                            tilOgMed = LocalDate.now().plusMonths(4),
                            landnavn = "Cuba",
                            landkode = "X"
                        )
                    )
                ),
                arbeidsgivere = listOf(
                    Arbeidsgiver(navn = "Org", organisasjonsnummer = "ABC123", erAnsatt = true),
                    Arbeidsgiver(
                        navn = "JobberIkkeHerLenger",
                        organisasjonsnummer = "977155436",
                        erAnsatt = false,
                        sluttetFørSøknadsperiode = false
                    )
                ),
                opptjeningIUtlandet = listOf(
                    OpptjeningIUtlandet(
                        navn = "Kiwi AS",
                        opptjeningType = OpptjeningType.ARBEIDSTAKER,
                        land = Land(landkode = "BEL", landnavn = "Belgia"),
                        fraOgMed = LocalDate.parse("2022-01-10"),
                        tilOgMed = LocalDate.parse("2022-01-09")
                    )
                ),
                utenlandskNæring = listOf(
                    UtenlandskNæring(
                        næringstype = Næringstype.FISKE,
                        navnPåVirksomheten = "Fiskeriet AS",
                        land = Land(landkode = "BEL", landnavn = " "),
                        fraOgMed = LocalDate.parse("2020-01-01")
                    )
                ),
                utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(
                    skalOppholdeSegIUtlandetIPerioden = true,
                    opphold = listOf(
                        Utenlandsopphold(
                            fraOgMed = LocalDate.parse("2020-02-01"),
                            tilOgMed = LocalDate.parse("2020-02-09"),
                            landnavn = "Cuba",
                            landkode = "UGYLDIG"
                        )
                    )
                ),
                selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
                    virksomhet = Virksomhet(
                        fraOgMed = LocalDate.parse("2015-01-01"),
                        tilOgMed = LocalDate.parse("2021-01-01"),
                        næringstype = Næringstype.ANNEN,
                        fiskerErPåBladB = true,
                        navnPåVirksomheten = "Bjarnes Bakeri",
                        registrertINorge = false,
                        registrertIUtlandet = Land("ABW", "Aruba"),
                        næringsinntekt = 9656876,
                        erNyoppstartet = false,
                        harFlereAktiveVirksomheter = false
                    ),
                    arbeidsforhold = Arbeidsforhold(37.5, ArbeidIPeriode(REDUSERT, emptyList()))
                ),
                pleierDuDenSykeHjemme = false
            ).valider()
        }.also {
            assertTrue { it.message.contains("medlemskap.harBoddIUtlandetSiste12Mnd kan ikke være null.") }
            assertTrue { it.message.contains("medlemskap.utenlandsoppholdNeste12Mnd[0].landkode/landnavn.landkode 'X' er ikke en gyldig ISO 3166-1 alpha-3 kode.") }
            assertTrue { it.message.contains("utenlandskNæring[0].land.landnavn kan ikke være tomt eller blankt.") }
            assertTrue { it.message.contains("opptjeningIUtlandet[0].tilOgMed må være lik eller etter fraOgMed.") }
            assertTrue { it.message.contains("utenlandsoppholdIPerioden.opphold[0].landkode/landnavn.landkode 'UGYLDIG' er ikke en gyldig ISO 3166-1 alpha-3 kode.") }
        }
    }

    @Test
    fun `Ugydlig søknad gir valideringsfeil (Hibernate)`() {
        Validator.verifiserValideringsFeil(
            SøknadUtils.defaultSøknad.copy(
                pleietrengende = Pleietrengende(norskIdentitetsnummer = "ABC", navn = "Bjarne"),
                medlemskap = Medlemskap(
                    harBoddIUtlandetSiste12Mnd = null,
                    skalBoIUtlandetNeste12Mnd = true,
                    utenlandsoppholdNeste12Mnd = listOf(
                        Utenlandsopphold(
                            fraOgMed = LocalDate.now().plusMonths(3),
                            tilOgMed = LocalDate.now().plusMonths(4),
                            landnavn = "Cuba",
                            landkode = "X"
                        )
                    )
                ),
                arbeidsgivere = listOf(
                    Arbeidsgiver(navn = "Org", organisasjonsnummer = "ABC123", erAnsatt = true),
                    Arbeidsgiver(
                        navn = "JobberIkkeHerLenger",
                        organisasjonsnummer = "977155436",
                        erAnsatt = false,
                        sluttetFørSøknadsperiode = false
                    )
                ),
                opptjeningIUtlandet = listOf(
                    OpptjeningIUtlandet(
                        navn = "Kiwi AS",
                        opptjeningType = OpptjeningType.ARBEIDSTAKER,
                        land = Land(landkode = "BEL", landnavn = "Belgia"),
                        fraOgMed = LocalDate.parse("2022-01-10"),
                        tilOgMed = LocalDate.parse("2022-01-09")
                    )
                ),
                utenlandskNæring = listOf(
                    UtenlandskNæring(
                        næringstype = Næringstype.FISKE,
                        navnPåVirksomheten = "Fiskeriet AS",
                        land = Land(landkode = "BEL", landnavn = " "),
                        fraOgMed = LocalDate.parse("2020-01-01")
                    )
                ),
                utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(
                    skalOppholdeSegIUtlandetIPerioden = true,
                    opphold = listOf(
                        Utenlandsopphold(
                            fraOgMed = LocalDate.parse("2020-02-01"),
                            tilOgMed = LocalDate.parse("2020-02-09"),
                            landnavn = "Cuba",
                            landkode = "UGYLDIG"
                        )
                    )
                ),
                selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
                    virksomhet = Virksomhet(
                        fraOgMed = LocalDate.parse("2015-01-01"),
                        tilOgMed = LocalDate.parse("2021-01-01"),
                        næringstype = Næringstype.ANNEN,
                        fiskerErPåBladB = true,
                        navnPåVirksomheten = "Bjarnes Bakeri",
                        registrertINorge = false,
                        registrertIUtlandet = Land("ABW", "Aruba"),
                        næringsinntekt = 9656876,
                        erNyoppstartet = false,
                        harFlereAktiveVirksomheter = false
                    ),
                    arbeidsforhold = Arbeidsforhold(37.5, ArbeidIPeriode(REDUSERT, emptyList()))
                ),
                pleierDuDenSykeHjemme = false
            ),
            4,
            "Kan ikke være tom liste",
            "'ABC123' matcher ikke tillatt pattern '^\\d+$'",
            "'ABC' matcher ikke tillatt pattern '^\\d+$'",
            "size must be between 11 and 11"
        )
    }

    @Test
    fun `Gyldig søknad mappes til forventet K9Format`() {
        //language=json
        val forventet = """
            {
              "søknadId": "4e62f8de-1ff6-40e9-bdcd-10485c789094",
              "versjon": "1.0.0",
              "mottattDato": "2022-01-02T03:04:05Z",
              "søker": {
                "norskIdentitetsnummer": "02119970078"
              },
              "ytelse": {
                "type": "PLEIEPENGER_LIVETS_SLUTTFASE",
                "pleietrengende": {
                  "norskIdentitetsnummer": "06098523047",
                  "fødselsdato": null
                },
                "søknadsperiode": [
                  "2021-01-01/2021-01-10"
                ],
                "trekkKravPerioder": [],
                "opptjeningAktivitet": {
                  "selvstendigNæringsdrivende": [
                    {
                      "perioder": {
                        "2015-01-01/2021-01-01": {
                          "virksomhetstyper": [
                            "ANNEN"
                          ],
                          "bruttoInntekt": 9656876,
                          "erNyoppstartet": false,
                          "registrertIUtlandet": true,
                          "landkode": "CUB"
                        }
                      },
                      "virksomhetNavn": "Bjarnes Bakeri"
                    }
                  ],
                  "frilanser": {
                    "startdato": "2019-01-01",
                    "sluttdato": "2021-05-01"
                  }
                },
                "bosteder": {
                  "perioder": {
                    "2021-01-01/2021-01-10": {
                      "land": "BRA"
                    },
                    "2023-01-01/2023-01-10": {
                      "land": "CUB"
                    }
                  },
                  "perioderSomSkalSlettes": {}
                },
                "utenlandsopphold": {
                  "perioder": {
                    "2020-02-01/2020-02-09": {
                      "land": "CUB",
                      "årsak": null,
                      "erSammenMedBarnet": true
                    }
                  },
                  "perioderSomSkalSlettes": {}
                },
                "lovbestemtFerie": {
                  "perioder": {}
                },
                "arbeidstid": {
                  "arbeidstakerList": [
                    {
                      "norskIdentitetsnummer": null,
                      "organisasjonsnummer": "917755736",
                      "organisasjonsnavn": "Org",
                      "arbeidstidInfo": {
                        "perioder": {
                          "2021-01-01/2021-01-01": {
                            "jobberNormaltTimerPerDag": "PT8H",
                            "faktiskArbeidTimerPerDag": "PT4H"
                          },
                          "2021-01-04/2021-01-04": {
                            "jobberNormaltTimerPerDag": "PT8H",
                            "faktiskArbeidTimerPerDag": "PT0S"
                          },
                          "2021-01-05/2021-01-05": {
                            "jobberNormaltTimerPerDag": "PT8H",
                            "faktiskArbeidTimerPerDag": "PT0S"
                          },
                          "2021-01-06/2021-01-06": {
                            "jobberNormaltTimerPerDag": "PT8H",
                            "faktiskArbeidTimerPerDag": "PT0S"
                          },
                          "2021-01-07/2021-01-07": {
                            "jobberNormaltTimerPerDag": "PT8H",
                            "faktiskArbeidTimerPerDag": "PT0S"
                          },
                          "2021-01-08/2021-01-08": {
                            "jobberNormaltTimerPerDag": "PT8H",
                            "faktiskArbeidTimerPerDag": "PT0S"
                          }
                        }
                      }
                    },
                    {
                      "norskIdentitetsnummer": null,
                      "organisasjonsnummer": "977155436",
                      "organisasjonsnavn": "JobberIkkeHerLenger",
                      "arbeidstidInfo": {
                        "perioder": {
                          "2021-01-01/2021-01-10": {
                            "jobberNormaltTimerPerDag": "PT0S",
                            "faktiskArbeidTimerPerDag": "PT0S"
                          }
                        }
                      }
                    }
                  ],
                  "frilanserArbeidstidInfo": {
                    "perioder": {
                      "2021-01-01/2021-01-10": {
                        "jobberNormaltTimerPerDag": "PT0S",
                        "faktiskArbeidTimerPerDag": "PT0S"
                      }
                    }
                  },
                  "selvstendigNæringsdrivendeArbeidstidInfo": {
                    "perioder": {
                      "2021-01-01/2021-01-01": {
                        "jobberNormaltTimerPerDag": "PT7H30M",
                        "faktiskArbeidTimerPerDag": "PT0S"
                      },
                      "2021-01-02/2021-01-02": {
                        "jobberNormaltTimerPerDag": "PT7H30M",
                        "faktiskArbeidTimerPerDag": "PT0S"
                      },
                      "2021-01-03/2021-01-03": {
                        "jobberNormaltTimerPerDag": "PT7H30M",
                        "faktiskArbeidTimerPerDag": "PT0S"
                      },
                      "2021-01-04/2021-01-04": {
                        "jobberNormaltTimerPerDag": "PT7H30M",
                        "faktiskArbeidTimerPerDag": "PT0S"
                      },
                      "2021-01-05/2021-01-05": {
                        "jobberNormaltTimerPerDag": "PT7H30M",
                        "faktiskArbeidTimerPerDag": "PT0S"
                      },
                      "2021-01-06/2021-01-06": {
                        "jobberNormaltTimerPerDag": "PT7H30M",
                        "faktiskArbeidTimerPerDag": "PT0S"
                      },
                      "2021-01-07/2021-01-07": {
                        "jobberNormaltTimerPerDag": "PT7H30M",
                        "faktiskArbeidTimerPerDag": "PT0S"
                      },
                      "2021-01-08/2021-01-08": {
                        "jobberNormaltTimerPerDag": "PT7H30M",
                        "faktiskArbeidTimerPerDag": "PT0S"
                      },
                      "2021-01-09/2021-01-09": {
                        "jobberNormaltTimerPerDag": "PT7H30M",
                        "faktiskArbeidTimerPerDag": "PT0S"
                      },
                      "2021-01-10/2021-01-10": {
                        "jobberNormaltTimerPerDag": "PT7H30M",
                        "faktiskArbeidTimerPerDag": "PT0S"
                      }
                    }
                  }
                },
                "uttak": {
                  "perioder": {
                    "2021-01-01/2021-01-10": {
                      "timerPleieAvBarnetPerDag": "PT7H30M"
                    }
                  }
                },
                "dataBruktTilUtledning": {
                    "harBekreftetOpplysninger": true,
                    "harForståttRettigheterOgPlikter": true,
                    "soknadDialogCommitSha": "abc-123",
                    "annetData": "{\"string\": \"tekst\", \"boolean\": false, \"number\": 1, \"array\": [1,2,3], \"object\": {\"key\": \"value\"}}"
                }
              },
              "språk": "nb",
              "journalposter": [],
              "begrunnelseForInnsending": {
                "tekst": null
              },
              "kildesystem": "søknadsdialog"
            }
        """.trimIndent()
        JSONAssert.assertEquals(
            forventet,
            SøknadUtils.defaultSøknad.copy(pleierDuDenSykeHjemme = false).somK9Format(søker, metadata).somJson(),
            true
        )
    }
}
