package no.nav.k9brukerdialogprosessering.utils

import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import jakarta.validation.Validator
import no.nav.k9brukerdialogprosessering.K9brukerdialogprosesseringApplication
import no.nav.k9brukerdialogprosessering.dittnavvarsel.DittnavVarselTopologyConfiguration.Companion.K9_DITTNAV_VARSEL_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.endringsmelding.PSBEndringsmeldingTopologyConfiguration.Companion.PSB_ENDRINGSMELDING_CLEANUP_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.endringsmelding.PSBEndringsmeldingTopologyConfiguration.Companion.PSB_ENDRINGSMELDING_MOTTATT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.endringsmelding.PSBEndringsmeldingTopologyConfiguration.Companion.PSB_ENDRINGSMELDING_PREPROSESSERT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.EttersendelseTopologyConfiguration.Companion.ETTERSENDELSE_CLEANUP_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.EttersendelseTopologyConfiguration.Companion.ETTERSENDELSE_MOTTATT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.EttersendelseTopologyConfiguration.Companion.ETTERSENDELSE_PREPROSESSERT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingat.OMPUtbetalingATTopologyConfiguration.Companion.OMP_UTB_AT_CLEANUP_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingat.OMPUtbetalingATTopologyConfiguration.Companion.OMP_UTB_AT_MOTTATT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingat.OMPUtbetalingATTopologyConfiguration.Companion.OMP_UTB_AT_PREPROSESSERT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.OMPUtbetalingSNFTopologyConfiguration.Companion.OMP_UTB_SNF_CLEANUP_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.OMPUtbetalingSNFTopologyConfiguration.Companion.OMP_UTB_SNF_MOTTATT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.OMPUtbetalingSNFTopologyConfiguration.Companion.OMP_UTB_SNF_PREPROSESSERT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengeraleneomsorg.OMPAleneomsorgTopologyConfiguration.Companion.OMP_AO_CLEANUP_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengeraleneomsorg.OMPAleneomsorgTopologyConfiguration.Companion.OMP_AO_MOTTATT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengeraleneomsorg.OMPAleneomsorgTopologyConfiguration.Companion.OMP_AO_PREPROSESSERT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn.OMPKSTopologyConfiguration.Companion.OMP_UTV_KS_SØKNAD_CLEANUP_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn.OMPKSTopologyConfiguration.Companion.OMP_UTV_KS_SØKNAD_MOTTATT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn.OMPKSTopologyConfiguration.Companion.OMP_UTV_KS_SØKNAD_PREPROSESSERT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengermidlertidigalene.OMPMidlertidigAleneTopologyConfiguration.Companion.OMP_MA_CLEANUP_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengermidlertidigalene.OMPMidlertidigAleneTopologyConfiguration.Companion.OMP_MA_MOTTATT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengermidlertidigalene.OMPMidlertidigAleneTopologyConfiguration.Companion.OMP_MA_PREPROSESSERT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.PILSTopologyConfiguration.Companion.PILS_CLEANUP_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.PILSTopologyConfiguration.Companion.PILS_MOTTATT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.PILSTopologyConfiguration.Companion.PILS_PREPROSESSERT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.PSBTopologyConfiguration.Companion.PSB_CLEANUP_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.PSBTopologyConfiguration.Companion.PSB_MOTTATT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.PSBTopologyConfiguration.Companion.PSB_PREPROSESSERT_TOPIC
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

/**
 * Annotates a test class to run with embedded Kafka and Spring Boot.
 * The embedded Kafka will be started with 3 brokers and 1 partition.
 * The topics specified in the annotation will be created.
 * The bootstrap servers property will be set to the value of the KAFKA_BROKERS environment variable.
 * The test class will be started with the test profile.
 * The test class will be started with a random port.
 * The test class will be started with the SpringExtension.
 * The test class will be started with the DirtiesContext annotation.
 * The test class will be started with the TestInstance annotation.
 * The test class will be started with the EnableMockOAuth2Server annotation.
 * The test class will be started with the K9brukerdialogprosesseringApplication class.
 * The test class will be started with the SpringBootTest annotation.
 *
 * @see EmbeddedKafka
 * @see SpringBootTest
 * @see EnableMockOAuth2Server
 * @see DirtiesContext
 * @see TestInstance
 * @see SpringExtension
 * @see K9brukerdialogprosesseringApplication
 * @see ActiveProfiles
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@EmbeddedKafka(
    partitions = 1,
    count = 3,
    bootstrapServersProperty = "KAFKA_BROKERS",
    topics = [
        // Endringsmelding - Pleiepenger sykt barn
        PSB_ENDRINGSMELDING_MOTTATT_TOPIC,
        PSB_ENDRINGSMELDING_PREPROSESSERT_TOPIC,
        PSB_ENDRINGSMELDING_CLEANUP_TOPIC,

        // Pleiepenger sykt barn
        PSB_MOTTATT_TOPIC,
        PSB_PREPROSESSERT_TOPIC,
        PSB_CLEANUP_TOPIC,

        // Pleiepenger i livets sluttfase
        PILS_MOTTATT_TOPIC,
        PILS_PREPROSESSERT_TOPIC,
        PILS_CLEANUP_TOPIC,

        // Ettersendelse
        ETTERSENDELSE_MOTTATT_TOPIC,
        ETTERSENDELSE_PREPROSESSERT_TOPIC,
        ETTERSENDELSE_CLEANUP_TOPIC,

        // Omsorgspenger utvidet rett - kronisk sykt barn
        OMP_UTV_KS_SØKNAD_MOTTATT_TOPIC,
        OMP_UTV_KS_SØKNAD_PREPROSESSERT_TOPIC,
        OMP_UTV_KS_SØKNAD_CLEANUP_TOPIC,

        // Omsorgspengerutbetaling - arbeidstaker
        OMP_UTB_AT_MOTTATT_TOPIC,
        OMP_UTB_AT_PREPROSESSERT_TOPIC,
        OMP_UTB_AT_CLEANUP_TOPIC,

        // Omsorgspengerutbetaling - selvstendig næringsdrivende og frilanser
        OMP_UTB_SNF_MOTTATT_TOPIC,
        OMP_UTB_SNF_PREPROSESSERT_TOPIC,
        OMP_UTB_SNF_CLEANUP_TOPIC,

        // Omsorgspenger - midlertidig alene
        OMP_MA_MOTTATT_TOPIC,
        OMP_MA_PREPROSESSERT_TOPIC,
        OMP_MA_CLEANUP_TOPIC,

        // Omsorgspenger - aleneomsorg
        OMP_AO_MOTTATT_TOPIC,
        OMP_AO_PREPROSESSERT_TOPIC,
        OMP_AO_CLEANUP_TOPIC,

        // K9 Dittnav varsel
        K9_DITTNAV_VARSEL_TOPIC
    ]
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension::class)
@EnableMockOAuth2Server
@ActiveProfiles("test")
@SpringBootTest(
    classes = [K9brukerdialogprosesseringApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
annotation class KafkaIntegrationTest

object TestUtils {
    val Validator: Validator = Validation.buildDefaultValidatorFactory().validator

    fun Validator.verifiserIngenValideringsFeil(objct: Any) = validate(objct).verifiserIngenValideringsFeil()

    fun Validator.verifiserValideringsFeil(data: Any, antallFeil: Int, vararg valideringsfeil: String) =
        validate(data).verifiserValideringsFeil(antallFeil, *valideringsfeil)

    private fun <E> MutableSet<ConstraintViolation<E>>.verifiserValideringsFeil(
        antallFeil: Int,
        vararg valideringsfeil: String,
    ) {
        assertThat(size).isEqualTo(antallFeil)
        assertThat(this.map { it.message }).containsOnly(*valideringsfeil)
    }

    private fun <E> MutableSet<ConstraintViolation<E>>.verifiserIngenValideringsFeil() {
        assertTrue(isEmpty())
    }

    fun List<String>.verifiserValideringsFeil(antallFeil: Int, valideringsfeil: List<String> = listOf()) {
        assertEquals(antallFeil, this.size)
        assertThat(this).contains(*valideringsfeil.toTypedArray())
    }

    fun List<String>.verifiserIngenValideringsFeil() {
        assertTrue(isEmpty())
    }

    internal fun MutableList<String>.assertFeilPå(reason: List<String> = emptyList()) {
        println(this)
        assertEquals(reason.size, size)

        forEach { reason.contains(it) }
    }
}
