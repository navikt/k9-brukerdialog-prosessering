package no.nav.brukerdialog.ytelse.opplæringspenger.api.domene

import io.swagger.v3.oas.annotations.Hidden
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import no.nav.k9.søknad.felles.type.Periode
import java.time.Duration
import java.time.LocalDate
import java.util.UUID
import no.nav.k9.søknad.ytelse.olp.v1.kurs.Kursholder as K9Kursholder
import no.nav.k9.søknad.ytelse.olp.v1.kurs.Kurs as K9Kurs
import no.nav.k9.søknad.ytelse.olp.v1.kurs.Reise as K9Reise

data class Kurs(
    @field:Valid val kursholder: Kursholder,
    val enkeltdagEllerPeriode: KursVarighetType,
    @field:Valid val kursperioder: List<Periode>? = null,
    @field:Valid val kursdager: List<KursDag>? = null,
    @field:Valid val reise: Reise? = null,
) {
    fun tilK9Format(): K9Kurs {
        return when (enkeltdagEllerPeriode) {
            KursVarighetType.ENKELTDAG -> K9Kurs(
                kursholder.tilK9Format(),
                kursdager?.map { Periode(it.dato, it.dato) },
                Reise(reiserUtenforKursdager = false).tilK9Format()
            )

            KursVarighetType.PERIODE -> K9Kurs(
                kursholder.tilK9Format(),
                kursperioder,
                reise?.tilK9Format()
            )
        }
    }

    @Hidden
    @AssertTrue(message = "Hvis enkeltdagEllerPeriode=PERIODE må det eksistere kursperioder og reise")
    fun isKursMedPerioder(): Boolean {
        if (enkeltdagEllerPeriode == KursVarighetType.PERIODE) {
            return !kursperioder.isNullOrEmpty() && kursdager.isNullOrEmpty() && reise != null
        }
        return true
    }

    @Hidden
    @AssertTrue(message = "Hvis enkeltdagEllerPeriode=ENKELTDAG må det eksistere kursdager og ikke reise")
    fun isKursMedDager(): Boolean {
        if (enkeltdagEllerPeriode == KursVarighetType.ENKELTDAG) {
            return !kursdager.isNullOrEmpty() && kursperioder.isNullOrEmpty() && reise == null
        }
        return true
    }
}

data class Kursholder(
    val uuid: UUID? = null,
    @field:NotBlank(message = "Kan ikke være tom") val navn: String
) {
    fun tilK9Format(): K9Kursholder {
        return K9Kursholder(navn, uuid)
    }
}

data class KursDag(
    val dato: LocalDate,
    val tidKurs: Duration,
    val tidReise: Duration? = null
)

data class Reise(
    @field:NotNull(message = "Kan ikke være null") val reiserUtenforKursdager: Boolean,
    val reisedager: List<LocalDate>? = null,
    val reisedagerBeskrivelse: String? = null
) {
    @Hidden
    @AssertTrue(message = "Dersom 'reiserUtenforKursdager' er true, kan ikke 'reisedager' være tom liste")
    fun isReisedagerMedDager(): Boolean {
        if (reiserUtenforKursdager) {
            return !reisedager.isNullOrEmpty()
        }
        return true
    }

    @Hidden
    @AssertTrue(message = "Dersom 'reiserUtenforKursdager' er true, må man sende med beskrivelse")
    fun isReisedagerMedBeskrivelse(): Boolean {
        if (reiserUtenforKursdager) {
            return !reisedagerBeskrivelse.isNullOrEmpty()
        }
        return true
    }

    fun tilK9Format(): K9Reise {
        return K9Reise(
            reiserUtenforKursdager,
            reisedager,
            reisedagerBeskrivelse
        )
    }
}

enum class KursVarighetType {
    ENKELTDAG,
    PERIODE
}
