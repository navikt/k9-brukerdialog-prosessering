package no.nav.brukerdialog.kafka

import no.nav.brukerdialog.ytelse.Ytelse
import no.nav.brukerdialog.ytelse.Ytelse.DINE_PLEIEPENGER
import no.nav.brukerdialog.ytelse.Ytelse.ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN
import no.nav.brukerdialog.ytelse.Ytelse.ETTERSENDING
import no.nav.brukerdialog.ytelse.Ytelse.ETTERSENDING_OMP
import no.nav.brukerdialog.ytelse.Ytelse.ETTERSENDING_PLEIEPENGER_LIVETS_SLUTTFASE
import no.nav.brukerdialog.ytelse.Ytelse.ETTERSENDING_PLEIEPENGER_SYKT_BARN
import no.nav.brukerdialog.ytelse.Ytelse.OMSORGSDAGER_ALENEOMSORG
import no.nav.brukerdialog.ytelse.Ytelse.OMSORGSPENGER_MIDLERTIDIG_ALENE
import no.nav.brukerdialog.ytelse.Ytelse.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER
import no.nav.brukerdialog.ytelse.Ytelse.OMSORGSPENGER_UTBETALING_SNF
import no.nav.brukerdialog.ytelse.Ytelse.OMSORGSPENGER_UTVIDET_RETT
import no.nav.brukerdialog.ytelse.Ytelse.PLEIEPENGER_LIVETS_SLUTTFASE
import no.nav.brukerdialog.ytelse.Ytelse.PLEIEPENGER_SYKT_BARN
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.kafka.Topics.ETTERSENDING_TOPIC
import no.nav.brukerdialog.kafka.Topics.MOTTATT_ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN_TOPIC
import no.nav.brukerdialog.kafka.Topics.OMSORGSDAGER_ALENEOMSORG_TOPIC
import no.nav.brukerdialog.kafka.Topics.OMSORGSPENGER_MIDLERTIDIG_ALENE_TOPIC
import no.nav.brukerdialog.kafka.Topics.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER_TOPIC
import no.nav.brukerdialog.kafka.Topics.OMSORGSPENGER_UTBETALING_SNF_TOPIC
import no.nav.brukerdialog.kafka.Topics.OMSORGSPENGER_UTVIDET_RETT_TOPIC
import no.nav.brukerdialog.kafka.Topics.PLEIEPENGER_LIVETS_SLUTTFASE_TOPIC
import no.nav.brukerdialog.kafka.Topics.PLEIEPENGER_SYKT_BARN_TOPIC
import no.nav.brukerdialog.kafka.Topics.UNGDOMSYTELSE_SOKNAD_TOPIC
import org.apache.kafka.common.serialization.Serializer
import org.json.JSONObject

object Topics {
    const val OMSORGSPENGER_UTVIDET_RETT_TOPIC = "dusseldorf.omp-utv-kronisk-sykt-barn-soknad-mottatt"
    const val OMSORGSPENGER_MIDLERTIDIG_ALENE_TOPIC = "dusseldorf.omp-ma-soknad-mottatt"
    const val ETTERSENDING_TOPIC = "dusseldorf.ettersendelse-mottatt"
    const val OMSORGSDAGER_ALENEOMSORG_TOPIC = "dusseldorf.omp-ao-soknad-mottatt"
    const val OMSORGSPENGER_UTBETALING_ARBEIDSTAKER_TOPIC = "dusseldorf.omp-utbetaling-at-soknad-mottatt"
    const val OMSORGSPENGER_UTBETALING_SNF_TOPIC = "dusseldorf.omp-utbetaling-snf-soknad-mottatt"
    const val PLEIEPENGER_LIVETS_SLUTTFASE_TOPIC = "dusseldorf.pp-i-livets-sluttfase-soknad-mottatt"
    const val PLEIEPENGER_SYKT_BARN_TOPIC = "dusseldorf.pp-sykt-barn-soknad-mottatt"
    const val MOTTATT_ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN_TOPIC = "dusseldorf.privat-endringsmelding-pleiepenger-sykt-barn-mottatt"
    const val UNGDOMSYTELSE_SOKNAD_TOPIC = "dusseldorf.ungdomsytelse-soknad-mottatt"
}

data class TopicEntry<V>(
    val metadata: MetaInfo,
    val data: V,
)

internal fun hentTopicForYtelse(ytelse: Ytelse) = when (ytelse) {
    PLEIEPENGER_LIVETS_SLUTTFASE -> PLEIEPENGER_LIVETS_SLUTTFASE_TOPIC
    OMSORGSPENGER_UTVIDET_RETT -> OMSORGSPENGER_UTVIDET_RETT_TOPIC
    OMSORGSPENGER_MIDLERTIDIG_ALENE -> OMSORGSPENGER_MIDLERTIDIG_ALENE_TOPIC
    ETTERSENDING, ETTERSENDING_OMP, ETTERSENDING_PLEIEPENGER_LIVETS_SLUTTFASE, ETTERSENDING_PLEIEPENGER_SYKT_BARN -> ETTERSENDING_TOPIC
    OMSORGSDAGER_ALENEOMSORG -> OMSORGSDAGER_ALENEOMSORG_TOPIC
    OMSORGSPENGER_UTBETALING_ARBEIDSTAKER -> OMSORGSPENGER_UTBETALING_ARBEIDSTAKER_TOPIC
    OMSORGSPENGER_UTBETALING_SNF -> OMSORGSPENGER_UTBETALING_SNF_TOPIC
    PLEIEPENGER_SYKT_BARN -> PLEIEPENGER_SYKT_BARN_TOPIC
    ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN -> MOTTATT_ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN_TOPIC
    DINE_PLEIEPENGER -> throw IllegalArgumentException("$ytelse er ikke en gyldig ytelse for denne operasjonen")
    Ytelse.UNGDOMSYTELSE -> UNGDOMSYTELSE_SOKNAD_TOPIC
    Ytelse.OPPLARINGSPENGER -> TODO("Må opprette topic. Se nais/topics i rot av repo")
}

internal class SøknadSerializer : Serializer<TopicEntry<JSONObject>> {
    override fun serialize(topic: String, data: TopicEntry<JSONObject>): ByteArray {
        val metadata = JSONObject()
            .put("correlationId", data.metadata.correlationId)
            .put("version", data.metadata.version)
            .put("soknadDialogCommitSha", data.metadata.soknadDialogCommitSha)

        return JSONObject()
            .put("metadata", metadata)
            .put("data", data.data)
            .toString()
            .toByteArray()
    }
}
