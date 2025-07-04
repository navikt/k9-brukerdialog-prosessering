package no.nav.brukerdialog.ytelse.pleiepengerilivetssluttfase.kafka

import io.mockk.coEvery
import io.mockk.coVerify
import no.nav.brukerdialog.AbstractIntegrationTest
import no.nav.brukerdialog.ytelse.pleiepengerilivetssluttfase.utils.SøknadUtils
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.config.JacksonConfiguration
import no.nav.brukerdialog.dittnavvarsel.DittnavVarselTopologyConfiguration
import no.nav.brukerdialog.dittnavvarsel.K9Beskjed
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.meldinger.pleiepengerilivetssluttfase.utils.PilsSøknadUtils
import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.kafka.PILSTopologyConfiguration
import no.nav.brukerdialog.utils.KafkaUtils.leggPåTopic
import no.nav.brukerdialog.utils.KafkaUtils.lesMelding
import no.nav.brukerdialog.utils.MockMvcUtils.sendInnSøknad
import no.nav.brukerdialog.utils.TokenTestUtils.hentToken
import org.intellij.lang.annotations.Language
import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.net.URI
import java.time.ZonedDateTime
import java.util.*

class PleiepengerILivetsSluttfaseSøknadKonsumentTest : AbstractIntegrationTest() {

    override val consumerGroupPrefix = "pleiepenger-i-livets-sluttfase"
    override val consumerGroupTopics = listOf(
        PILSTopologyConfiguration.PILS_MOTTATT_TOPIC,
        PILSTopologyConfiguration.PILS_PREPROSESSERT_TOPIC,
        PILSTopologyConfiguration.PILS_CLEANUP_TOPIC
    )

    @Test
    fun `forvent at melding konsumeres riktig og dokumenter blir slettet`() {
        val søker = mockSøker()
        mockBarn()
        mockLagreDokument()
        mockJournalføring()

        val søknadId = UUID.randomUUID().toString()
        val søknad = SøknadUtils.defaultSøknad.copy(
            søknadId = søknadId,
            vedleggUrls = listOf(),
            opplastetIdVedleggUrls = listOf()
        )

        mockMvc.sendInnSøknad(søknad, mockOAuth2Server.hentToken())

        coVerify(exactly = 1, timeout = 120 * 1000) {
            dokumentService.slettDokumenter(any(), any())
        }

        k9DittnavVarselConsumer.lesMelding(
            key = søknadId,
            topic = DittnavVarselTopologyConfiguration.K9_DITTNAV_VARSEL_TOPIC
        ).value().assertDittnavVarsel(
            K9Beskjed(
                metadata = no.nav.brukerdialog.utils.SøknadUtils.metadata,
                grupperingsId = søknadId,
                tekst = "Søknad om pleiepenger i livets sluttfase er mottatt",
                link = null,
                dagerSynlig = 7,
                søkerFødselsnummer = søker.fødselsnummer,
                eventId = "testes ikke",
                ytelse = "PLEIEPENGER_LIVETS_SLUTTFASE",
            )
        )
    }

    @Test
    fun `Forvent at melding bli prosessert på 5 forsøk etter 4 feil`() {
        val søknadId = UUID.randomUUID().toString()
        val mottattString = "2020-01-01T10:30:15Z"
        val mottatt = ZonedDateTime.parse(mottattString, JacksonConfiguration.zonedDateTimeFormatter)
        val søknadMottatt = PilsSøknadUtils.gyldigSøknad(søknadId = søknadId, mottatt = mottatt)
        val correlationId = UUID.randomUUID().toString()
        val metadata = MetaInfo(version = 1, correlationId = correlationId)
        val topicEntry = TopicEntry(metadata, søknadMottatt)
        val topicEntryJson = objectMapper.writeValueAsString(topicEntry)

        coEvery { dokumentService.lagreDokument(any(), any(), any(), any()) }
            .throws(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenMany(listOf("123456789", "987654321"))

        producer.leggPåTopic(
            key = søknadId,
            value = topicEntryJson,
            topic = PILSTopologyConfiguration.PILS_MOTTATT_TOPIC
        )
        val lesMelding =
            consumer.lesMelding(key = søknadId, topic = PILSTopologyConfiguration.PILS_PREPROSESSERT_TOPIC)
                .value()

        val preprosessertSøknadJson = JSONObject(lesMelding).getJSONObject("data").toString()
        JSONAssert.assertEquals(preprosessertSøknadSomJson(søknadId, mottattString), preprosessertSøknadJson, true)
    }

    @Language("JSON")
    private fun preprosessertSøknadSomJson(søknadId: String, mottatt: String) = """
        {
          "søknadId": "$søknadId",
          "mottatt": "$mottatt",
          "søker": {
            "etternavn": "Nordmann",
            "mellomnavn": "Mellomnavn",
            "aktørId": "123456",
            "fødselsdato": "2000-01-01",
            "fornavn": "Ola",
            "fødselsnummer": "02119970078"
          },
          "pleietrengende": {
            "fødselsdato": null,
            "navn": "Bjarne",
            "årsakManglerIdentitetsnummer": null,
            "norskIdentitetsnummer": "02119970078"
          },
          "flereSokere": "JA",
          "selvstendigNæringsdrivende": {
            "arbeidsforhold": {
              "arbeidIPeriode": {
                "enkeltdager": null,
                "jobberIPerioden": "SOM_VANLIG"
              },
              "jobberNormaltTimer": 7.5
            },
            "virksomhet": {
              "varigEndring": {
                "forklaring": "Opplevde en varig endring fordi.....",
                "dato": "2019-09-09",
                "inntektEtterEndring": 854875
              },
              "harFlereAktiveVirksomheter": false,
              "næringsinntekt": 9656876,
              "fraOgMed": "2015-01-01",
              "erNyoppstartet": false,
              "yrkesaktivSisteTreFerdigliknedeÅrene": {
                "oppstartsdato": "2020-03-04"
              },
              "næringstype": "FISKE",
              "tilOgMed": null,
              "fiskerErPåBladB": false,
              "navnPåVirksomheten": "Bjarnes Bakeri",
              "registrertINorge": false,
              "organisasjonsnummer": null,
              "registrertIUtlandet": {
                "landkode": "ABW",
                "landnavn": "Aruba"
              },
              "regnskapsfører": {
                "telefon": "987654321",
                "navn": "Regn"
              }
            }
          },
          "medlemskap": {
            "skalBoIUtlandetNeste12Mnd": true,
            "harBoddIUtlandetSiste12Mnd": true,
            "utenlandsoppholdSiste12Mnd": [
              {
                "fraOgMed": "2020-01-01",
                "landkode": "BR",
                "landnavn": "Brasil",
                "tilOgMed": "2020-01-10"
              }
            ],
            "utenlandsoppholdNeste12Mnd": [
              {
                "fraOgMed": "2021-01-01",
                "landkode": "CU",
                "landnavn": "Cuba",
                "tilOgMed": "2021-01-10"
              }
            ]
          },
          "frilans": {
            "harHattInntektSomFrilanser": true,
            "jobberFortsattSomFrilans": false,
            "startdato": "2015-01-01",
            "arbeidsforhold": {
              "arbeidIPeriode": {
                "enkeltdager": null,
                "jobberIPerioden": "HELT_FRAVÆR"
              },
              "jobberNormaltTimer": 7.5
            },
            "sluttdato": "2021-01-01"
          },
          "harVærtEllerErVernepliktig": false,
          "opptjeningIUtlandet": [
            {
              "fraOgMed": "2022-01-01",
              "tilOgMed": "2022-01-10",
              "navn": "Kiwi AS",
              "land": {
                "landkode": "IKKE GYLDIG",
                "landnavn": "Belgia"
              },
              "opptjeningType": "ARBEIDSTAKER"
            }
          ],
          "fraOgMed": "2022-01-01",
          "språk": "nb",
          "utenlandskNæring": [
            {
              "fraOgMed": "2022-01-01",
              "næringstype": "DAGMAMMA",
              "tilOgMed": "2022-01-10",
              "land": {
                "landkode": "NDL",
                "landnavn": "Nederland"
              },
              "navnPåVirksomheten": "Dagmamma AS",
              "organisasjonsnummer": "123ABC"
            }
          ],
          "arbeidsgivere": [
            {
              "erAnsatt": true,
              "arbeidsforhold": {
                "arbeidIPeriode": {
                  "enkeltdager": [
                    {
                      "dato": "2022-01-01",
                      "tid": "PT4H"
                    },
                    {
                      "dato": "2022-01-02",
                      "tid": "PT4H"
                    },
                    {
                      "dato": "2022-01-03",
                      "tid": "PT4H"
                    },
                    {
                      "dato": "2022-01-04",
                      "tid": "PT4H"
                    },
                    {
                      "dato": "2022-02-01",
                      "tid": "PT4H"
                    },
                    {
                      "dato": "2022-02-02",
                      "tid": "PT4H"
                    },
                    {
                      "dato": "2022-04-10",
                      "tid": "PT4H"
                    }
                  ],
                  "jobberIPerioden": "REDUSERT"
                },
                "jobberNormaltTimer": 7.5
              },
              "navn": "Something Fishy AS",
              "sluttetFørSøknadsperiode": false,
              "organisasjonsnummer": "123456789"
            },
            {
              "erAnsatt": false,
              "arbeidsforhold": null,
              "navn": "Slutta",
              "sluttetFørSøknadsperiode": true,
              "organisasjonsnummer": "12121212"
            }
          ],
          "tilOgMed": "2022-02-01",
          "harForståttRettigheterOgPlikter": true,
          "dokumentId": [
            [
              "123456789",
              "987654321"
            ],
            [
              "123"
            ],
            [
              "456"
            ],
            [
              "987"
            ]
          ],
          "harBekreftetOpplysninger": true,
          "utenlandsoppholdIPerioden": {
            "opphold": [
              {
                "fraOgMed": "2021-01-01",
                "landkode": "CU",
                "landnavn": "Cuba",
                "tilOgMed": "2021-01-10"
              },
              {
                "fraOgMed": "2021-02-01",
                "landkode": "CU",
                "landnavn": "Cuba",
                "tilOgMed": "2021-02-10"
              }
            ],
            "skalOppholdeSegIUtlandetIPerioden": true
          },
          "k9Format": {
            "språk": "nb",
            "kildesystem": "søknadsdialog",
            "mottattDato": "$mottatt",
            "søknadId": "$søknadId",
            "søker": {
              "norskIdentitetsnummer": "02119970078"
            },
            "ytelse": {
              "arbeidstid": {
                "frilanserArbeidstidInfo": null,
                "arbeidstakerList": [],
                "selvstendigNæringsdrivendeArbeidstidInfo": null
              },
              "utenlandsopphold": {
                "perioder": {
                  "2021-03-01\/2021-03-03": {
                    "årsak": null,
                    "land": "CAN",
                     "erSammenMedBarnet": true
                  }
                },
                "perioderSomSkalSlettes": {}
              },
              "uttak": {
                "perioder": {}
              },
              "søknadsperiode": [],
              "type": "PLEIEPENGER_LIVETS_SLUTTFASE",
              "bosteder": {
                "perioder": {
                  "2021-01-01\/2021-01-01": {
                    "land": "DNK"
                  }
                },
                "perioderSomSkalSlettes": {}
              },
              "lovbestemtFerie": {
                "perioder": {}
              },
              "pleietrengende": {
                "fødselsdato": null,
                "norskIdentitetsnummer": "02119970078"
              },
              "opptjeningAktivitet": {
                "frilanser": {
                  "startdato": "2015-01-01",
                  "sluttdato": "2021-01-01"
                }
              },
              "trekkKravPerioder": [],
              "dataBruktTilUtledning": null
            },
            "journalposter": [],
            "begrunnelseForInnsending": {
              "tekst": null
            },
            "versjon": "1.0.0"
          }
        }
        """.trimIndent()

    private fun String.assertDittnavVarsel(k9Beskjed: K9Beskjed) {
        val k9BeskjedJson = JSONObject(this)
        Assertions.assertEquals(k9Beskjed.grupperingsId, k9BeskjedJson.getString("grupperingsId"))
        Assertions.assertEquals(k9Beskjed.tekst, k9BeskjedJson.getString("tekst"))
        Assertions.assertEquals(k9Beskjed.ytelse, k9BeskjedJson.getString("ytelse"))
        Assertions.assertEquals(k9Beskjed.dagerSynlig, k9BeskjedJson.getLong("dagerSynlig"))
    }
}
