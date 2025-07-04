package no.nav.brukerdialog.ytelse.opplæringspenger.api.domene

import jakarta.validation.Valid
import no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold.UtenlandsoppholdPeriodeInfo
import no.nav.k9.søknad.felles.type.Periode
import org.jetbrains.annotations.NotNull
import no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold as K9Utenlandsopphold

data class UtenlandsoppholdIPerioden(
    @field:NotNull val skalOppholdeSegIUtlandetIPerioden: Boolean,
    @field:Valid val opphold: List<Utenlandsopphold> = listOf(),
) {
    internal fun tilK9Utenlandsopphold() = K9Utenlandsopphold().medPerioder(
        mutableMapOf<Periode, UtenlandsoppholdPeriodeInfo>().apply {
            opphold.forEach { this[it.k9Periode()] = it.tilK9UtenlandsoppholdPeriodeInfo() }
        }
    )
}
