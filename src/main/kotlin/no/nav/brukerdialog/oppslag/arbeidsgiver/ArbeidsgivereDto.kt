package no.nav.brukerdialog.oppslag.arbeidsgiver

import java.time.LocalDate

data class ArbeidsgivereDto(
    val organisasjoner: List<OrganisasjonDto>,
    val privateArbeidsgivere: List<PrivatArbeidsgiverDto>?,
    val frilansoppdrag: List<FrilansoppdragDto>?,
)

class OrganisasjonDto(
    val organisasjonsnummer: String,
    val navn: String?,
    val ansattFom: LocalDate? = null,
    val ansattTom: LocalDate? = null,
)

data class PrivatArbeidsgiverDto(
    val offentligIdent: String,
    val ansattFom: LocalDate? = null,
    val ansattTom: LocalDate? = null,
)

data class FrilansoppdragDto(
    val type: String,
    val organisasjonsnummer: String? = null,
    val navn: String? = null,
    val offentligIdent: String? = null,
    val ansattFom: LocalDate? = null,
    val ansattTom: LocalDate? = null,
)
