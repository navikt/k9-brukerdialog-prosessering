package no.nav.brukerdialog.validation.fritekst

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import no.nav.brukerdialog.utils.StringUtils


class FritekstValidator : ConstraintValidator<ValidFritekst, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value.isNullOrEmpty()) return true

        // Filtrer ut alle tegn som ikke matcher FRITEKST_REGEX og konverter til liste
        val invalidCharsList: List<Char> =
            value.filter { !StringUtils.FRITEKST_REGEX.matches(it.toString()) }.toList()

        if (invalidCharsList.isNotEmpty()) {
            // For hvert ugyldig tegn: hvis det er et kontrolltegn (f.eks. null-byte), formatter med dobbel backslash og Unicode-escape,
            // ellers behold tegnet som det er.
            val invalidCharsString = invalidCharsList.joinToString(separator = ", ") { char ->
                if (char.isISOControl()) {
                    "\\\\u" + char.code.toString(16).padStart(4, '0')
                } else {
                    char.toString()
                }
            }
            context.disableDefaultConstraintViolation()
            context.buildConstraintViolationWithTemplate("Ugyldige tegn funnet i teksten: $invalidCharsString")
                .addConstraintViolation()
            return false
        }
        return true
    }
}
