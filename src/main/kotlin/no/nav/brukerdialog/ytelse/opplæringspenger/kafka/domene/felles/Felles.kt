package no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene.felles

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

enum class BarnRelasjon(val utskriftsvennlig: String) {
    MOR("Mor"),
    MEDMOR("Medmor"),
    FAR("Far"),
    FOSTERFORELDER("Fosterforelder"),
    ANNET("Annet")
}

data class Barn(
    val navn : String,
    val norskIdentifikator: String? = null,
    val fødselsdato: LocalDate? = null,
    val aktørId: String? = null, // Brukes av sif-innsyn-api
    val årsakManglerIdentitetsnummer: ÅrsakManglerIdentitetsnummer? = null
    ) {
    override fun toString(): String {
        return "Barn()"
    }
}

enum class ÅrsakManglerIdentitetsnummer(val pdfTekst: String) {
    NYFØDT ("Barnet er nyfødt, og har ikke fått fødselsnummer enda"),
    BARNET_BOR_I_UTLANDET ("Barnet bor i utlandet"),
    ANNET ("Annet")
}

data class Medlemskap(
    val harBoddIUtlandetSiste12Mnd : Boolean,
    val utenlandsoppholdSiste12Mnd: List<Bosted> = listOf(),
    val skalBoIUtlandetNeste12Mnd : Boolean,
    val utenlandsoppholdNeste12Mnd: List<Bosted> = listOf()
)

data class Bosted(
    @JsonFormat(pattern = "yyyy-MM-dd")
    val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val tilOgMed: LocalDate,
    val landkode: String,
    val landnavn: String
) {
    override fun toString(): String {
        return "Utenlandsopphold(fraOgMed=$fraOgMed, tilOgMed=$tilOgMed, landkode='$landkode', landnavn='$landnavn')"
    }
}


enum class Årsak(val beskrivelse: String) {
    BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING("Barnet innlagt i helseinstitusjon for norsk offentlig regning"),
    BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD("Barnet innlagt i helseinstitusjon dekket etter avtale med et annet land om trygd"),
    ANNET("Innleggelsen dekkes av søker selv"),
}

data class FerieuttakIPerioden(
    val skalTaUtFerieIPerioden: Boolean,
    val ferieuttak: List<Ferieuttak>
) {
    override fun toString(): String {
        return "FerieuttakIPerioden(skalTaUtFerieIPerioden=$skalTaUtFerieIPerioden, ferieuttak=$ferieuttak)"
    }
}

data class Ferieuttak(
    @JsonFormat(pattern = "yyyy-MM-dd")
    val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val tilOgMed: LocalDate
) {
    override fun toString(): String {
        return "Ferieuttak(fraOgMed=$fraOgMed, tilOgMed=$tilOgMed)"
    }
}
