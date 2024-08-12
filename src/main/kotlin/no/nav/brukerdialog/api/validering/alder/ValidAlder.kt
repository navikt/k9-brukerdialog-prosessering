package no.nav.brukerdialog.api.validering.alder

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [AlderValidator::class])
annotation class ValidAlder(
    val alder: Int = 0,
    val message: String = "{landkode} er ikke en gyldig ISO 3166-1 alpha-3 kode",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
