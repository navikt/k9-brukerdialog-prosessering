package no.nav.k9brukerdialogprosessering.common

import no.nav.k9brukerdialogprosessering.meldinger.endringsmelding.PSBEndringsmeldingTopologyConfiguration.Companion.PSB_ENDRINGSMELDING_CLEANUP_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.endringsmelding.PSBEndringsmeldingTopologyConfiguration.Companion.PSB_ENDRINGSMELDING_MOTTATT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.endringsmelding.PSBEndringsmeldingTopologyConfiguration.Companion.PSB_ENDRINGSMELDING_PREPROSESSERT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.PSBTopologyConfiguration.Companion.PSB_CLEANUP_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.PSBTopologyConfiguration.Companion.PSB_MOTTATT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.PSBTopologyConfiguration.Companion.PSB_PREPROSESSERT_TOPIC

enum class Ytelse {
    OMSORGSPENGER_UTVIDET_RETT,
    OMSORGSPENGER_MIDLERTIDIG_ALENE,
    ETTERSENDING,
    OMSORGSDAGER_ALENEOMSORG,
    OMSORGSPENGER_UTBETALING_ARBEIDSTAKER,
    OMSORGSPENGER_UTBETALING_SNF,
    PLEIEPENGER_LIVETS_SLUTTFASE,
    ETTERSENDING_PLEIEPENGER_SYKT_BARN,
    ETTERSENDING_PLEIEPENGER_LIVETS_SLUTTFASE,
    ETTERSENDING_OMP,
    PLEIEPENGER_SYKT_BARN,
    PLEIEPENGER_SYKT_BARN_ENDRINGSMELDING;

    companion object {
        fun fraTopic(topic: String): Ytelse = when (topic) {
            PSB_MOTTATT_TOPIC, PSB_PREPROSESSERT_TOPIC, PSB_CLEANUP_TOPIC -> PLEIEPENGER_SYKT_BARN
            PSB_ENDRINGSMELDING_MOTTATT_TOPIC, PSB_ENDRINGSMELDING_PREPROSESSERT_TOPIC, PSB_ENDRINGSMELDING_CLEANUP_TOPIC -> PLEIEPENGER_SYKT_BARN_ENDRINGSMELDING
            else -> {
                throw IllegalArgumentException("Kan ikke finne ytelse for topic: $topic")
            }
        }
    }
}
