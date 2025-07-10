package no.nav.brukerdialog.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders


@Configuration
class SwaggerConfiguration {

    @Bean
    fun ettersendelseApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("ettersendelse")
            .displayName("Ettersendelse API")
            .packagesToScan("no.nav.brukerdialog.ytelse.ettersendelse.api")
            .build()
    }

    @Bean
    fun omsorgspengerUtbetalingAtApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("omsorgspengerutbetaling-arbeidstaker")
            .displayName("Omsorgspengerutbetaling Arbeidstaker API")
            .packagesToScan("no.nav.brukerdialog.ytelse.omsorgpengerutbetalingat.api")
            .build()
    }

    @Bean
    fun omsorgspengerUtbetalingSnfApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("omsorgspengerutbetaling-snf")
            .displayName("Omsorgspengerutbetaling Selvstendig Næringsdrivende / Frilanser API")
            .packagesToScan("no.nav.brukerdialog.ytelse.omsorgpengerutbetalingsnf.api")
            .build()
    }

    @Bean
    fun omsorgspengerAleneomsorgApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("omsorgspenger-aleneomsorg")
            .displayName("Omsorgspenger Aleneomsorg API")
            .packagesToScan("no.nav.brukerdialog.ytelse.omsorgspengeraleneomsorg.api")
            .build()
    }

    @Bean
    fun omsorgspengerKroniskSyktBarnApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("omsorgspenger-kronisk-sykt-barn")
            .displayName("Omsorgspenger Kronisk Sykt Barn API")
            .packagesToScan("no.nav.brukerdialog.ytelse.omsorgspengerkronisksyktbarn.api")
            .build()
    }

    @Bean
    fun omsorgspengerMidlertidigAleneApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("omsorgspenger-midlertidig-alene")
            .displayName("Omsorgspenger Midlertidig Alene API")
            .packagesToScan("no.nav.brukerdialog.ytelse.omsorgspengermidlertidigalene.api")
            .build()
    }

    @Bean
    fun opplaeringspengerApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("opplaeringspenger")
            .displayName("Opplæringspenger API")
            .packagesToScan("no.nav.brukerdialog.ytelse.opplæringspenger.api")
            .build()
    }

    @Bean
    fun pleiepengerLivetsSluttfaseApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("pleiepenger-livets-sluttfase")
            .displayName("Pleiepenger Livets Sluttfase API")
            .packagesToScan("no.nav.brukerdialog.ytelse.pleiepengerilivetssluttfase.api")
            .build()
    }

    @Bean
    fun pleiepengerSyktBarnSoknadApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("pleiepenger-sykt-barn-soknad")
            .displayName("Pleiepenger Sykt Barn Søknad API")
            .packagesToScan("no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api")
            .build()
    }

    @Bean
    fun pleiepengerSyktBarnEndringsmeldingApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("pleiepenger-sykt-barn-endringsmelding")
            .displayName("Pleiepenger Sykt Barn Endringsmelding API")
            .packagesToScan("no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.api")
            .build()
    }

    @Bean
    fun ungdomsytelseApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("ungdomsytelse")
            .displayName("Ungdomsytelse API")
            .packagesToScan("no.nav.brukerdialog.ytelse.ungdomsytelse.api")
            .build()
    }

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
}
