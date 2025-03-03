package no.nav.brukerdialog.validation.fritekst

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [FritekstValidator::class])
annotation class ValidFritekst(
    val message: String = "Ugyldig tegn funnet: {invalidChar}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
