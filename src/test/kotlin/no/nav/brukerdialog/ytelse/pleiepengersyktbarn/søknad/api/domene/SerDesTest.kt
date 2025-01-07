package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.brukerdialog.ytelse.fellesdomene.Land
import no.nav.brukerdialog.ytelse.fellesdomene.Næringstype
import no.nav.brukerdialog.ytelse.fellesdomene.Regnskapsfører
import no.nav.brukerdialog.ytelse.fellesdomene.VarigEndring
import no.nav.brukerdialog.ytelse.fellesdomene.Virksomhet
import no.nav.brukerdialog.ytelse.fellesdomene.YrkesaktivSisteTreFerdigliknedeArene
import no.nav.brukerdialog.config.JacksonConfiguration.Companion.zonedDateTimeFormatter
import no.nav.brukerdialog.oppslag.soker.Søker
import no.nav.brukerdialog.utils.SøknadUtils.Companion.objectMapper
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.arbeid.ArbeidIPeriode
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.arbeid.ArbeidIPeriodeType
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.arbeid.Arbeidsforhold
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.arbeid.NormalArbeidstid
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.utils.SøknadUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

internal class SerDesTest {

    @Test
    fun `Test reserialisering av request`() {
        val søknadId = UUID.randomUUID().toString()
        val mottatt = ZonedDateTime.parse("2021-01-10T03:04:05.000Z", zonedDateTimeFormatter)
        val søknad = SøknadUtils.defaultSøknad(søknadId).copy(mottatt = mottatt)
        val søknadJson = søknadJson(søknadId, mottatt.toString())
        val serialisertJson = søknad.somJson()
        JSONAssert.assertEquals(søknadJson, serialisertJson, true)
        assertEquals(søknad, objectMapper.readValue<PleiepengerSyktBarnSøknad>(søknadJson))
    }

    @Test
    fun `Test serialisering av request til mottak`() {
        val søknadId = UUID.randomUUID().toString()
        val mottatt = ZonedDateTime.parse("2021-01-10T03:04:05.000Z")
        val komplettSøknad = komplettSøknad(søknadId).copy(mottatt = mottatt)
        val komplettSøknadJson = komplettSøknadJson(søknadId, zonedDateTimeFormatter.format(mottatt))

        JSONAssert.assertEquals(komplettSøknadJson, komplettSøknad.somJson(), true)
        assertEquals(komplettSøknad, objectMapper.readValue<KomplettPleiepengerSyktBarnSøknad>(komplettSøknadJson))
    }

    private companion object {
        fun PleiepengerSyktBarnSøknad.somJson(): String = objectMapper.writeValueAsString(this)
        fun KomplettPleiepengerSyktBarnSøknad.somJson(): String = objectMapper.writeValueAsString(this)
        fun søknadJson(søknadsId: String, mottatt: String) =
            //language=json
            """
            {
              "newVersion": null,
              "apiDataVersjon": "1.0.0",
              "søknadId" : "$søknadsId",
              "mottatt" : "$mottatt",
              "språk": "nb",
              "søkerNorskIdent": null,
              "barn": {
                "fødselsnummer": "03028104560",
                "navn": "Barn Barnesen",
                "fødselsdato" : "2018-01-01",
                "aktørId" : null,
                "årsakManglerIdentitetsnummer": null
              },
              "fraOgMed": "2021-01-01",
              "tilOgMed": "2021-10-01",
              "arbeidsgivere" :  [
                {
                  "navn": "Org",
                  "organisasjonsnummer": "917755736",
                  "erAnsatt": true,
                  "sluttetFørSøknadsperiode": null,
                  "arbeidsforhold": {
                  "normalarbeidstid": {
                    "timerPerUkeISnitt": "PT37H30M"
                  },
                  "arbeidIPeriode": {
                    "type": "ARBEIDER_VANLIG",
                    "redusertArbeid": null
                  }
                }
                },
                {
                  "navn": "JobberIkkeHerLenger",
                  "organisasjonsnummer" : "977155436",
                  "erAnsatt": false,
                  "sluttetFørSøknadsperiode": false,
                  "arbeidsforhold" : null
                }
              ],
              "vedlegg": [
                "http://localhost:8080/vedlegg/1"
              ],
              "fødselsattestVedleggUrls": [
                "http://localhost:8080/vedlegg/2"
              ],
              "medlemskap": {
                "harBoddIUtlandetSiste12Mnd": true,
                "skalBoIUtlandetNeste12Mnd": true,
                "utenlandsoppholdNeste12Mnd": [
                  {
                    "fraOgMed": "2018-01-01",
                    "tilOgMed": "2018-01-10",
                    "landkode": "DEU",
                    "landnavn": "Tyskland"
                  }
                ],
                "utenlandsoppholdSiste12Mnd": [
                  {
                    "fraOgMed": "2017-01-01",
                    "tilOgMed": "2017-01-10",
                    "landkode": "DEU",
                    "landnavn": "Tyskland"
                  }
                ]
              },
              "selvstendigNæringsdrivende": {
                "harInntektSomSelvstendig": true,
                "virksomhet": {
                  "næringstype":"ANNEN",
                  "fiskerErPåBladB": false,
                  "fraOgMed": "2021-01-01",
                  "tilOgMed": null,
                  "næringsinntekt": 1111,
                  "navnPåVirksomheten": "TullOgTøys",
                  "organisasjonsnummer": null,
                  "registrertINorge": false,
                  "registrertIUtlandet": {
                    "landkode": "DEU",
                    "landnavn": "Tyskland"
                  },
                  "erNyoppstartet": false,
                  "yrkesaktivSisteTreFerdigliknedeÅrene": {
                    "oppstartsdato": "2018-01-01"
                  },
                  "varigEndring": {
                    "dato": "2020-01-01",
                    "inntektEtterEndring": 9999,
                    "forklaring": "Korona"
                  },
                  "regnskapsfører": {
                    "navn": "Kjell Regnskap",
                    "telefon": "123456789"
                  },
                  "harFlereAktiveVirksomheter": true
                },
                "arbeidsforhold": {
                  "normalarbeidstid": {
                    "timerPerUkeISnitt": "PT37H30M"
                  },
                  "arbeidIPeriode": {
                    "type": "ARBEIDER_VANLIG",
                    "redusertArbeid": null
                  }
                }
              },
              "utenlandsoppholdIPerioden": {
                "skalOppholdeSegIUtlandetIPerioden": true,
                "opphold": [
                  {
                    "fraOgMed": "2019-10-10",
                    "tilOgMed": "2019-11-10",
                    "landkode": "SE",
                    "landnavn": "Sverige",
                    "erUtenforEøs": false,
                    "erBarnetInnlagt": true,
                    "erSammenMedBarnet":  false,
                    "perioderBarnetErInnlagt" : [
                      {
                        "fraOgMed" : "2019-10-15",
                        "tilOgMed": "2019-10-20"
                      }
                    ],
                    "årsak": "BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD"
                  },
                  {
                    "landnavn": "Sverige",
                    "landkode": "SE",
                    "fraOgMed": "2020-11-10",
                    "tilOgMed": "2020-11-15",
                    "erUtenforEøs": false,
                    "erBarnetInnlagt": true,
                    "erSammenMedBarnet":  false,
                    "perioderBarnetErInnlagt" : [
                      {
                        "fraOgMed" : "2020-11-10",
                        "tilOgMed": "2020-11-12"
                      }
                    ],
                    "årsak": "BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING"
                  },{
                    "landnavn": "Sverige",
                    "landkode": "SE",
                    "fraOgMed": "2022-12-10",
                    "tilOgMed": "2022-12-20",
                    "erUtenforEøs": false,
                    "erBarnetInnlagt": false,
                    "erSammenMedBarnet": false,
                    "perioderBarnetErInnlagt" : [],
                    "årsak": null
                  }
                ]
              },
              "opptjeningIUtlandet": [
                {
                  "navn": "Kiwi AS",
                  "opptjeningType": "ARBEIDSTAKER",
                  "land": {
                    "landkode": "BEL",
                    "landnavn": "Belgia"
                  },
                  "fraOgMed": "2022-01-01",
                  "tilOgMed": "2022-01-10"
                }
              ],
              "utenlandskNæring" : [],
              "harBekreftetOpplysninger": true,
              "harForståttRettigheterOgPlikter": true,
              "ferieuttakIPerioden": {
                "skalTaUtFerieIPerioden": true,
                "ferieuttak": [
                  {
                    "fraOgMed": "2021-01-01",
                    "tilOgMed": "2021-01-10"
                  }
                ]
              },
              "beredskap": {
                "beredskap": true,
                "tilleggsinformasjon": "Ikke beredskap"
              },
              "frilans": {
                "startdato": "2018-01-01",
                "startetFørSisteTreHeleMåneder": false,
                "sluttdato": null,
                "jobberFortsattSomFrilans": true,
                "harInntektSomFrilanser": true,
                "type": "FRILANS",
                "misterHonorar": true,
                "arbeidsforhold": {
                  "normalarbeidstid": {
                    "timerPerUkeISnitt": "PT37H30M"
                  },
                  "arbeidIPeriode": {
                   "type": "ARBEIDER_VANLIG",
                    "redusertArbeid": null
                  }
                }
              },
              "stønadGodtgjørelse": {
                "mottarStønadGodtgjørelse": true,
                "startdato": "2018-01-01",
                "sluttdato": "2018-02-01"
              },
              "nattevåk": {
                "harNattevåk": true,
                "tilleggsinformasjon": "Æ har nattevåk"
              },
              "omsorgstilbud": {
                "svarFortid": "JA",
                "svarFremtid": null,
                "erLiktHverUke": false,
                "ukedager" : null,
                "enkeltdager" : [
                      {
                        "dato": "2021-01-01",
                        "tid": "PT4H"
                      },
                      {
                        "dato": "2021-01-02",
                        "tid": "PT4H"
                      },
                      {
                        "dato": "2021-01-03",
                        "tid": "PT4H"
                      },
                      {
                        "dato": "2021-01-04",
                        "tid": "PT4H"
                      }
                    ]
               },
              "barnRelasjon" : "ANNET",
              "barnRelasjonBeskrivelse" : "Gudfar til barnet",
              "harVærtEllerErVernepliktig" : true,
              "dataBruktTilUtledningAnnetData": "{\"string\": \"tekst\", \"boolean\": false, \"number\": 1, \"array\": [1,2,3], \"object\": {\"key\": \"value\"}}"
            }
        """.trimIndent()

        fun komplettSøknadJson(søknadsId: String, mottatt: String) =
            //language=json
            """
        {
              "mottatt": "$mottatt",
              "språk": "nb",
              "apiDataVersjon": "1.0.0",
              "søknadId" : "$søknadsId",
              "søker": {
                "aktørId": "12345",
                "fødselsnummer": "26104500284",
                "fødselsdato": "1945-10-26",
                "etternavn": "Nordmann",
                "fornavn": "Ola",
                "mellomnavn": null
              },
              "barn": {
                "fødselsnummer": "03028104560",
                "navn": "Barn Barnesen",
                "aktørId": "12345",
                "fødselsdato" : "2018-01-01",
                "årsakManglerIdentitetsnummer" : null
              },
              "fraOgMed": "2020-01-01",
              "tilOgMed": "2020-02-01",
              "arbeidsgivere": [
                {
                  "navn": "Org",
                  "organisasjonsnummer": "917755736",
                  "erAnsatt": true,
                  "sluttetFørSøknadsperiode" : null,
                  "arbeidsforhold": {
                      "normalarbeidstid": {
                        "timerPerUkeISnitt": "PT37H30M"
                      },
                      "arbeidIPeriode": {
                        "type": "ARBEIDER_VANLIG",
                        "redusertArbeid": null
                      }
                    }
                }
              ],
              "vedleggId": [],
              "fødselsattestVedleggId": [],
              "medlemskap": {
                "harBoddIUtlandetSiste12Mnd": true,
                "skalBoIUtlandetNeste12Mnd": true,
                "utenlandsoppholdNeste12Mnd": [
                  {
                    "fraOgMed": "2018-01-01",
                    "tilOgMed": "2018-01-10",
                    "landkode": "DEU",
                    "landnavn": "Tyskland"
                  }
                ],
                "utenlandsoppholdSiste12Mnd": [
                  {
                    "fraOgMed": "2017-01-01",
                    "tilOgMed": "2017-01-10",
                    "landkode": "DEU",
                    "landnavn": "Tyskland"
                  }
                ]
              },
              "selvstendigNæringsdrivende": {
                "harInntektSomSelvstendig": true,
                "virksomhet": {
                  "næringstype": "ANNEN",
                  "fiskerErPåBladB": false,
                  "fraOgMed": "2020-01-01",
                  "tilOgMed": null,
                  "næringsinntekt": 1111,
                  "navnPåVirksomheten": "TullOgTøys",
                  "organisasjonsnummer": null,
                  "registrertINorge": false,
                  "registrertIUtlandet": {
                    "landkode": "DEU",
                    "landnavn": "Tyskland"
                  },
                  "erNyoppstartet": true,
                  "yrkesaktivSisteTreFerdigliknedeÅrene": {
                    "oppstartsdato": "2018-01-01"
                  },
                  "varigEndring": {
                    "dato": "2020-01-01",
                    "inntektEtterEndring": 9999,
                    "forklaring": "Korona"
                  },
                  "regnskapsfører": {
                    "navn": "Kjell Regnskap",
                    "telefon": "123456789"
                  },
                  "harFlereAktiveVirksomheter": true
                },
                "arbeidsforhold": {
                  "normalarbeidstid": {
                    "timerPerUkeISnitt": "PT37H30M"
                  },
                  "arbeidIPeriode": {
                    "type": "ARBEIDER_VANLIG",
                    "redusertArbeid": null
                  }
                }
              },
              "utenlandsoppholdIPerioden": {
                "skalOppholdeSegIUtlandetIPerioden": true,
                "opphold": [
                  {
                    "fraOgMed": "2019-10-10",
                    "tilOgMed": "2019-11-10",
                    "landkode": "SE",
                    "landnavn": "Sverige",
                    "erUtenforEøs": false,
                    "erBarnetInnlagt": true,
                    "erSammenMedBarnet":  false,
                    "perioderBarnetErInnlagt" : [
                      {
                        "fraOgMed" : "2020-01-01",
                        "tilOgMed": "2020-01-02"
                      }
                    ],
                    "årsak": "ANNET"
                  },
                  {
                    "landnavn": "Sverige",
                    "landkode": "SE",
                    "fraOgMed": "2019-10-10",
                    "tilOgMed": "2019-11-10",
                    "erUtenforEøs": false,
                    "erBarnetInnlagt": true,
                    "erSammenMedBarnet":  false,
                    "perioderBarnetErInnlagt" : [
                      {
                        "fraOgMed" : "2020-01-01",
                        "tilOgMed": "2020-01-02"
                      }
                    ],
                    "årsak": "BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING"
                  },
                  {
                    "landnavn": "Sverige",
                    "landkode": "SE",
                    "fraOgMed": "2019-10-10",
                    "tilOgMed": "2019-11-10",
                    "erUtenforEøs": false,
                    "erBarnetInnlagt": true,
                    "erSammenMedBarnet":  false,
                    "perioderBarnetErInnlagt" : [
                      {
                        "fraOgMed" : "2020-01-01",
                        "tilOgMed": "2020-01-02"
                      }
                    ],
                    "årsak": "BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD"
                  },{
                    "landnavn": "Sverige",
                    "landkode": "SE",
                    "fraOgMed": "2019-10-10",
                    "tilOgMed": "2019-11-10",
                    "erUtenforEøs": false,
                    "erBarnetInnlagt": false,
                    "erSammenMedBarnet":  false,
                    "perioderBarnetErInnlagt" : [],
                    "årsak": null
                  }
                ]
              },
              "opptjeningIUtlandet": [
                {
                  "navn": "Kiwi AS",
                  "opptjeningType": "ARBEIDSTAKER",
                  "land": {
                    "landkode": "BEL",
                    "landnavn": "Belgia"
                  },
                  "fraOgMed": "2022-01-01",
                  "tilOgMed": "2022-01-10"
                }
              ],
              "harBekreftetOpplysninger": true,
              "harForståttRettigheterOgPlikter": true,
              "ferieuttakIPerioden": {
                "skalTaUtFerieIPerioden": false,
                "ferieuttak": [
                  {
                    "fraOgMed": "2020-01-05",
                    "tilOgMed": "2020-01-07"
                  }
                ]
              },
              "utenlandskNæring" : [
                {
                  "næringstype" : "JORDBRUK_SKOGBRUK",
                  "navnPåVirksomheten" : "Flush AS",
                  "land" : {
                    "landkode" : "NLD",
                    "landnavn" : "Nederland"
                  },
                  "organisasjonsnummer" : "123ABC",
                  "fraOgMed" : "2022-01-05",
                  "tilOgMed" : null
                }
              ],
              "beredskap": {
                "beredskap": true,
                "tilleggsinformasjon": "Ikke beredskap"
              },
              "frilans": {
                  "jobberFortsattSomFrilans": true,
                  "harInntektSomFrilanser": true,
                  "startdato": "2018-01-01",
                  "startetFørSisteTreHeleMåneder": false,
                  "sluttdato": null,
                  "type": "FRILANS",
                  "misterHonorar": true,
                  "arbeidsforhold": {
                    "normalarbeidstid": {
                      "timerPerUkeISnitt": "PT37H30M"
                    },
                    "arbeidIPeriode": {
                      "type": "ARBEIDER_VANLIG",
                      "redusertArbeid": null
                    }
                  }
                },
              "stønadGodtgjørelse": {
                "mottarStønadGodtgjørelse": true,
                "startdato": "2018-01-01",
                "sluttdato": "2018-02-01"
              },
              "nattevåk": {
                "harNattevåk": true,
                "tilleggsinformasjon": "Har nattevåk"
              },
              "omsorgstilbud": null,
              "barnRelasjon" : null,
              "barnRelasjonBeskrivelse" : null,
              "harVærtEllerErVernepliktig" : true,
              "k9FormatSøknad" : null 
            } 
        """.trimIndent()

        fun komplettSøknad(søknadId: String = UUID.randomUUID().toString()) = KomplettPleiepengerSyktBarnSøknad(
            mottatt = LocalDate.parse("2020-05-05").atStartOfDay(ZoneId.of("UTC")),
            språk = Språk.nb,
            apiDataVersjon = "1.0.0",
            søknadId = søknadId,
            barn = BarnDetaljer(
                aktørId = "12345",
                fødselsnummer = "03028104560",
                fødselsdato = LocalDate.parse("2018-01-01"),
                navn = "Barn Barnesen"
            ),
            søker = Søker(
                aktørId = "12345",
                fødselsnummer = "26104500284",
                fødselsdato = LocalDate.parse("1945-10-26"),
                etternavn = "Nordmann",
                fornavn = "Ola"
            ),
            arbeidsgivere = listOf(
                Arbeidsgiver(
                    navn = "Org",
                    organisasjonsnummer = "917755736",
                    erAnsatt = true,
                    arbeidsforhold = Arbeidsforhold(
                        normalarbeidstid = NormalArbeidstid(
                            timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
                        ),
                        arbeidIPeriode = ArbeidIPeriode(
                            type = ArbeidIPeriodeType.ARBEIDER_VANLIG
                        )
                    )
                )
            ),
            vedleggId = listOf(),
            fødselsattestVedleggId = listOf(),
            fraOgMed = LocalDate.parse("2020-01-01"),
            tilOgMed = LocalDate.parse("2020-02-01"),
            nattevåk = Nattevåk(
                harNattevåk = true,
                tilleggsinformasjon = "Har nattevåk"
            ),
            selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
                harInntektSomSelvstendig = true,
                virksomhet = Virksomhet(
                    næringstype = Næringstype.ANNEN,
                    fiskerErPåBladB = false,
                    fraOgMed = LocalDate.parse("2020-01-01"),
                    næringsinntekt = 1111,
                    navnPåVirksomheten = "TullOgTøys",
                    registrertINorge = false,
                    registrertIUtlandet = Land(
                        landnavn = "Tyskland",
                        landkode = "DEU"
                    ),
                    varigEndring = VarigEndring(
                        inntektEtterEndring = 9999,
                        dato = LocalDate.parse("2020-01-01"),
                        forklaring = "Korona"
                    ),
                    regnskapsfører = Regnskapsfører(
                        "Kjell Regnskap",
                        "123456789"
                    ),
                    yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeArene(LocalDate.parse("2018-01-01")),
                    harFlereAktiveVirksomheter = true,
                    erNyoppstartet = true
                ),
                arbeidsforhold = Arbeidsforhold(
                    normalarbeidstid = NormalArbeidstid(
                        timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
                    ),
                    arbeidIPeriode = ArbeidIPeriode(
                        type = ArbeidIPeriodeType.ARBEIDER_VANLIG
                    )
                )
            ),
            medlemskap = Medlemskap(
                harBoddIUtlandetSiste12Mnd = true,
                skalBoIUtlandetNeste12Mnd = true,
                utenlandsoppholdNeste12Mnd = listOf(
                    Bosted(
                        fraOgMed = LocalDate.parse("2018-01-01"),
                        tilOgMed = LocalDate.parse("2018-01-10"),
                        landnavn = "Tyskland",
                        landkode = "DEU"
                    )
                ),
                utenlandsoppholdSiste12Mnd = listOf(
                    Bosted(
                        fraOgMed = LocalDate.parse("2017-01-01"),
                        tilOgMed = LocalDate.parse("2017-01-10"),
                        landnavn = "Tyskland",
                        landkode = "DEU"
                    )
                )
            ),
            beredskap = Beredskap(
                beredskap = true,
                tilleggsinformasjon = "Ikke beredskap"
            ),
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true,
            opptjeningIUtlandet = listOf(
                OpptjeningIUtlandet(
                    navn = "Kiwi AS",
                    opptjeningType = OpptjeningType.ARBEIDSTAKER,
                    land = Land(
                        landkode = "BEL",
                        landnavn = "Belgia",
                    ),
                    fraOgMed = LocalDate.parse("2022-01-01"),
                    tilOgMed = LocalDate.parse("2022-01-10")
                )
            ),
            utenlandskNæring = listOf(
                UtenlandskNæring(
                    næringstype = Næringstype.JORDBRUK_SKOGBRUK,
                    navnPåVirksomheten = "Flush AS",
                    land = Land("NLD", "Nederland"),
                    organisasjonsnummer = "123ABC",
                    fraOgMed = LocalDate.parse("2022-01-05")
                )
            ),
            utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(
                skalOppholdeSegIUtlandetIPerioden = true, opphold = listOf(
                    Utenlandsopphold(
                        fraOgMed = LocalDate.parse("2019-10-10"),
                        tilOgMed = LocalDate.parse("2019-11-10"),
                        landkode = "SE",
                        landnavn = "Sverige",
                        erBarnetInnlagt = true,
                        perioderBarnetErInnlagt = listOf(
                            Periode(
                                fraOgMed = LocalDate.parse("2020-01-01"),
                                tilOgMed = LocalDate.parse("2020-01-02")
                            )
                        ),
                        erUtenforEøs = false,
                        erSammenMedBarnet = false,
                        årsak = Årsak.ANNET
                    ),
                    Utenlandsopphold(
                        fraOgMed = LocalDate.parse("2019-10-10"),
                        tilOgMed = LocalDate.parse("2019-11-10"),
                        landkode = "SE",
                        landnavn = "Sverige",
                        erBarnetInnlagt = true,
                        perioderBarnetErInnlagt = listOf(
                            Periode(
                                fraOgMed = LocalDate.parse("2020-01-01"),
                                tilOgMed = LocalDate.parse("2020-01-02")
                            )
                        ),
                        erUtenforEøs = false,
                        erSammenMedBarnet = false,
                        årsak = Årsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING
                    ),
                    Utenlandsopphold(
                        fraOgMed = LocalDate.parse("2019-10-10"),
                        tilOgMed = LocalDate.parse("2019-11-10"),
                        landkode = "SE",
                        landnavn = "Sverige",
                        erBarnetInnlagt = true,
                        perioderBarnetErInnlagt = listOf(
                            Periode(
                                fraOgMed = LocalDate.parse("2020-01-01"),
                                tilOgMed = LocalDate.parse("2020-01-02")
                            )
                        ),
                        erUtenforEøs = false,
                        erSammenMedBarnet = false,
                        årsak = Årsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD
                    ),
                    Utenlandsopphold(
                        fraOgMed = LocalDate.parse("2019-10-10"),
                        tilOgMed = LocalDate.parse("2019-11-10"),
                        landkode = "SE",
                        landnavn = "Sverige",
                        erBarnetInnlagt = false,
                        erUtenforEøs = false,
                        erSammenMedBarnet = false,
                        årsak = null
                    )
                )
            ),
            ferieuttakIPerioden = FerieuttakIPerioden(
                skalTaUtFerieIPerioden = false, ferieuttak = listOf(
                    Ferieuttak(
                        fraOgMed = LocalDate.parse("2020-01-05"),
                        tilOgMed = LocalDate.parse("2020-01-07")
                    )
                )
            ),
            frilans = Frilans(
                harInntektSomFrilanser = true,
                jobberFortsattSomFrilans = true,
                startetFørSisteTreHeleMåneder = false,
                startdato = LocalDate.parse("2018-01-01"),
                misterHonorar = true,
                type = FrilansType.FRILANS,
                arbeidsforhold = Arbeidsforhold(
                    normalarbeidstid = NormalArbeidstid(
                        timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
                    ),
                    arbeidIPeriode = ArbeidIPeriode(
                        type = ArbeidIPeriodeType.ARBEIDER_VANLIG
                    )
                )
            ),
            stønadGodtgjørelse = StønadGodtgjørelse(
                mottarStønadGodtgjørelse = true,
                startdato = LocalDate.parse("2018-01-01"),
                sluttdato = LocalDate.parse("2018-02-01")
            ),
            harVærtEllerErVernepliktig = true,
            k9FormatSøknad = null
        )
    }
}
