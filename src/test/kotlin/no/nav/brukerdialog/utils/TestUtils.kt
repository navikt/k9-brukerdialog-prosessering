package no.nav.brukerdialog.utils

import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import jakarta.validation.Validator
import no.nav.brukerdialog.K9brukerdialogprosesseringApplication
import no.nav.brukerdialog.dittnavvarsel.DittnavVarselTopologyConfiguration.Companion.K9_DITTNAV_VARSEL_TOPIC
import no.nav.brukerdialog.ytelse.ettersendelse.kafka.EttersendelseTopologyConfiguration.Companion.ETTERSENDELSE_CLEANUP_TOPIC
import no.nav.brukerdialog.ytelse.ettersendelse.kafka.EttersendelseTopologyConfiguration.Companion.ETTERSENDELSE_MOTTATT_TOPIC
import no.nav.brukerdialog.ytelse.ettersendelse.kafka.EttersendelseTopologyConfiguration.Companion.ETTERSENDELSE_PREPROSESSERT_TOPIC
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingat.kafka.OMPUtbetalingATTopologyConfiguration.Companion.OMP_UTB_AT_CLEANUP_TOPIC
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingat.kafka.OMPUtbetalingATTopologyConfiguration.Companion.OMP_UTB_AT_MOTTATT_TOPIC
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingat.kafka.OMPUtbetalingATTopologyConfiguration.Companion.OMP_UTB_AT_PREPROSESSERT_TOPIC
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingsnf.kafka.OMPUtbetalingSNFTopologyConfiguration.Companion.OMP_UTB_SNF_CLEANUP_TOPIC
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingsnf.kafka.OMPUtbetalingSNFTopologyConfiguration.Companion.OMP_UTB_SNF_MOTTATT_TOPIC
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingsnf.kafka.OMPUtbetalingSNFTopologyConfiguration.Companion.OMP_UTB_SNF_PREPROSESSERT_TOPIC
import no.nav.brukerdialog.ytelse.omsorgspengeraleneomsorg.kafka.OMPAleneomsorgTopologyConfiguration.Companion.OMP_AO_CLEANUP_TOPIC
import no.nav.brukerdialog.ytelse.omsorgspengeraleneomsorg.kafka.OMPAleneomsorgTopologyConfiguration.Companion.OMP_AO_MOTTATT_TOPIC
import no.nav.brukerdialog.ytelse.omsorgspengeraleneomsorg.kafka.OMPAleneomsorgTopologyConfiguration.Companion.OMP_AO_PREPROSESSERT_TOPIC
import no.nav.brukerdialog.ytelse.omsorgspengerkronisksyktbarn.kafka.OMPKSTopologyConfiguration.Companion.OMP_UTV_KS_SØKNAD_CLEANUP_TOPIC
import no.nav.brukerdialog.ytelse.omsorgspengerkronisksyktbarn.kafka.OMPKSTopologyConfiguration.Companion.OMP_UTV_KS_SØKNAD_MOTTATT_TOPIC
import no.nav.brukerdialog.ytelse.omsorgspengerkronisksyktbarn.kafka.OMPKSTopologyConfiguration.Companion.OMP_UTV_KS_SØKNAD_PREPROSESSERT_TOPIC
import no.nav.brukerdialog.ytelse.omsorgspengermidlertidigalene.kafka.OMPMidlertidigAleneTopologyConfiguration.Companion.OMP_MA_CLEANUP_TOPIC
import no.nav.brukerdialog.ytelse.omsorgspengermidlertidigalene.kafka.OMPMidlertidigAleneTopologyConfiguration.Companion.OMP_MA_MOTTATT_TOPIC
import no.nav.brukerdialog.ytelse.omsorgspengermidlertidigalene.kafka.OMPMidlertidigAleneTopologyConfiguration.Companion.OMP_MA_PREPROSESSERT_TOPIC
import no.nav.brukerdialog.ytelse.opplæringspenger.kafka.OLPTopologyConfiguration.Companion.OLP_CLEANUP_TOPIC
import no.nav.brukerdialog.ytelse.opplæringspenger.kafka.OLPTopologyConfiguration.Companion.OLP_MOTTATT_TOPIC
import no.nav.brukerdialog.ytelse.opplæringspenger.kafka.OLPTopologyConfiguration.Companion.OLP_PREPROSESSERT_TOPIC
import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.kafka.PILSTopologyConfiguration.Companion.PILS_CLEANUP_TOPIC
import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.kafka.PILSTopologyConfiguration.Companion.PILS_MOTTATT_TOPIC
import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.kafka.PILSTopologyConfiguration.Companion.PILS_PREPROSESSERT_TOPIC
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.kafka.PSBEndringsmeldingTopologyConfiguration.Companion.PSB_ENDRINGSMELDING_CLEANUP_TOPIC
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.kafka.PSBEndringsmeldingTopologyConfiguration.Companion.PSB_ENDRINGSMELDING_MOTTATT_TOPIC
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.kafka.PSBEndringsmeldingTopologyConfiguration.Companion.PSB_ENDRINGSMELDING_PREPROSESSERT_TOPIC
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.kafka.PSBTopologyConfiguration.Companion.PSB_CLEANUP_TOPIC
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.kafka.PSBTopologyConfiguration.Companion.PSB_MOTTATT_TOPIC
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.kafka.PSBTopologyConfiguration.Companion.PSB_PREPROSESSERT_TOPIC
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.inntektsrapportering.UngdomsytelseInntektsrapporteringTopologyConfiguration.Companion.UNGDOMSYTELSE_INNTEKTSRAPPORTERING_CLEANUP_TOPIC
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.inntektsrapportering.UngdomsytelseInntektsrapporteringTopologyConfiguration.Companion.UNGDOMSYTELSE_INNTEKTSRAPPORTERING_MOTTATT_TOPIC
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.inntektsrapportering.UngdomsytelseInntektsrapporteringTopologyConfiguration.Companion.UNGDOMSYTELSE_INNTEKTSRAPPORTERING_PREPROSESSERT_TOPIC
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.oppgavebekreftelse.UngdomsytelseOppgavebekreftelseTopologyConfiguration.Companion.UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_CLEANUP_TOPIC
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.oppgavebekreftelse.UngdomsytelseOppgavebekreftelseTopologyConfiguration.Companion.UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_MOTTATT_TOPIC
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.oppgavebekreftelse.UngdomsytelseOppgavebekreftelseTopologyConfiguration.Companion.UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_PREPROSESSERT_TOPIC
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.soknad.UngdomsytelsesøknadTopologyConfiguration.Companion.UNGDOMSYTELSE_SØKNAD_CLEANUP_TOPIC
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.soknad.UngdomsytelsesøknadTopologyConfiguration.Companion.UNGDOMSYTELSE_SØKNAD_MOTTATT_TOPIC
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.soknad.UngdomsytelsesøknadTopologyConfiguration.Companion.UNGDOMSYTELSE_SØKNAD_PREPROSESSERT_TOPIC
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.support.TestPropertySourceUtils
import java.util.concurrent.TimeUnit

val ALL_KAFKA_TOPICS = listOf(
    PSB_ENDRINGSMELDING_MOTTATT_TOPIC, PSB_ENDRINGSMELDING_PREPROSESSERT_TOPIC, PSB_ENDRINGSMELDING_CLEANUP_TOPIC,
    PSB_MOTTATT_TOPIC, PSB_PREPROSESSERT_TOPIC, PSB_CLEANUP_TOPIC,
    PILS_MOTTATT_TOPIC, PILS_PREPROSESSERT_TOPIC, PILS_CLEANUP_TOPIC,
    ETTERSENDELSE_MOTTATT_TOPIC, ETTERSENDELSE_PREPROSESSERT_TOPIC, ETTERSENDELSE_CLEANUP_TOPIC,
    OMP_UTV_KS_SØKNAD_MOTTATT_TOPIC, OMP_UTV_KS_SØKNAD_PREPROSESSERT_TOPIC, OMP_UTV_KS_SØKNAD_CLEANUP_TOPIC,
    OMP_UTB_AT_MOTTATT_TOPIC, OMP_UTB_AT_PREPROSESSERT_TOPIC, OMP_UTB_AT_CLEANUP_TOPIC,
    OMP_UTB_SNF_MOTTATT_TOPIC, OMP_UTB_SNF_PREPROSESSERT_TOPIC, OMP_UTB_SNF_CLEANUP_TOPIC,
    OMP_MA_MOTTATT_TOPIC, OMP_MA_PREPROSESSERT_TOPIC, OMP_MA_CLEANUP_TOPIC,
    OMP_AO_MOTTATT_TOPIC, OMP_AO_PREPROSESSERT_TOPIC, OMP_AO_CLEANUP_TOPIC,
    UNGDOMSYTELSE_SØKNAD_MOTTATT_TOPIC, UNGDOMSYTELSE_SØKNAD_PREPROSESSERT_TOPIC, UNGDOMSYTELSE_SØKNAD_CLEANUP_TOPIC,
    UNGDOMSYTELSE_INNTEKTSRAPPORTERING_MOTTATT_TOPIC, UNGDOMSYTELSE_INNTEKTSRAPPORTERING_PREPROSESSERT_TOPIC, UNGDOMSYTELSE_INNTEKTSRAPPORTERING_CLEANUP_TOPIC,
    UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_MOTTATT_TOPIC, UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_PREPROSESSERT_TOPIC, UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_CLEANUP_TOPIC,
    OLP_MOTTATT_TOPIC, OLP_PREPROSESSERT_TOPIC, OLP_CLEANUP_TOPIC,
    K9_DITTNAV_VARSEL_TOPIC
)

class KafkaContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        val kafkaContainer = TestContainers.KAFKA_CONTAINER
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
            applicationContext,
            "KAFKA_BROKERS=${kafkaContainer.bootstrapServers}"
        )

        AdminClient.create(
            mapOf(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaContainer.bootstrapServers)
        ).use { adminClient ->
            val topics = ALL_KAFKA_TOPICS.map { NewTopic(it, 1, 1.toShort()) }
            try {
                adminClient.createTopics(topics).all().get(30, TimeUnit.SECONDS)
            } catch (e: Exception) {
                // Topics may already exist from previous context (@DirtiesContext)
            }
        }
    }
}

/**
 * Annotates a test class to run with Kafka Testcontainer and Spring Boot.
 * A singleton KafkaContainer is started once and shared across all test classes.
 * Topics are created via KafkaContainerInitializer.
 * Mocks resettes i AbstractIntegrationTest @BeforeEach for å unngå crosstalk mellom testklasser.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@ContextConfiguration(initializers = [KafkaContainerInitializer::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension::class)
@EnableMockOAuth2Server
@ActiveProfiles("test")
@SpringBootTest(
    classes = [K9brukerdialogprosesseringApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Tag("kafka")
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
