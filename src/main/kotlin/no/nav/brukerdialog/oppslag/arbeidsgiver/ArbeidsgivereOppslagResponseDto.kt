package no.nav.brukerdialog.oppslag.arbeidsgiver

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class ArbeidsgivereOppslagResponsDto (
    val arbeidsgivere: ArbeidsgivereOppslagDto
)

data class ArbeidsgivereOppslagDto (
    val organisasjoner: List<OrganisasjonOppslagDto>,
    val privateArbeidsgivere: List<PrivatArbeidsgiverOppslagDto>?,
    val frilansoppdrag: List<FrilansoppdragOppslagDto>?
)

class OrganisasjonOppslagDto (
    val organisasjonsnummer: String,
    val navn: String?,
    @JsonProperty("ansatt_fom") val ansattFom: LocalDate? = null,
    @JsonProperty("ansatt_tom") val ansattTom: LocalDate? = null
)

data class PrivatArbeidsgiverOppslagDto (
    @JsonProperty("offentlig_ident") val offentligIdent: String,
    @JsonProperty("ansatt_fom") val ansattFom: LocalDate? = null,
    @JsonProperty("ansatt_tom") val ansattTom: LocalDate? = null
)

data class FrilansoppdragOppslagDto (
    val type: String,
    val organisasjonsnummer: String? = null,
    val navn: String? = null,
    @JsonProperty("offentlig_ident") val offentligIdent: String? = null,
    @JsonProperty("ansatt_fom") val ansattFom: LocalDate? = null,
    @JsonProperty("ansatt_tom") val ansattTom: LocalDate? = null
)
