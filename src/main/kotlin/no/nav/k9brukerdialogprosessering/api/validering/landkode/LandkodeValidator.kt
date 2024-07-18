package no.nav.k9brukerdialogprosessering.api.validering.landkode

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.util.*

class LandkodeValidator : ConstraintValidator<ValidLandkode, String> {
    companion object {
        private val LANDKODER: MutableSet<String> =
            Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA3).toMutableSet().also {
                it.add("XXK") // Kode for "Kosovo"
            }
    }

    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value == null) {
            return true // @NotBlank will handle null case
        }

        val isValid = LANDKODER.contains(value)
        if (!isValid) {
            context.disableDefaultConstraintViolation()
            context.buildConstraintViolationWithTemplate("$value er ikke en gyldig ISO 3166-1 alpha-3 kode.")
                .addConstraintViolation()
        }
        return isValid
    }
}
