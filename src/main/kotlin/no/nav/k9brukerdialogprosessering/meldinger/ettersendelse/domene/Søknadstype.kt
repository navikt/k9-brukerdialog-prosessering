package no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.domene

enum class Søknadstype(val pdfNavn: String){
    PLEIEPENGER_SYKT_BARN("Pleiepenger sykt barn"),
    PLEIEPENGER_LIVETS_SLUTTFASE("Pleiepenger i livets sluttfase"),
    OMP_UTV_KS("Ekstra omsorgsdager"), // Omsorgspenger utvidet rett - kronisk syke eller funksjonshemming.
    OMP_UT_SNF("Omsorgspenger utbetaling selvstendig/frilanser"), // Omsorgspenger utbetaling SNF ytelse.
    OMP_UT_ARBEIDSTAKER("Omsorgspenger utbetaling arbeidstaker"), // Omsorgspenger utbetaling arbeidstaker ytelse.
    OMP_UTV_MA("Omsorgspenger regnet som alene"), // Omsorgspenger utvidet rett - midlertidig alene
    OMP_DELE_DAGER("Melding om deling av omsorgsdager")
}
