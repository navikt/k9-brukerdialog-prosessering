package no.nav.brukerdialog.kafka.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.core.io.Resource
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "no.nav.kafka")
@Validated
data class KafkaProperties(
    val bootstrapServers: String,
    val applicationId: String,
    val retries: Int,
    val retryBackoffMs: Int,
    val replicationFactor: Int,
    val producer: KafkaProducerProperties,
    val streams: Map<String, KafkaStreamsProperties>,
    val security: KafkaSecurityProperties? = null,
) {
    fun streamProperties(streamPropertyKey: KafkaStreamName): KafkaStreamsProperties {
        return streams[streamPropertyKey.value] ?: throw IllegalStateException("Mangler konfiguration for ${streamPropertyKey.value} streams")
    }
}

data class KafkaProducerProperties(
    val clientId: String,
    val keySerializer: String,
    val valueSerializer: String,
    val retries: Int,
    val transactionIdPrefix: String
)

data class KafkaStreamsProperties(
    val applicationIdSuffix: String,
    val autoOffsetReset: String
)

data class KafkaSecurityProperties(
    val protocol: String,
    val ssl: KafkaSSlProperties,
)

data class KafkaSSlProperties(
    val keyPassword: String,
    val trustStoreType: String,
    val trustStoreLocation: Resource,
    val trustStorePassword: String,
    val keyStoreLocation: Resource,
    val keyStoreType: String,
    val keyStorePassword: String
)

enum class KafkaStreamName(val value: String) {
    // PSB Søknad
    PSB_SØKNAD_PREPROSESSERING("psb-soknad-preprosessering"),
    PSB_SØKNAD_JOURNALFØRING("psb-soknad-journalforing"),
    PSB_SØKNAD_CLEANUP("psb-soknad-cleanup"),

    // PSB Endringsmelding
    PSB_ENDRINGSMELDING_PREPROSESSERING("psb-endringsmelding-preprosessering"),
    PSB_ENDRINGSMELDING_JOURNALFØRING("psb-endringsmelding-journalforing"),
    PSB_ENDRINGSMELDING_CLEANUP("psb-endringsmelding-cleanup"),


    // PILS Søknad
    PILS_SØKNAD_PREPROSESSERING("pils-soknad-preprosessering"),
    PILS_SØKNAD_JOURNALFØRING("pils-soknad-journalforing"),
    PILS_SØKNAD_CLEANUP("pils-soknad-cleanup"),

    // Ettersendelse
    ETTERSENDELSE_PREPROSESSERING("ettersendelse-preprosessering"),
    ETTERSENDELSE_JOURNALFØRING("ettersendelse-journalforing"),
    ETTERSENDELSE_CLEANUP("ettersendelse-cleanup"),

    // Omsorgspenger utvidet rett - kronisk sykt barn
    OMP_UTV_KS_PREPROSESSERING("omp-utv-ks-preprosessering"),
    OMP_UTV_KS_JOURNALFØRING("omp-utv-ks-journalforing"),
    OMP_UTV_KS_CLEANUP("omp-utv-ks-cleanup"),

    // Omsorgspengerutbetaling - arbeidstaker
    OMP_UTB_AT_PREPROSESSERING("omp-utb-at-preprosessering"),
    OMP_UTB_AT_JOURNALFØRING("omp-utb-at-journalforing"),
    OMP_UTB_AT_CLEANUP("omp-utb-at-cleanup"),

    // Omsorgspengerutbetaling - selvstendig næringsdrivende og frilanser
    OMP_UTB_SNF_PREPROSESSERING("omp-utb-snf-preprosessering"),
    OMP_UTB_SNF_JOURNALFØRING("omp-utb-snf-journalforing"),
    OMP_UTB_SNF_CLEANUP("omp-utb-snf-cleanup"),

    // Omsorgspenger - midlertidig alene
    OMP_MA_PREPROSESSERING("omp-ma-preprosessering"),
    OMP_MA_JOURNALFØRING("omp-ma-journalforing"),
    OMP_MA_CLEANUP("omp-ma-cleanup"),

    // Omsorgspenger - alene omsorg
    OMP_AO_PREPROSESSERING("omp-ao-preprosessering"),
    OMP_AO_JOURNALFØRING("omp-ao-journalforing"),
    OMP_AO_CLEANUP("omp-ao-cleanup"),

    // Ungdomsytelse
    UNGDOMSYTELSE_SØKNAD_PREPROSESSERING("ungdomsytelse-soknad-preprosessering"),
    UNGDOMSYTELSE_SØKNAD_JOURNALFØRING("ungdomsytelse-soknad-journalforing"),
    UNGDOMSYTELSE_SØKNAD_CLEANUP("ungdomsytelse-soknad-cleanup"),

    // Ungdomsytelse inntektsrapportering
    UNGDOMSYTELSE_INNTEKTSRAPPORTERING_PREPROSESSERING("ungdomsytelse-inntektsrapportering-preprosessering"),
    UNGDOMSYTELSE_INNTEKTSRAPPORTERING_JOURNALFØRING("ungdomsytelse-inntektsrapportering-journalforing"),
    UNGDOMSYTELSE_INNTEKTSRAPPORTERING_CLEANUP("ungdomsytelse-inntektsrapportering-cleanup"),

    // Ungdomsytelse oppgavebekreftelse
    UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_PREPROSESSERING("ungdomsytelse-oppgavebekreftelse-preprosessering"),
    UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_JOURNALFØRING("ungdomsytelse-oppgavebekreftelse-journalforing"),
    UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_CLEANUP("ungdomsytelse-oppgavebekreftelse-cleanup"),

    // Opplæringspenger
    OPPLÆRINGSPENGER_SØKNAD_PREPROSESSERING("olp-soknad-preprosessering"),
    OPPLÆRINGSPENGER_SØKNAD_JOURNALFØRING("olp-soknad-journalforing"),
    OPPLÆRINGSPENGER_SØKNAD_CLEANUP("olp-soknad-cleanup"),
}
