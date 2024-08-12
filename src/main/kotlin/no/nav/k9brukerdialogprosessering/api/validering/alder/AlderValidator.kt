package no.nav.k9brukerdialogprosessering.api.validering.alder

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import no.nav.k9brukerdialogprosessering.utils.erLikEllerEtter
import java.time.LocalDate

class AlderValidator : ConstraintValidator<ValidAlder, LocalDate> {

    private lateinit var message: String
    private var maxAlder: Int = 0


    override fun initialize(constraintAnnotation: ValidAlder) {
        this.maxAlder = constraintAnnotation.alder
        this.message = constraintAnnotation.message
    }

    override fun isValid(value: LocalDate?, context: ConstraintValidatorContext): Boolean {
        if (value == null) {
            return true
        }

        val alder = LocalDate.now().minusYears(maxAlder.toLong())
        val isValid = value.erLikEllerEtter(alder)

        if (!isValid) {
            context.disableDefaultConstraintViolation()
            context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation()
        }
        return isValid
    }
}
