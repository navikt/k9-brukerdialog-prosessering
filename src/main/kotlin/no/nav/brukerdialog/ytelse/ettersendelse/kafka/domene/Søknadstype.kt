package no.nav.brukerdialog.ytelse.ettersendelse.kafka.domene

enum class Søknadstype(val tittel: String) {
    PLEIEPENGER_SYKT_BARN("Ettersendelse av dokumentasjon til søknad om pleiepenger"),
    PLEIEPENGER_LIVETS_SLUTTFASE("Ettersendelse av dokumentasjon til søknad om pleiepenger i livets sluttfase"),
    OMP_UTV_KS("Ettersendelse av dokumentasjon til søknad om ekstra omsorgsdager for barn som har kronisk/langvarig sykdom eller funksjonshemning"),
    OMP_UT_SNF("Ettersendelse av dokumentasjon til søknad om utbetaling av omsorgspenger for selvstendig næringsdrivende og frilansere"),
    OMP_UT_ARBEIDSTAKER("Ettersendelse av dokumentasjon til søknad om utbetaling av omsorgspenger når arbeidsgiver ikke utbetaler"),
    OMP_UTV_MA("Ettersendelse av dokumentasjon til søknad om ekstra omsorgsdager når den andre forelderen ikke kan ha tilsyn med barn"),
    OMP_UTV_AO("Ettersendelse av dokumentasjon til søknad om ekstra omsorgsdager ved aleneomsorg"),
}
