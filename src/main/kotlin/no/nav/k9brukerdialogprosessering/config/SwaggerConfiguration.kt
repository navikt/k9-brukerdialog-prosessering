package no.nav.k9brukerdialogprosessering.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.EnvironmentAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.HttpHeaders


@Configuration
class SwaggerConfiguration : EnvironmentAware {
    private var env: Environment? = null

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("K9 Brukerdialog API")
                    .description("API spesifikasjon for K9 Brukerdialog prosessering")
                    .version("v1.0.0")
            )
            .externalDocs(
                ExternalDocumentation()
                    .description("K9 Brukerdialog prosessering GitHub repository")
                    .url("https://github.com/navikt/k9-brukerdialog-prosessering")
            )
            .components(
                Components()
                    .addSecuritySchemes("Authorization", tokenXApiToken())
            )
            .addSecurityItem(
                SecurityRequirement()
                    .addList("Authorization")
            )
    }

    private fun tokenXApiToken(): SecurityScheme {
        return SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .name(HttpHeaders.AUTHORIZATION)
            .scheme("bearer")
            .bearerFormat("JWT")
            .`in`(SecurityScheme.In.HEADER)
            .description(
                """Eksempel på verdi som skal inn i Value-feltet (Bearer trengs altså ikke å oppgis): 'eyAidH...'
                For nytt token -> https://tokenx-token-generator.intern.dev.nav.no/api/obo?aud=dev-gcp:dusseldorf:k9-brukerdialog-prosessering
            """.trimMargin()
            )
    }

    override fun setEnvironment(env: Environment) {
        this.env = env
    }

    companion object {
        // language=json
        const val SAKER_RESPONSE_EKSEMPEL = """
                    [
                      {
                        "pleietrengende": {
                          "fødselsdato": "2000-01-01",
                          "fornavn": "Ola",
                          "mellomnavn": null,
                          "etternavn": "Nordmann",
                          "aktørId": "11111111111",
                          "identitetsnummer": "1234567890"
                        },
                        "sak": {
                          "saksnummer": "ABC123",
                          "utledetStatus": {
                            "status": "OPPRETTET",
                            "aksjonspunkter": [
                              {
                                "venteårsak": "INNTEKTSMELDING",
                                "tidsfrist": "2024-01-01"
                              }
                            ],
                            "saksbehandlingsFrist": "2024-01-01"
                          },
                          "saksbehandlingsFrist": "2024-01-01",
                          "fagsakYtelseType": {
                            "kode": "PSB",
                            "kodeverk": "FAGSAK_YTELSE"
                          },
                          "behandlinger": [
                            {
                              "status": "OPPRETTET",
                              "opprettetTidspunkt": "2024-02-06T00:00:00.000Z",
                              "avsluttetTidspunkt": null,
                              "innsendelser": [
                                {
                                  "søknadId": "10ed495f-83f2-46c1-a7bb-58d55fd1b1b2",
                                  "innsendelsestype": "SØKNAD",
                                  "arbeidsgivere": [
                                    {
                                      "organisasjonsnummer": "123456789",
                                      "navn": "Arbeidsgiver AS"
                                    }
                                  ],
                                  "k9FormatInnsendelse": {
                                    "søknadId": "10ed495f-83f2-46c1-a7bb-58d55fd1b1b2",
                                    "versjon": "1.0.0",
                                    "mottattDato": "2024-02-06T14:50:24.318Z",
                                    "søker": {
                                      "norskIdentitetsnummer": "1234567890"
                                    },
                                    "ytelse": {
                                      "type": "PLEIEPENGER_SYKT_BARN",
                                      "barn": {
                                        "norskIdentitetsnummer": "21121879023",
                                        "fødselsdato": null
                                      },
                                      "søknadsperiode": [
                                        "2024-01-01/2024-01-31"
                                      ],
                                      "endringsperiode": [],
                                      "trekkKravPerioder": [],
                                      "opptjeningAktivitet": {},
                                      "dataBruktTilUtledning": null,
                                      "annetDataBruktTilUtledning": null,
                                      "infoFraPunsj": null,
                                      "bosteder": {
                                        "perioder": {},
                                        "perioderSomSkalSlettes": {}
                                      },
                                      "utenlandsopphold": {
                                        "perioder": {},
                                        "perioderSomSkalSlettes": {}
                                      },
                                      "beredskap": {
                                        "perioder": {},
                                        "perioderSomSkalSlettes": {}
                                      },
                                      "nattevåk": {
                                        "perioder": {},
                                        "perioderSomSkalSlettes": {}
                                      },
                                      "tilsynsordning": {
                                        "perioder": {}
                                      },
                                      "lovbestemtFerie": {
                                        "perioder": {}
                                      },
                                      "arbeidstid": {
                                        "arbeidstakerList": [],
                                        "frilanserArbeidstidInfo": null,
                                        "selvstendigNæringsdrivendeArbeidstidInfo": null
                                      },
                                      "uttak": {
                                        "perioder": {}
                                      },
                                      "omsorg": {
                                        "relasjonTilBarnet": null,
                                        "beskrivelseAvOmsorgsrollen": null
                                      }
                                    },
                                    "språk": "nb",
                                    "journalposter": [
                                      {
                                        "inneholderInfomasjonSomIkkeKanPunsjes": null,
                                        "inneholderInformasjonSomIkkeKanPunsjes": null,
                                        "inneholderMedisinskeOpplysninger": null,
                                        "journalpostId": "123456789"
                                      }
                                    ],
                                    "begrunnelseForInnsending": {
                                      "tekst": null
                                    },
                                    "kildesystem": null
                                  },
                                  "dokumenter": [
                                    {
                                      "journalpostId": "123456789",
                                      "dokumentInfoId": "123456789",
                                      "saksnummer": "ABC123",
                                      "tittel": "Søknad om pleiepenger",
                                      "dokumentType": "PLEIEPENGER_SYKT_BARN_SOKNAD",
                                      "filtype": "PDFA",
                                      "harTilgang": true,
                                      "url": "http://localhost:8080/saker/123456789",
                                      "relevanteDatoer": [
                                        {
                                          "dato": "2024-02-06T14:50:24.318Z",
                                          "datotype": "DATO_OPPRETTET"
                                        }
                                      ]
                                    }
                                  ]
                                },
                                {
                                  "søknadId": "e9514b88-ace4-4faa-b894-a9ef66b53e79",
                                  "innsendelsestype": "ETTERSENDELSE",
                                  "k9FormatInnsendelse": {
                                    "søknadId": "e9514b88-ace4-4faa-b894-a9ef66b53e79",
                                    "versjon": "0.0.1",
                                    "mottattDato": "2024-02-06T14:50:24.318Z",
                                    "søker": {
                                      "norskIdentitetsnummer": "1234567890"
                                    },
                                    "ytelse": "PLEIEPENGER_SYKT_BARN",
                                    "pleietrengende": {
                                      "norskIdentitetsnummer": "21121879023"
                                    },
                                    "type": "LEGEERKLÆRING"
                                  },
                                  "dokumenter": [
                                    {
                                      "journalpostId": "123456789",
                                      "dokumentInfoId": "123456789",
                                      "saksnummer": "ABC123",
                                      "tittel": "Ettersendelse for Søknad om pleiepenger",
                                      "dokumentType": "PLEIEPENGER_SYKT_BARN_ETTERSENDELSE",
                                      "filtype": "PDFA",
                                      "harTilgang": true,
                                      "url": "http://localhost:8080/saker/123456789",
                                      "relevanteDatoer": [
                                        {
                                          "dato": "2024-02-06T14:50:24.318Z",
                                          "datotype": "DATO_OPPRETTET"
                                        }
                                      ]
                                    }
                                  ],
                                  "arbeidsgivere": null
                                }
                              ],
                               "utgåendeDokumenter": [
                                {
                                  "journalpostId": "123456789",
                                  "dokumentInfoId": "123456789",
                                  "saksnummer": "ABC123",
                                  "tittel": "Etterlysning av inntektsmelding",
                                  "dokumentType": "ETTERLYST_INNTEKTSMELDING",
                                  "filtype": "PDFA",
                                  "harTilgang": true,
                                  "url": "http://localhost:8080/saker/123456789",
                                  "relevanteDatoer": [
                                    {
                                      "dato": "2024-02-06T14:50:24.318Z",
                                      "datotype": "DATO_OPPRETTET"
                                    }
                                  ]
                                }
                              ],
                              "aksjonspunkter": [
                                {
                                  "venteårsak": "INNTEKTSMELDING"
                                }
                              ]
                            }
                          ]
                        }
                      }
                    ]
                """
    }
}
