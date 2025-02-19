package no.nav.brukerdialog.integrasjon.dokarkiv.dto

import no.nav.helse.journalforing.Tema

enum class YtelseType(
    val brevkode: BrevKode,
    val tittel: String,
    val tema: Tema,
    val innsendingstype: Innsendingstype,
) {
    PLEIEPENGESØKNAD(
        brevkode = BrevKode(brevKode = "NAV 09-11.05", dokumentKategori = "SOK"),
        tittel = "Søknad om pleiepenger – sykt barn - NAV 09-11.05",
        tema = Tema.K9_YTELSER,
        innsendingstype = Innsendingstype.SØKNAD
    ),

    PLEIEPENGESØKNAD_ENDRINGSMELDING(
        brevkode = BrevKode(brevKode = "NAV 09-11.05", dokumentKategori = "SOK"),
        tittel = "Søknad om pleiepenger – sykt barn - NAV 09-11.05",
        tema = Tema.K9_YTELSER,
        innsendingstype = Innsendingstype.ENDRING
    ),

    PLEIEPENGESØKNAD_ETTERSENDING(
        brevkode = BrevKode(brevKode = "NAVe 09-11.05", dokumentKategori = "SOK"),
        tittel = "Søknad om pleiepenger – sykt barn - NAVe 09-11.05",
        tema = Tema.K9_YTELSER,
        innsendingstype = Innsendingstype.ETTERSENDELSE
    ),

    PLEIEPENGESØKNAD_LIVETS_SLUTTFASE(
        brevkode = BrevKode(brevKode = "NAV 09-12.05", dokumentKategori = "SOK"),
        tittel = "Søknad om pleiepenger ved pleie i hjemmet av nærstående i livets sluttfase - NAV 09-12.05",
        tema = Tema.K9_YTELSER,
        innsendingstype = Innsendingstype.SØKNAD
    ),

    PLEIEPENGESØKNAD_LIVETS_SLUTTFASE_ETTERSENDING(
        brevkode = BrevKode(brevKode = "NAVe 09-12.05", dokumentKategori = "SOK"),
        tittel = "Søknad om pleiepenger ved pleie i hjemmet av nærstående i livets sluttfase - NAVe 09-12.05",
        tema = Tema.K9_YTELSER,
        innsendingstype = Innsendingstype.ETTERSENDELSE
    ),

    OMSORGSPENGESØKNAD(
        brevkode = BrevKode(brevKode = "NAV 09-06.05", dokumentKategori = "SOK"),
        tittel = "Søknad om flere omsorgsdager - NAV 09-06.05",
        tema = Tema.K9_YTELSER,
        innsendingstype = Innsendingstype.SØKNAD
    ),

    OMSORGSPENGESØKNAD_ETTERSENDING(
        brevkode = BrevKode(brevKode = "NAVe 09-06.05", dokumentKategori = "SOK"),
        tittel = "Søknad om flere omsorgsdager - NAVe 09-06.05",
        tema = Tema.K9_YTELSER,
        innsendingstype = Innsendingstype.ETTERSENDELSE
    ),

    OMSORGSPENGESØKNAD_UTBETALING_FRILANSER_SELVSTENDIG(
        brevkode = BrevKode(brevKode = "NAV 09-35.01", dokumentKategori = "SOK"), // TODO: Riktig kode er: NAV 09-09.03
        tittel = "Søknad om utbetaling av omsorgsdager frilanser/selvstendig - NAV 09-35.01",
        tema = Tema.K9_YTELSER,
        innsendingstype = Innsendingstype.SØKNAD
    ),

    OMSORGSPENGESØKNAD_UTBETALING_FRILANSER_SELVSTENDIG_ETTERSENDING(
        brevkode = BrevKode(
            brevKode = "NAVe 09-35.01",
            dokumentKategori = "SOK"
        ), // TODO: Riktig kode er: NAVe 09-09.03
        tittel = "Søknad om utbetaling av omsorgsdager frilanser/selvstendig - NAVe 09-35.01",
        tema = Tema.K9_YTELSER,
        innsendingstype = Innsendingstype.ETTERSENDELSE
    ),

    OMSORGSPENGESØKNAD_UTBETALING_ARBEIDSTAKER(
        brevkode = BrevKode(brevKode = "NAV 09-35.02", dokumentKategori = "SOK"), // TODO: Riktig kode er: NAV 09-09.01
        tittel = "Søknad om utbetaling av omsorgspenger for arbeidstakere - NAV 09-35.02",
        tema = Tema.K9_YTELSER,
        innsendingstype = Innsendingstype.SØKNAD
    ),

    OMSORGSPENGESØKNAD_UTBETALING_ARBEIDSTAKER_ETTERSENDING(
        brevkode = BrevKode(
            brevKode = "NAVe 09-35.02",
            dokumentKategori = "SOK"
        ), // TODO: Riktig kode er: NAVe 09-09.01
        tittel = "Søknad om utbetaling av omsorgspenger for arbeidstakere - NAVe 09-35.02",
        tema = Tema.K9_YTELSER,
        innsendingstype = Innsendingstype.ETTERSENDELSE
    ),

    OMSORGSPENGESØKNAD_OVERFØRING_AV_DAGER(
        brevkode = BrevKode(brevKode = "NAV 09-06.08", dokumentKategori = "SOK"),
        tittel = "Søknad om overføring av omsorgsdager - NAV 09-06.08",
        tema = Tema.K9_YTELSER,
        innsendingstype = Innsendingstype.MELDING
    ),

    OMSORGSPENGEMELDING_DELING_AV_DAGER(
        brevkode = BrevKode(brevKode = "NAV 09-06.08", dokumentKategori = "SOK"),
        tittel = "Melding om deling av omsorgsdager - NAV 09-06.08",
        tema = Tema.K9_YTELSER,
        innsendingstype = Innsendingstype.MELDING
    ),

    OMSORGSPENGEMELDING_DELING_AV_DAGER_ETTERSENDING(
        brevkode = BrevKode(brevKode = "NAVe 09-06.08", dokumentKategori = "SOK"),
        tittel = "Melding om deling av omsorgsdager - NAVe 09-06.08",
        tema = Tema.K9_YTELSER,
        innsendingstype = Innsendingstype.ETTERSENDELSE
    ),

    OMSORGSPENGESØKNAD_MIDLERTIDIG_ALENE(
        brevkode = BrevKode(brevKode = "NAV 09-06.07", dokumentKategori = "SOK"),
        tittel = "Søknad om ekstra omsorgsdager når den andre forelderen ikke kan ha tilsyn med barn - NAV 09-06.07",
        tema = Tema.K9_YTELSER,
        innsendingstype = Innsendingstype.SØKNAD
    ),

    OMSORGSPENGESØKNAD_MIDLERTIDIG_ALENE_ETTERSENDING(
        brevkode = BrevKode(brevKode = "NAVe 09-06.07", dokumentKategori = "SOK"),
        tittel = "Søknad om å bli regnet som alene  - NAVe 09-06.07",
        tema = Tema.K9_YTELSER,
        innsendingstype = Innsendingstype.ETTERSENDELSE
    ),

    OMSORGSDAGER_ALENEOMSORG(
        brevkode = BrevKode(brevKode = "NAV 09-06.10", dokumentKategori = "SOK"),
        tittel = "Registrering av aleneomsorg for omsorgsdager - NAV 09-06.10",
        tema = Tema.K9_YTELSER,
        innsendingstype = Innsendingstype.MELDING
    ),

    OMSORGSDAGER_ALENEOMSORG_ETTERSENDING(
        brevkode = BrevKode(brevKode = "NAVe 09-06.10", dokumentKategori = "SOK"),
        tittel = "Registrering av aleneomsorg for omsorgsdager - NAVe 09-06.10",
        tema = Tema.K9_YTELSER,
        innsendingstype = Innsendingstype.ETTERSENDELSE
    ),

    OPPLÆRINGSPENGERSØKNAD(
        brevkode = BrevKode(brevKode = "NAV 09-11.08", dokumentKategori = "SOK"),
        tittel = "Søknad om opplæringspenger - NAV 09-11.08",
        tema = Tema.K9_YTELSER,
        innsendingstype = Innsendingstype.SØKNAD
    ),

    OPPLÆRINGSPENGERSØKNAD_ETTERSENDING(
        brevkode = BrevKode(brevKode = "NAVe 09-11.08", dokumentKategori = "SOK"),
        tittel = "Søknad om opplæringspenger - NAVe 09-11.08",
        tema = Tema.K9_YTELSER,
        innsendingstype = Innsendingstype.ETTERSENDELSE
    ),

    FRISINNSØKNAD(
        brevkode = BrevKode(brevKode = "NAV 00-03.02", dokumentKategori = "SOK"),
        tittel = "Søknad om inntektskompensasjon for frilansere og selvstendig næringdrivende - NAV 00-03.02",
        tema = Tema.FRISINN,
        innsendingstype = Innsendingstype.SØKNAD
    ),

    UNGDOMSYTELSE_SØKNAD(
        brevkode = BrevKode(brevKode = "UNG Søknad", dokumentKategori = "SOK"),
        tittel = "Søknad om ungdomsytelse - UNG Søknad",
        tema = Tema.K9_YTELSER, // TODO Bruk Tema.UNGDOMSYTELSE før lansering
        innsendingstype = Innsendingstype.SØKNAD
    ),

    UNGDOMSYTELSE_INNTEKTRAPPORTERING(
        brevkode = BrevKode(brevKode = "UNG Inntektrapportering", dokumentKategori = "SOK"),
        tittel = "Rapporteringsmelding for ungdomsytelsen - UNG Inntektrapportering",
        tema = Tema.K9_YTELSER, // TODO Bruk Tema.UNGDOMSYTELSE før lansering
        innsendingstype = Innsendingstype.SØKNAD
    )
    ;

    init {
        if (Innsendingstype.ETTERSENDELSE == innsendingstype) {
            require(brevkode.brevKode.startsWith("NAVe")) {
                "Ettersendelser skal starte med NAVe. Ugyldig brevkode for ettersendelser ${brevkode.brevKode}"
            }
        }
        if (brevkode.brevKode.startsWith("NAVe")) {
            require(Innsendingstype.ETTERSENDELSE == innsendingstype) {
                "Innsendingstype ${innsendingstype.name} kan ikke ha brevkode for ettersendelser ${brevkode.brevKode}"
            }
        }
    }

    internal companion object {
        private val norskeBokstaver = "[ÆØÅ]".toRegex()
        private fun YtelseType.envKey() =
            "ENABLE_${name.replace(norskeBokstaver, "")}"

        internal fun enabled(env: Map<String, String> = System.getenv()) =
            entries.associateWith {
                val envKey = it.envKey()
                val value = env[envKey]?.equals("true") ?: true
                value
            }
    }
}
