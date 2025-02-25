package no.nav.brukerdialog.common

import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.kafka.PSBEndringsmeldingTopologyConfiguration.Companion.PSB_ENDRINGSMELDING_CLEANUP_TOPIC
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.kafka.PSBEndringsmeldingTopologyConfiguration.Companion.PSB_ENDRINGSMELDING_MOTTATT_TOPIC
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.kafka.PSBEndringsmeldingTopologyConfiguration.Companion.PSB_ENDRINGSMELDING_PREPROSESSERT_TOPIC
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
import no.nav.k9.søknad.felles.type.Språk

enum class Ytelse(val tittel: String, val nynorskTittel: String? = null) {
    OMSORGSPENGER_UTVIDET_RETT(
        "Søknad om ekstra omsorgsdager for barn som har kronisk/langvarig sykdom eller funksjonshemning",
        "Søknad om ekstra omsorgsdagar for barn som har kronisk/langvarig sjukdom eller funksjonshemming"
    ),
    OMSORGSPENGER_MIDLERTIDIG_ALENE("Søknad om ekstra omsorgsdager når den andre forelderen ikke kan ha tilsyn med barn"),
    ETTERSENDELSE("Ettersendelse av dokumentasjon"),
    OMSORGSDAGER_ALENEOMSORG("Søknad om ekstra omsorgsdager ved aleneomsorg"),
    OMSORGSPENGER_UTBETALING_ARBEIDSTAKER("Søknad om utbetaling av omsorgspenger når arbeidsgiver ikke utbetaler"),
    OMSORGSPENGER_UTBETALING_SNF("Søknad om utbetaling av omsorgspenger for selvstendig næringsdrivende og frilansere"),
    PLEIEPENGER_LIVETS_SLUTTFASE("Søknad om pleiepenger i livets sluttfase"),
    PLEIEPENGER_SYKT_BARN("Søknad om pleiepenger for sykt barn"),
    PLEIEPENGER_SYKT_BARN_ENDRINGSMELDING("Endringsmelding for pleiepenger sykt barn"),
    UNGDOMSYTELSE_DELTAKELSE_SØKNAD("Søknad om ungdomsytelse"),
    UNGDOMSYTELSE_OPPGAVEBEKREFTELSE("Bekreftelse på løst oppgave for ungdomsytelse"),
    UNGDOMSYTELSE_INNTEKTSRAPPORTERING("Inntektsrapportering for ungdomsytelsen"),
    OPPLÆRINGSPENGER("Søknad om opplæringspenger")
    ;

    fun utledTittel(språk: Språk): String = when (språk) {
        Språk.NORSK_NYNORSK -> nynorskTittel ?: tittel
        else -> tittel
    }

    companion object {
        fun fraTopic(topic: String): Ytelse = when (topic) {
            PSB_MOTTATT_TOPIC, PSB_PREPROSESSERT_TOPIC, PSB_CLEANUP_TOPIC -> PLEIEPENGER_SYKT_BARN
            PSB_ENDRINGSMELDING_MOTTATT_TOPIC, PSB_ENDRINGSMELDING_PREPROSESSERT_TOPIC, PSB_ENDRINGSMELDING_CLEANUP_TOPIC -> PLEIEPENGER_SYKT_BARN_ENDRINGSMELDING
            ETTERSENDELSE_MOTTATT_TOPIC, ETTERSENDELSE_PREPROSESSERT_TOPIC, ETTERSENDELSE_CLEANUP_TOPIC -> ETTERSENDELSE
            OMP_UTV_KS_SØKNAD_MOTTATT_TOPIC, OMP_UTV_KS_SØKNAD_PREPROSESSERT_TOPIC, OMP_UTV_KS_SØKNAD_CLEANUP_TOPIC -> OMSORGSPENGER_UTVIDET_RETT
            PILS_MOTTATT_TOPIC, PILS_PREPROSESSERT_TOPIC, PILS_CLEANUP_TOPIC -> PLEIEPENGER_LIVETS_SLUTTFASE
            OMP_UTB_AT_MOTTATT_TOPIC, OMP_UTB_AT_PREPROSESSERT_TOPIC, OMP_UTB_AT_CLEANUP_TOPIC -> OMSORGSPENGER_UTBETALING_ARBEIDSTAKER
            OMP_UTB_SNF_MOTTATT_TOPIC, OMP_UTB_SNF_PREPROSESSERT_TOPIC, OMP_UTB_SNF_CLEANUP_TOPIC -> OMSORGSPENGER_UTBETALING_SNF
            OMP_MA_MOTTATT_TOPIC, OMP_MA_PREPROSESSERT_TOPIC, OMP_MA_CLEANUP_TOPIC -> OMSORGSPENGER_MIDLERTIDIG_ALENE
            OMP_AO_MOTTATT_TOPIC, OMP_AO_PREPROSESSERT_TOPIC, OMP_AO_CLEANUP_TOPIC -> OMSORGSDAGER_ALENEOMSORG
            UNGDOMSYTELSE_SØKNAD_MOTTATT_TOPIC, UNGDOMSYTELSE_SØKNAD_PREPROSESSERT_TOPIC, UNGDOMSYTELSE_SØKNAD_CLEANUP_TOPIC -> UNGDOMSYTELSE_DELTAKELSE_SØKNAD
            UNGDOMSYTELSE_INNTEKTSRAPPORTERING_MOTTATT_TOPIC, UNGDOMSYTELSE_INNTEKTSRAPPORTERING_PREPROSESSERT_TOPIC, UNGDOMSYTELSE_INNTEKTSRAPPORTERING_CLEANUP_TOPIC -> UNGDOMSYTELSE_INNTEKTSRAPPORTERING
            UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_MOTTATT_TOPIC, UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_PREPROSESSERT_TOPIC, UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_CLEANUP_TOPIC -> UNGDOMSYTELSE_OPPGAVEBEKREFTELSE
            OLP_MOTTATT_TOPIC, OLP_PREPROSESSERT_TOPIC, OLP_CLEANUP_TOPIC -> OPPLÆRINGSPENGER
            else -> {
                throw IllegalArgumentException("Kan ikke finne ytelse for topic: $topic")
            }
        }
    }
}
