package no.nav.k9brukerdialogprosessering.api.validering.landkode

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [LandkodeValidator::class])
annotation class ValidLandkode(
    val message: String = "{landkode} er ikke en gyldig ISO 3166-1 alpha-3 kode.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
