package no.nav.k9brukerdialogapi.oppslag.arbeidsgiver

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class ArbeidsgivereOppslagRespons (
    val arbeidsgivere: Arbeidsgivere
)

data class Arbeidsgivere (
    val organisasjoner: List<Organisasjon>,
    @JsonProperty("private_arbeidsgivere") val privateArbeidsgivere: List<PrivatArbeidsgiver>?,
    val frilansoppdrag: List<Frilansoppdrag>?
)

class Organisasjon (
    val organisasjonsnummer: String,
    val navn: String?,
    @JsonProperty("ansatt_fom") val ansattFom: LocalDate? = null,
    @JsonProperty("ansatt_tom") val ansattTom: LocalDate? = null
)

data class PrivatArbeidsgiver (
    @JsonProperty("offentlig_ident") val offentligIdent: String,
    @JsonProperty("ansatt_fom") val ansattFom: LocalDate? = null,
    @JsonProperty("ansatt_tom") val ansattTom: LocalDate? = null
)

data class Frilansoppdrag (
    val type: String,
    val organisasjonsnummer: String? = null,
    val navn: String? = null,
    @JsonProperty("offentlig_ident") val offentligIdent: String? = null,
    @JsonProperty("ansatt_fom") val ansattFom: LocalDate? = null,
    @JsonProperty("ansatt_tom") val ansattTom: LocalDate? = null
)
